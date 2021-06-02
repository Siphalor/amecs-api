package de.siphalor.amecs.impl;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class KeyBindingManager {
	public static Map<InputUtil.Key, ConcurrentLinkedQueue<KeyBinding>> keysById = new HashMap<>();

	public static void register(KeyBinding keyBinding) {
		InputUtil.Key keyCode = ((IKeyBinding) keyBinding).amecs$getKeyCode();
		if (keysById.containsKey(keyCode)) {
			keysById.get(keyCode).add(keyBinding);
		} else {
			keysById.put(keyCode, new ConcurrentLinkedQueue<>(Collections.singleton(keyBinding)));
		}
	}

	public static Stream<KeyBinding> getMatchingKeyBindings(InputUtil.Key keyCode) {
		Queue<KeyBinding> keyBindingQueue = keysById.get(keyCode);
		if (keyBindingQueue == null) return Stream.empty();
		Stream<KeyBinding> result = keyBindingQueue.stream().filter(keyBinding -> ((IKeyBinding) keyBinding).amecs$getKeyModifiers().isPressed());
		Set<KeyBinding> keyBindings = result.collect(Collectors.toSet());
		if (keyBindings.isEmpty())
			return keysById.get(keyCode).stream().filter(keyBinding -> ((IKeyBinding) keyBinding).amecs$getKeyModifiers().isUnset());
		return keyBindings.stream();
	}

	public static void onKeyPressed(InputUtil.Key keyCode) {
		boolean nmuk = FabricLoader.getInstance().isModLoaded("nmuk");
		getMatchingKeyBindings(keyCode).filter(keyBinding -> !(keyBinding instanceof PriorityKeyBinding)).forEach(keyBinding -> {
			((IKeyBinding) keyBinding).amecs$setTimesPressed(((IKeyBinding) keyBinding).amecs$getTimesPressed() + 1);
			if (nmuk) {
				KeyBinding parent = NMUKProxy.getParent(keyBinding);
				if (parent != null) {
					((IKeyBinding) parent).amecs$setTimesPressed(((IKeyBinding) parent).amecs$getTimesPressed() + 1);
				}
			}
		});
	}

	public static void updatePressedStates() {
		Collection<KeyBinding> keyBindings = KeyBindingManager.keysById.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
		for (KeyBinding keyBinding : keyBindings) {
			boolean pressed = !keyBinding.isUnbound() && ((IKeyBinding) keyBinding).amecs$getKeyCode().getCategory() == InputUtil.Type.KEYSYM && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), ((IKeyBinding) keyBinding).amecs$getKeyCode().getCode());
			keyBinding.setPressed(pressed);
		}
	}

	public static void updateKeysByCode() {
		keysById.clear();
		KeyBindingUtils.getIdToKeyBindingMap().values().forEach(KeyBindingManager::register);
	}

	public static void unpressAll() {
		KeyBindingUtils.getIdToKeyBindingMap().values().forEach(keyBinding -> keyBinding.setPressed(false));
	}

	public static boolean onKeyPressedPriority(InputUtil.Key keyCode) {
		Set<KeyBinding> keyBindings = getMatchingKeyBindings(keyCode).filter(keyBinding -> keyBinding instanceof PriorityKeyBinding).collect(Collectors.toSet());
		for (KeyBinding keyBinding : keyBindings) {
			if (((PriorityKeyBinding) keyBinding).onPressedPriority()) {
				return true;
			}
		}
		return false;
	}

	public static void setKeyPressed(InputUtil.Key keyCode, boolean pressed) {
		AmecsAPI.CURRENT_MODIFIERS.set(KeyModifier.fromKeyCode(keyCode.getCode()), pressed);

		getMatchingKeyBindings(keyCode).forEach(keyBinding -> keyBinding.setPressed(pressed));
	}
}
