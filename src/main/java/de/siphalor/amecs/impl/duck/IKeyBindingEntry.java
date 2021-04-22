package de.siphalor.amecs.impl.duck;

import net.minecraft.client.options.KeyBinding;

public interface IKeyBindingEntry {
	String amecs$getBindingName();

	KeyBinding amecs$getKeyBinding();
}
