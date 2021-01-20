package de.siphalor.amecs.api;

import de.siphalor.amecs.impl.AmecsAPI;
import org.apache.commons.lang3.ArrayUtils;

@SuppressWarnings("WeakerAccess")
public class KeyModifier {
	public static final KeyModifier NONE = new KeyModifier("none", -1);
	public static final KeyModifier ALT = new KeyModifier("alt", 0, 342, 346);
	public static final KeyModifier CONTROL = new KeyModifier("control", 1, 341, 345);
	public static final KeyModifier SHIFT = new KeyModifier("shift", 2, 340, 344);

	public final String name;
	public final int id;
	final int[] keyCodes;

	private KeyModifier(String name, int id, int... keyCodes) {
		this.name = name;
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

	public String getTranslationKey() {
		return AmecsAPI.MOD_ID + ".modifier." + name;
	}

	public static int getModifierCount() {
		return 3;
	}
}
