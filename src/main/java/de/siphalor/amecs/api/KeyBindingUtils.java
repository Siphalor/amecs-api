package de.siphalor.amecs.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import de.siphalor.amecs.impl.KeyBindingManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;

/**
 * Utility methods and constants for Amecs and vanilla key bindings
 */
@Environment(EnvType.CLIENT)
public class KeyBindingUtils {
	public static final int MOUSE_SCROLL_UP = 512;
	public static final int MOUSE_SCROLL_DOWN = 513;

	private static double lastScrollAmount = 0;
	private static Map<String, KeyBinding> idToKeyBindingMap;

	/**
	 * Gets the last (y directional) scroll delta
	 *
	 * @return the value
	 */
	public static double getLastScrollAmount() {
		return lastScrollAmount;
	}

	/**
	 * Sets the last (y directional) scroll amount. <b>For internal use only.</b>
	 *
	 * @param lastScrollAmount
	 *            the amount
	 */
	public static void setLastScrollAmount(double lastScrollAmount) {
		KeyBindingUtils.lastScrollAmount = lastScrollAmount;
	}

	/**
	 * Gets the "official" idToKeys map
	 *
	 * @return the map (use with care)
	 */
	public static Map<String, KeyBinding> getIdToKeyBindingMap() {
		if (idToKeyBindingMap == null) {
			try {
				// reflections accessors should be initialized statically if the are static
				// but in this case its fine because we only do this once because it is cached in a static field

				// noinspection JavaReflectionMemberAccess
				Method method = KeyBinding.class.getDeclaredMethod("amecs$getIdToKeyBindingMap");
				method.setAccessible(true);
				// noinspection unchecked
				idToKeyBindingMap = (Map<String, KeyBinding>) method.invoke(null);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return idToKeyBindingMap;
	}

	/**
	 * Unregisters a keybinding from input querying but is NOT removed from the controls GUI
	 * <br>
	 * if you unregister a keybinding which is already in the controls GUI you can call {@link #registerHiddenKeyBinding(KeyBinding)} with this keybinding to undo this
	 * <p>
	 * This is possible even after the game initialized
	 *
	 * @param keyBinding
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	public static boolean unregisterKeyBinding(KeyBinding keyBinding) {
		return KeyBindingManager.unregister(keyBinding);
	}

	/**
	 * Unregisters a keybinding with the given id
	 * <br>
	 * for more details {@link #unregisterKeyBinding(KeyBinding)}
	 *
	 * @see #unregisterKeyBinding(KeyBinding)
	 * @param id
	 *            the translation key
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	public static boolean unregisterKeyBinding(String id) {
		KeyBinding keyBinding = getIdToKeyBindingMap().remove(id);
		return KeyBindingManager.unregister(keyBinding);
	}

	/**
	 * Registers a keybinding for input querying but is NOT added to the controls GUI
	 * <br>
	 * you can register a keybinding which is already in the controls GUI but was removed from input querying via {@link #unregisterKeyBinding(KeyBinding)}
	 * <p>
	 * This is possible even after the game initialized
	 *
	 * @param keyBinding
	 * @return whether the keyBinding was added. It is not added if it is already contained
	 */
	public static boolean registerHiddenKeyBinding(KeyBinding keyBinding) {
		return KeyBindingManager.register(keyBinding);
	}
}
