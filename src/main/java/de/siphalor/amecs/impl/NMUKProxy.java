package de.siphalor.amecs.impl;

import de.siphalor.nmuk.api.NMUKAlternatives;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;

@Environment(EnvType.CLIENT)
public class NMUKProxy {
	public static KeyBinding getParent(KeyBinding binding) {
		return NMUKAlternatives.getBase(binding);
	}
}
