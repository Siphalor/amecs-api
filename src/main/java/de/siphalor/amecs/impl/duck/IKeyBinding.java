package de.siphalor.amecs.impl.duck;

import de.siphalor.amecs.api.KeyModifiers;
import net.minecraft.client.util.InputUtil;

public interface IKeyBinding {
	InputUtil.KeyCode amecs$getKeyCode();

	int amecs$getTimesPressed();
	void amecs$setTimesPressed(int timesPressed);

	KeyModifiers amecs$getKeyModifiers();

	void amecs$setPressed(boolean pressed);
}
