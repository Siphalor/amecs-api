package de.siphalor.amecs.api;

import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

/**
 * A {@link net.minecraft.client.option.KeyBinding} base class to be used when you want to define modifiers keys as default
 */
@Environment(EnvType.CLIENT)
public class AmecsKeyBinding extends KeyBinding {
	private final KeyModifiers defaultModifiers;

	//for backward compatibility
	/**
	 * Constructs a new amecs keybinding. And because how the vanilla key binding works. It is automatically registered.
	 * <br> See {@link KeyBindingUtils#unregisterKeyBinding(KeyBinding)} for how to unregister it
	 *
	 * @param id               the id to use
	 * @param type             the input type which triggers this keybinding
	 * @param code             the the default key code
	 * @param category         the id of the category which should include this keybinding
	 * @param defaultModifiers the default modifiers
	 */
	@Deprecated
	public AmecsKeyBinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		this("key." + id.getNamespace() + "." + id.getPath(), type, code, category, defaultModifiers);
	}

	//why limit the caller with the predefined id pattern with the id for the key but not the category? Just let caller do what he wants to do
	/**
	 * Constructs a new amecs keybinding. And because how the vanilla key binding works. It is automatically registered.
	 * <br> See {@link KeyBindingUtils#unregisterKeyBinding(KeyBinding)} for how to unregister it
	 *
	 * @param id               the id to use
	 * @param type             the input type which triggers this keybinding
	 * @param code             the the default key code
	 * @param category         the id of the category which should include this keybinding
	 * @param defaultModifiers the default modifiers
	 */
	public AmecsKeyBinding(String id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		super(id, type, code, category);
		if(defaultModifiers == null || defaultModifiers == KeyModifiers.NO_MODIFIERS) {
			defaultModifiers = new KeyModifiers(); //the modifiable version of: KeyModifiers.NO_MODIFIERS
		}
		this.defaultModifiers = defaultModifiers;
		((IKeyBinding) this).amecs$getKeyModifiers().copyModifiers(this.defaultModifiers);
	}

	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (pressed)
			onPressed();
		else
			onReleased();
	}

	/**
	 * A convenience method which gets fired when the keybinding is used
	 */
	public void onPressed() {
	}

	/**
	 * A convenience method which gets fired when the keybinding is stopped being used
	 */
	public void onReleased() {
	}

	/**
	 * Resets this keybinding (triggered when the user clicks on the "Reset" button).
	 */
	public void resetKeyBinding() {
		((IKeyBinding) this).amecs$getKeyModifiers().copyModifiers(defaultModifiers);
	}

	@Override
	public boolean isDefault() {
		return defaultModifiers.equals(((IKeyBinding) this).amecs$getKeyModifiers()) && super.isDefault();
	}
}
