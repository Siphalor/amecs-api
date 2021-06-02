package de.siphalor.amecs.impl;

import de.siphalor.nmuk.api.NMUKAlternatives;
import net.minecraft.client.options.KeyBinding;

public class NMUKProxy {
	public static KeyBinding getParent(KeyBinding binding) {
		return NMUKAlternatives.getBase(binding);
	}
}
