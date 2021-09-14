package de.siphalor.amecs.impl.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.amecs.api.input.InputHandlerManager;
import de.siphalor.amecs.impl.AmecsAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

@Environment(EnvType.CLIENT)
@Mixin(value = MinecraftClient.class, priority = 50)
public abstract class MixinMinecraftClient {

	// we remember if we just did a drop then we skip the drop from the single drop keybinding
	@Unique
	private boolean justDroppedStack = false;

	@Inject(method = "handleInputEvents()V", at = @At(value = "HEAD"))
	private void handleInputEvents(CallbackInfo ci) {
		InputHandlerManager.handleInputEvents((MinecraftClient) (Object) this);
	}

	// we add in the dropEntireStack logic before keyDrop is checked
	@Inject(method = "handleInputEvents()V", at = @At(value = "FIELD", shift = Shift.BEFORE, opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/option/GameOptions;keyDrop:Lnet/minecraft/client/option/KeyBinding;", ordinal = 0))
	private void addIn_dropEntireStack(CallbackInfo ci) {
		justDroppedStack = AmecsAPI.KEYBINDING_DROP_STACK.handleDropItemStackEvent((MinecraftClient) (Object) this);
	}

	@Redirect(method = "handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropSelectedItem(Z)Z", ordinal = 0))
	public boolean dropSelectedItem(ClientPlayerEntity player, boolean entireStack) {
		boolean dropResult = false;
		if (!justDroppedStack) {
			// ensure that entireStack is always false
			dropResult = player.dropSelectedItem(false);
		}
		justDroppedStack = false;
		return dropResult;
	}

}
