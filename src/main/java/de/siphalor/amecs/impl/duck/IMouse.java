package de.siphalor.amecs.impl.duck;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IMouse {
	// this is used by KTIG
	boolean amecs$getMouseScrolledEventUsed();
}
