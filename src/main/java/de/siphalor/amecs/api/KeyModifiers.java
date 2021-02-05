package de.siphalor.amecs.api;

import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.Collectors;

/**
 * Defines modifiers for a key binding
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
@Environment(EnvType.CLIENT)
public class KeyModifiers {
	private BitSet value;

	/**
	 * Constructs new object with no modifiers set
	 */
	public KeyModifiers() {
		this(new BitSet(KeyModifier.getModifierCount()));
	}

	/**
	 * Constructs a new modifier object by a raw {@link BitSet}
	 * @param value the raw value with flags set
	 * @deprecated for internal use only
	 */
	@Deprecated
	public KeyModifiers(BitSet value) {
		this.value = value;
	}

	/**
	 * Constructs a new modifier object by all modifier bits
	 * @param alt sets whether the alt flag should be set
	 * @param control sets whether the control flag should be set
	 * @param shift sets whether the shift flag should be set
	 */
	public KeyModifiers(boolean alt, boolean control, boolean shift) {
		this();
		setAlt(alt);
		setControl(control);
		setShift(shift);
	}

	/**
	 * Compares this object with the currently pressed keys
	 * @return whether the modifiers match in the current context
	 */
	public boolean isPressed() {
		return equals(AmecsAPI.CURRENT_MODIFIERS);
	}

	/**
	 * Sets the raw value
	 * @param value the value with flags set
	 * @deprecated for internal use only
	 */
	@Deprecated
	public KeyModifiers setValue(BitSet value) {
		this.value = (BitSet) value.clone();
		return this;
	}

	/**
	 * Gets the raw value
	 * @return the value with all flags set
	 * @deprecated for internal use only
	 */
	@Deprecated
	public BitSet getValue() {
		return value;
	}

	/**
	 * Sets the alt flag
	 * @param value whether the alt flag should be activated or not
	 */
	public KeyModifiers setAlt(boolean value) {
		this.value.set(KeyModifier.ALT.id, value);
		return this;
	}

	/**
	 * Gets the state of the alt flag
	 * @return whether the alt key needs to be pressed
	 */
	public boolean getAlt() {
		return value.get(KeyModifier.ALT.id);
	}

	/**
	 * Sets the control flag
	 * @param value whether the control flag should be activated or not
	 */
	public KeyModifiers setControl(boolean value) {
		this.value.set(KeyModifier.CONTROL.id, value);
		return this;
	}

	/**
	 * Gets the state of the control flag
	 * @return whether the control key needs to be pressed
	 */
	public boolean getControl() {
		return value.get(KeyModifier.CONTROL.id);
	}

	/**
	 * Sets the shift flag
	 * @param value whether the shift flag should be activated or not
	 */
	public KeyModifiers setShift(boolean value) {
		this.value.set(KeyModifier.SHIFT.id, value);
		return this;
	}

	/**
	 * Gets the state of the shift flag
	 * @return whether the shift key needs to be pressed
	 */
	public boolean getShift() {
		return value.get(KeyModifier.SHIFT.id);
	}

	public void set(KeyModifier keyModifier, boolean value) {
		if(keyModifier != KeyModifier.NONE)
			this.value.set(keyModifier.id, value);
	}

	public boolean get(KeyModifier keyModifier) {
		if(keyModifier == KeyModifier.NONE)
			return true;
		return value.get(keyModifier.id);
	}

	/**
	 * Returns whether no flag is set
	 * @return value == 0
	 */
	public boolean isUnset() {
		return value.isEmpty();
	}

	/**
	 * Clears all flags
	 */
	public void unset() {
		value.clear();
	}

	/**
	 * Cleans up the flags by the key code present in the given key binding
	 * @param keyBinding the key binding from where to extract the key code
	 */
	public void cleanup(KeyBinding keyBinding) {
		int keyCode = ((IKeyBinding) keyBinding).amecs$getKeyCode().getCode();
		set(KeyModifier.fromKeyCode(keyCode), false);
	}

	/**
	 * Returns whether this object equals another one
	 * @param other another modifier object
	 * @return whether both values are equal
	 */
	public boolean equals(KeyModifiers other) {
		return value.equals(other.value);
	}

	/**
	 * @deprecated for internal use only
	 */
	@Deprecated
	public String serializeValue() {
		return Arrays.stream(value.toLongArray()).mapToObj(Long::toHexString).collect(Collectors.joining(","));
	}

	/**
	 * @deprecated for internal use only
	 */
	@Deprecated
	public static BitSet deserializeValue(String value) {
		if (value.isEmpty())
			return new BitSet(KeyModifier.getModifierCount());
		return BitSet.valueOf(Arrays.stream(value.split(",")).mapToLong(Long::valueOf).toArray());
	}
}
