package de.siphalor.amecs.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.amecs.api.input.InputHandlerManager;
import net.minecraft.client.MinecraftClient;

@Mixin(value = MinecraftClient.class, priority = 50)
public abstract class MixinMinecraftClient {

	@Inject(method = "handleInputEvents()V", at = @At(value = "HEAD"))
	private void handleInputEvents(CallbackInfo ci) {
		InputHandlerManager.handleInputEvents((MinecraftClient) (Object) this);
	}

}
