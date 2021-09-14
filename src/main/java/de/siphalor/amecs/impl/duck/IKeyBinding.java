package de.siphalor.amecs.impl.duck;

import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public interface IKeyBinding {
	InputUtil.Key amecs$getBoundKey();

	int amecs$getTimesPressed();

	void amecs$setTimesPressed(int timesPressed);

	void amecs$incrementTimesPressed();

	void amecs$reset();

	KeyModifiers amecs$getKeyModifiers();
}
