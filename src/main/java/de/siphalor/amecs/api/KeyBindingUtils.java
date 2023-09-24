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

package de.siphalor.amecs.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import de.siphalor.amecs.impl.KeyBindingManager;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import lombok.CustomLog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * Utility methods and constants for Amecs and vanilla key bindings
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
@CustomLog
public class KeyBindingUtils {
	public static final int MOUSE_SCROLL_UP = 512;
	public static final int MOUSE_SCROLL_DOWN = 513;
	public static final int MOUSE_SCROLL_LEFT = 514;
	public static final int MOUSE_SCROLL_RIGHT = 515;

	/**
	 * The last (y directional) scroll delta
	 */
	@Deprecated
	private static double lastScrollAmountY = 0;
	private static Map<String, KeyBinding> idToKeyBindingMap;

	private KeyBindingUtils() {
	}

	/**
	 * Gets the last (y directional) scroll amount.
	 *
	 * @return the last (y directional) scroll amount
	 * @deprecated Just exists for legacy support.
	 */
	@Deprecated
	public static double getLastScrollAmount() {
		return lastScrollAmountY;
	}

	/**
	 * Sets the last (y directional) scroll amount. <b>For internal use only.</b>
	 *
	 * @param lastScrollAmountY the amount
	 * @deprecated Just exists for legacy support.
	 */
	@Deprecated
	public static void setLastScrollAmount(double lastScrollAmountY) {
		KeyBindingUtils.lastScrollAmountY = lastScrollAmountY;
	}

	/**
	 * Gets the key object for the vertical scroll direction
	 *
	 * @param deltaY the vertical (y) scroll amount {@link #getLastScrollAmount}
	 * @return the key object
	 * @see #getKeyFromVerticalScroll(double)
	 * @see #getKeyFromHorizontalScroll(double)
	 * @deprecated Just exists for legacy support. Use {@link #getKeyFromVerticalScroll(double)} instead.
	 */
	@Deprecated
	public static InputUtil.Key getKeyFromScroll(double deltaY) {
		return InputUtil.Type.MOUSE.createFromCode(deltaY > 0 ? KeyBindingUtils.MOUSE_SCROLL_UP : KeyBindingUtils.MOUSE_SCROLL_DOWN);
	}

	/**
	 * Gets the key object for the vertical scroll direction
	 *
	 * @param deltaY the vertical (y) scroll amount {@link #getLastScrollAmount}
	 * @return the key object
	 */
	public static InputUtil.Key getKeyFromVerticalScroll(double deltaY) {
		if (deltaY == 0D) {
			return null;
		}
		return InputUtil.Type.MOUSE.createFromCode(deltaY > 0 ? KeyBindingUtils.MOUSE_SCROLL_UP : KeyBindingUtils.MOUSE_SCROLL_DOWN);
	}

	/**
	 * Gets the key object for the horizontal scroll direction
	 *
	 * @param deltaX the horizontal (x) scroll amount {@link #getLastScrollAmount}
	 * @return the key object
	 */
	public static InputUtil.Key getKeyFromHorizontalScroll(double deltaX) {
		if (deltaX == 0D) {
			return null;
		}
		return InputUtil.Type.MOUSE.createFromCode(deltaX > 0 ? KeyBindingUtils.MOUSE_SCROLL_RIGHT : KeyBindingUtils.MOUSE_SCROLL_LEFT);
	}

	/**
	 * Gets the "official" idToKeys map
	 *
	 * @return the map (use with care)
	 */
	public static Map<String, KeyBinding> getIdToKeyBindingMap() {
		if (idToKeyBindingMap == null) {
			try {
				// reflections accessors should be initialized statically if they are static
				// but in this case its fine because we only do this once because it is cached in a static field

				// noinspection JavaReflectionMemberAccess
				Method method = KeyBinding.class.getDeclaredMethod("amecs$getIdToKeyBindingMap");
				method.setAccessible(true);
				// noinspection unchecked
				idToKeyBindingMap = (Map<String, KeyBinding>) method.invoke(null);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				log.error("Failed to get access to key bindings", e);
			}
		}
		return idToKeyBindingMap;
	}

	/**
	 * Unregisters a keybinding from input querying but is NOT removed from the controls GUI
	 * <br>
	 * if you unregister a keybinding which is already in the controls GUI you can call {@link #registerHiddenKeyBinding(KeyBinding)} with this keybinding to undo this
	 * <br>
	 * <br>
	 * This is possible even after the game initialized
	 *
	 * @param keyBinding the keybinding
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	public static boolean unregisterKeyBinding(KeyBinding keyBinding) {
		return unregisterKeyBinding(keyBinding.getTranslationKey());
	}

	/**
	 * Unregisters a keybinding with the given id
	 * <br>
	 * for more details {@link #unregisterKeyBinding(KeyBinding)}
	 *
	 * @param id the translation key
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 * @see #unregisterKeyBinding(KeyBinding)
	 */
	public static boolean unregisterKeyBinding(String id) {
		KeyBinding keyBinding = getIdToKeyBindingMap().remove(id);
		return KeyBindingManager.unregister(keyBinding);
	}

	/**
	 * Registers a keybinding for input querying but is NOT added to the controls GUI
	 * <br>
	 * you can register a keybinding which is already in the controls GUI but was removed from input querying via {@link #unregisterKeyBinding(KeyBinding)}
	 * <br>
	 * <br>
	 * This is possible even after the game initialized
	 *
	 * @param keyBinding the keybinding
	 * @return whether the keybinding was added. It is not added if it is already contained
	 */
	public static boolean registerHiddenKeyBinding(KeyBinding keyBinding) {
		return KeyBindingManager.register(keyBinding);
	}

	/**
	 * Gets the key modifiers that are bound to the given key binding
	 *
	 * @param keyBinding the key binding
	 * @return the key modifiers
	 */
	public static KeyModifiers getBoundModifiers(KeyBinding keyBinding) {
		return ((IKeyBinding) keyBinding).amecs$getKeyModifiers();
	}

	/**
	 * Gets the default modifiers of the given key binding.
	 * The returned value <b>must not be modified!</b>
	 *
	 * @param keyBinding the key binding
	 * @return a reference to the default modifiers
	 */
	public static KeyModifiers getDefaultModifiers(KeyBinding keyBinding) {
		if (keyBinding instanceof AmecsKeyBinding) {
			return ((AmecsKeyBinding) keyBinding).getDefaultModifiers();
		}
		return KeyModifiers.NO_MODIFIERS;
	}

	public static void resetBoundModifiers(KeyBinding keyBinding) {
		((IKeyBinding) keyBinding).amecs$getKeyModifiers().unset();
		if (keyBinding instanceof AmecsKeyBinding) {
			((AmecsKeyBinding) keyBinding).resetKeyBinding();
		}
	}
}
