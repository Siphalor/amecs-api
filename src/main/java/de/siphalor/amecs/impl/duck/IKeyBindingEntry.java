package de.siphalor.amecs.impl.duck;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;

@Environment(EnvType.CLIENT)
public interface IKeyBindingEntry {
	KeyBinding amecs$getKeyBinding();
}
