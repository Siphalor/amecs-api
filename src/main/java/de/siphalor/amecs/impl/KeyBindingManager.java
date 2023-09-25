/*
 * Copyright 2020-2023 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.siphalor.amecs.impl;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class KeyBindingManager {
	// split it in two maps because it is ways faster to only stream the map with the objects we need
	// rather than streaming all and throwing out a bunch every time
	public static final Map<InputUtil.KeyCode, List<KeyBinding>> keysById = new HashMap<>();
	public static final Map<InputUtil.KeyCode, List<KeyBinding>> priorityKeysById = new HashMap<>();

	private static final List<KeyBinding> pressedKeyBindings = new ArrayList<>(10);

	private KeyBindingManager() {}

	/**
	 * Removes a key binding from one of the internal maps
	 * @param targetMap the key binding map to remove from
	 * @param keyBinding the key binding to remove
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	private static boolean removeKeyBindingFromMap(Map<InputUtil.KeyCode, List<KeyBinding>> targetMap, KeyBinding keyBinding) {
		// we need to get the backing list to remove elements thus we can not use any of the other methods that return streams
		InputUtil.KeyCode keyCode = ((IKeyBinding) keyBinding).amecs$getBoundKey();
		List<KeyBinding> keyBindings = targetMap.get(keyCode);
		if (keyBindings == null) {
			return false;
		}
		boolean removed = false;
		// while loop to ensure that we remove all equal KeyBindings if for some reason there should be duplicates
		while (keyBindings.remove(keyBinding)) {
			removed = true;
		}
		return removed;
	}

	/**
	 * Adds a key binding to one of the internal maps
	 * @param targetMap the key binding map to add to
	 * @param keyBinding the key binding to add
	 * @return whether the keyBinding was added. It is not added if it is already contained
	 */
	private static boolean addKeyBindingToListFromMap(Map<InputUtil.KeyCode, List<KeyBinding>> targetMap, KeyBinding keyBinding) {
		InputUtil.KeyCode keyCode = ((IKeyBinding) keyBinding).amecs$getBoundKey();
		List<KeyBinding> keyBindings = targetMap.computeIfAbsent(keyCode, k -> new ArrayList<>());
		if (keyBindings.contains(keyBinding)) {
			return false;
		}
		keyBindings.add(keyBinding);
		return true;
	}

	/**
	 * Registers a key binding to Amecs API
	 * @param keyBinding the key binding to register
	 * @return whether the keyBinding was added. It is not added if it is already contained
	 */
	public static boolean register(KeyBinding keyBinding) {
		if (keyBinding instanceof PriorityKeyBinding) {
			return addKeyBindingToListFromMap(priorityKeysById, keyBinding);
		} else {
			return addKeyBindingToListFromMap(keysById, keyBinding);
		}
	}

	public static Stream<KeyBinding> getMatchingKeyBindings(InputUtil.KeyCode keyCode, boolean priority) {
		List<KeyBinding> keyBindingList = (priority ? priorityKeysById : keysById).get(keyCode);
		if (keyBindingList == null)
			return Stream.empty();
		// If there are two key bindings, alt + y and shift + alt + y, and you press shift + alt + y, both will be triggered.
		// This is intentional.
		Stream<KeyBinding> result = keyBindingList.stream().filter(KeyBindingManager::areExactModifiersPressed);
		List<KeyBinding> keyBindings = result.collect(Collectors.toList());
		if (keyBindings.isEmpty())
			return keyBindingList.stream().filter(keyBinding -> ((IKeyBinding) keyBinding).amecs$getKeyModifiers().isUnset());
		return keyBindings.stream();
	}

	private static boolean areExactModifiersPressed(KeyBinding keyBinding) {
		return KeyBindingUtils.getBoundModifiers(keyBinding).equals(AmecsAPI.CURRENT_MODIFIERS);
	}

	public static void onKeyPressed(InputUtil.KeyCode keyCode) {
		getMatchingKeyBindings(keyCode, false).forEach(keyBinding ->
			((IKeyBinding) keyBinding).amecs$incrementTimesPressed()
		);
	}

	private static Stream<KeyBinding> getKeyBindingsFromMap(Map<InputUtil.KeyCode, List<KeyBinding>> keysById_map) {
		return keysById_map.values().stream().flatMap(Collection::stream);
	}

	private static void forEachKeyBinding(Consumer<KeyBinding> consumer) {
		getKeyBindingsFromMap(priorityKeysById).forEach(consumer);
		getKeyBindingsFromMap(keysById).forEach(consumer);
	}

	private static void forEachKeyBindingWithKey(InputUtil.KeyCode key, Consumer<KeyBinding> consumer) {
		getMatchingKeyBindings(key, true).forEach(consumer);
		getMatchingKeyBindings(key, false).forEach(consumer);
	}

	public static void updatePressedStates() {
		long windowHandle = MinecraftClient.getInstance().window.getHandle();
		forEachKeyBinding(keyBinding -> {
			InputUtil.KeyCode key = ((IKeyBinding) keyBinding).amecs$getBoundKey();
			boolean pressed = !keyBinding.isNotBound() && key.getCategory() == InputUtil.Type.KEYSYM && InputUtil.isKeyPressed(windowHandle, key.getKeyCode());
			setKeyBindingPressed(keyBinding, pressed);
		});
	}

	/**
	 * Unregisters a key binding from Amecs API
	 * @param keyBinding the key binding to unregister
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	public static boolean unregister(KeyBinding keyBinding) {
		if (keyBinding == null) {
			return false;
		}
		// avoid having to rebuild the whole entry map with KeyBinding.updateKeysByCode()
		boolean removed = false;
		removed |= removeKeyBindingFromMap(keysById, keyBinding);
		removed |= removeKeyBindingFromMap(priorityKeysById, keyBinding);
		return removed;
	}

	public static void updateKeysByCode() {
		keysById.clear();
		priorityKeysById.clear();
		KeyBindingUtils.getIdToKeyBindingMap().values().forEach(KeyBindingManager::register);
	}

	public static void setKeyBindingPressed(KeyBinding keyBinding, boolean pressed) {
		if (pressed != keyBinding.isPressed()) {
			if (pressed) {
				pressedKeyBindings.add(keyBinding);
			} else {
				pressedKeyBindings.remove(keyBinding);
			}
		}
		((IKeyBinding) keyBinding).amecs$setPressed(pressed);
	}

	public static void unpressAll() {
		KeyBindingUtils.getIdToKeyBindingMap().values().forEach(keyBinding -> ((IKeyBinding) keyBinding).amecs$reset());
	}

	public static boolean onKeyPressedPriority(InputUtil.KeyCode keyCode) {
		// because streams are lazily evaluated, this code only calls onPressedPriority so often until one returns true
		Optional<KeyBinding> keyBindings = getMatchingKeyBindings(keyCode, true).filter(keyBinding -> ((PriorityKeyBinding) keyBinding).onPressedPriority()).findFirst();
		return keyBindings.isPresent();
	}

	public static boolean onKeyReleasedPriority(InputUtil.KeyCode keyCode) {
		// because streams are lazily evaluated, this code only calls onPressedPriority so often until one returns true
		Optional<KeyBinding> keyBindings = getMatchingKeyBindings(keyCode, true).filter(keyBinding -> ((PriorityKeyBinding) keyBinding).onReleasedPriority()).findFirst();
		return keyBindings.isPresent();
	}

	public static void setKeyPressed(InputUtil.KeyCode keyCode, boolean pressed) {
		KeyModifier modifier = KeyModifier.fromKeyCode(keyCode.getKeyCode());
		AmecsAPI.CURRENT_MODIFIERS.set(modifier, pressed);

		// Update keybindings with matching modifiers and the same keycode
		forEachKeyBindingWithKey(keyCode, keyBinding -> setKeyBindingPressed(keyBinding, pressed));

		if (modifier != null && !pressed) {
			handleReleasedModifier();
		}
	}

	private static void handleReleasedModifier() {
		// Handle the case that a modifier has been released
		pressedKeyBindings.removeIf(pressedKeyBinding -> {
			KeyModifiers boundModifiers = KeyBindingUtils.getBoundModifiers(pressedKeyBinding);
			if (!AmecsAPI.CURRENT_MODIFIERS.contains(boundModifiers)) {
				((IKeyBinding) pressedKeyBinding).amecs$setPressed(false);
				return true;
			}
			return false;
		});
	}
}
