package de.siphalor.amecs.api;

import org.apache.commons.lang3.ArrayUtils;

@SuppressWarnings("WeakerAccess")
public class KeyModifier {
	public static final KeyModifier NONE = new KeyModifier(-1);
	public static final KeyModifier ALT = new KeyModifier(0, 342, 346);
	public static final KeyModifier CONTROL = new KeyModifier(1, 341, 345);
	public static final KeyModifier SHIFT = new KeyModifier(2, 340, 344);

	public final int id;
	final int[] keyCodes;

	private KeyModifier(int id, int... keyCodes) {
		this.id = id;
		this.keyCodes = keyCodes;
	}

	public static KeyModifier fromKeyCode(int keyCode) {
		if(ALT.matches(keyCode)) return ALT;
		if(CONTROL.matches(keyCode)) return CONTROL;
		if(SHIFT.matches(keyCode)) return SHIFT;
		return NONE;
	}

	public boolean matches(int keyCode) {
		return ArrayUtils.contains(keyCodes, keyCode);
	}

	public static int getModifierCount() {
		return 3;
	}
}
