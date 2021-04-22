package de.siphalor.amecs.impl.duck;

import de.siphalor.amecs.api.KeyModifiers;
import net.minecraft.client.util.InputUtil;

public interface IKeyBinding {
	InputUtil.Key amecs$getKeyCode();

	int amecs$getTimesPressed();

	void amecs$setTimesPressed(int timesPressed);

	KeyModifiers amecs$getKeyModifiers();
}
