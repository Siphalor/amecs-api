package de.siphalor.amecs.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.minecraft.client.option.KeyBinding;

@Mixin(targets = "de/siphalor/nmuk/impl/NMUKKeyBindingHelper")
public class MixinNMUKKeyBindingHelper {
	@Inject(method = "resetSingleKeyBinding", at = @At("HEAD"))
	private static void resetSingleKeyBinding(KeyBinding binding, CallbackInfo callbackInfo) {
		((IKeyBinding) binding).amecs$getKeyModifiers().unset();
		if (binding instanceof AmecsKeyBinding)
			((AmecsKeyBinding) binding).resetKeyBinding();
	}
}
