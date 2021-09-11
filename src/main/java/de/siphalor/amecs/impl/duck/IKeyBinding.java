package de.siphalor.amecs.impl.duck;

import de.siphalor.amecs.api.KeyModifiers;
import net.minecraft.client.util.InputUtil;

public interface IKeyBinding {
	//changed to make it more like fabrics KeyCodeAccessor. Maybe even remove this and use fabics KeyCodeAccessor instead?
	InputUtil.Key amecs$getBoundKey();

	int amecs$getTimesPressed();

	void amecs$setTimesPressed(int timesPressed);

	void amecs$incrementTimesPressed();
	
	void amecs$reset();
	
	KeyModifiers amecs$getKeyModifiers();
}
