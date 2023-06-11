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
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class KeyBindingManager {
	// split it in two maps because it is ways faster to only stream the map with the objects we need
	// rather than streaming all and throwing out a bunch every time
	public static final Map<InputUtil.Key, List<KeyBinding>> keysById = new HashMap<>();
	public static final Map<InputUtil.Key, List<KeyBinding>> priorityKeysById = new HashMap<>();
	/**
	 * Removes a key binding from one of the internal maps
	 * @param targetMap the key binding map to remove from
	 * @param keyBinding the key binding to remove
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	private static boolean removeKeyBindingFromListFromMap(Map<InputUtil.Key, List<KeyBinding>> targetMap, KeyBinding keyBinding) {
		// we need to get the backing list to remove elements thus we can not use any of the other methods that return streams
		InputUtil.Key keyCode = ((IKeyBinding) keyBinding).amecs$getBoundKey();
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
	private static boolean addKeyBindingToListFromMap(Map<InputUtil.Key, List<KeyBinding>> targetMap, KeyBinding keyBinding) {
		InputUtil.Key keyCode = ((IKeyBinding) keyBinding).amecs$getBoundKey();
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

	public static Stream<KeyBinding> getMatchingKeyBindings(InputUtil.Key keyCode, boolean priority) {
		List<KeyBinding> keyBindingList = (priority ? priorityKeysById : keysById).get(keyCode);
		if (keyBindingList == null)
			return Stream.empty();
		// this looks not right: If you have a kb: alt + y and shift + alt + y and you press shift + alt + y both will be triggered
		// Correction: It works as it should. Leaving this comments for future readers
		Stream<KeyBinding> result = keyBindingList.stream().filter(keyBinding -> ((IKeyBinding) keyBinding).amecs$getKeyModifiers().isPressed());
		List<KeyBinding> keyBindings = result.collect(Collectors.toList());
		if (keyBindings.isEmpty())
			return keyBindingList.stream().filter(keyBinding -> ((IKeyBinding) keyBinding).amecs$getKeyModifiers().isUnset());
		return keyBindings.stream();
	}

	public static void onKeyPressed(InputUtil.Key keyCode) {
		getMatchingKeyBindings(keyCode, false).forEach(keyBinding ->
			((IKeyBinding) keyBinding).amecs$incrementTimesPressed()
		);
	}

	private static Stream<KeyBinding> getKeyBindingsFromMap(Map<InputUtil.Key, List<KeyBinding>> keysById_map) {
		return keysById_map.values().stream().flatMap(Collection::stream);
	}

	private static void forEachKeyBinding(Consumer<KeyBinding> consumer) {
		getKeyBindingsFromMap(priorityKeysById).forEach(consumer);
		getKeyBindingsFromMap(keysById).forEach(consumer);
	}

	private static void forEachKeyBindingWithKey(InputUtil.Key key, Consumer<KeyBinding> consumer) {
		getMatchingKeyBindings(key, true).forEach(consumer);
		getMatchingKeyBindings(key, false).forEach(consumer);
	}

	public static void updatePressedStates() {
		long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
		forEachKeyBinding(keyBinding -> {
			InputUtil.Key key = ((IKeyBinding) keyBinding).amecs$getBoundKey();
			boolean pressed = !keyBinding.isUnbound() && key.getCategory() == InputUtil.Type.KEYSYM && InputUtil.isKeyPressed(windowHandle, key.getCode());
			keyBinding.setPressed(pressed);
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
		// do not rebuild the entrie map if we do not have to
		// KeyBinding.updateKeysByCode();
		// instead
		boolean removed = false;
		removed |= removeKeyBindingFromListFromMap(keysById, keyBinding);
		removed |= removeKeyBindingFromListFromMap(priorityKeysById, keyBinding);
		return removed;
	}

	public static void updateKeysByCode() {
		keysById.clear();
		priorityKeysById.clear();
		KeyBindingUtils.getIdToKeyBindingMap().values().forEach(KeyBindingManager::register);
	}

	public static void unpressAll() {
		KeyBindingUtils.getIdToKeyBindingMap().values().forEach(keyBinding -> ((IKeyBinding) keyBinding).amecs$reset());
	}

	public static boolean onKeyPressedPriority(InputUtil.Key keyCode) {
		// because streams are lazily evaluated, this code only calls onPressedPriority so often until one returns true
		Optional<KeyBinding> keyBindings = getMatchingKeyBindings(keyCode, true).filter(keyBinding -> ((PriorityKeyBinding) keyBinding).onPressedPriority()).findFirst();
		return keyBindings.isPresent();
	}

	public static boolean onKeyReleasedPriority(InputUtil.Key keyCode) {
		// because streams are lazily evaluated, this code only calls onPressedPriority so often until one returns true
		Optional<KeyBinding> keyBindings = getMatchingKeyBindings(keyCode, true).filter(keyBinding -> ((PriorityKeyBinding) keyBinding).onReleasedPriority()).findFirst();
		return keyBindings.isPresent();
	}

	public static void setKeyPressed(InputUtil.Key keyCode, boolean pressed) {
		AmecsAPI.CURRENT_MODIFIERS.set(KeyModifier.fromKeyCode(keyCode.getCode()), pressed);

		forEachKeyBindingWithKey(keyCode, keyBinding -> keyBinding.setPressed(pressed));
	}
}
