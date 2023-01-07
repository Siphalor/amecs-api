package de.siphalor.amecs.testmod;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.util.function.BooleanSupplier;

public class TestPriorityKeybinding extends AmecsKeyBinding implements PriorityKeyBinding {
	private final BooleanSupplier action;

	public TestPriorityKeybinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers, BooleanSupplier action) {
		super(id, type, code, category, defaultModifiers);
		this.action = action;
	}

	@Override
	public boolean onPressedPriority() {
		return action.getAsBoolean();
	}
}
