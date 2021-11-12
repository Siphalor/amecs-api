package de.siphalor.amecs.impl.mixin;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.KeyBindingManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Keyboard.class)
public class MixinKeyboard {

	@Shadow
	private boolean repeatEvents;

	@Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;hasControlDown()Z", ordinal = 1, shift = At.Shift.BEFORE), cancellable = true)
	private void onKeyPriority(long window, int int_1, int int_2, int int_3, int int_4, CallbackInfo callbackInfo) {
		if (int_3 == 1 || (int_3 == 2 && repeatEvents)) {
			if (KeyBindingManager.onKeyPressedPriority(InputUtil.fromKeyCode(int_1, int_2)))
				callbackInfo.cancel();
		}
	}

	@Inject(method = "onKey", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J", ordinal = 0))
	private void onKey(long window, int int_1, int int_2, int int_3, int int_4, CallbackInfo callbackInfo) {
		// Key released
		if (int_3 == 0 && MinecraftClient.getInstance().currentScreen instanceof KeybindsScreen) {
			KeybindsScreen screen = (KeybindsScreen) MinecraftClient.getInstance().currentScreen;

			screen.selectedKeyBinding = null;
			screen.lastKeyCodeUpdateTime = Util.getMeasuringTimeMs();
		}

		AmecsAPI.CURRENT_MODIFIERS.set(KeyModifier.fromKeyCode(InputUtil.fromKeyCode(int_1, int_2).getCode()), int_3 != 0);
	}
}
