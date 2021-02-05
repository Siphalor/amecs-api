package de.siphalor.amecs.impl.mixin;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("WeakerAccess")
@Mixin(ControlsOptionsScreen.class)
public abstract class MixinControlsOptionsScreen extends GameOptionsScreen {
	@Shadow public KeyBinding focusedBinding;

	@Shadow public long time;

	public MixinControlsOptionsScreen(Screen screen, GameOptions gameOptions, Text text) {
		super(screen, gameOptions, text);
	}

	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;setKeyCode(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V"))
	public void onClicked(double x, double y, int type, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		InputUtil.Key keyCode = ((IKeyBinding) focusedBinding).amecs$getKeyCode();
		KeyModifiers keyModifiers = ((IKeyBinding) focusedBinding).amecs$getKeyModifiers();
		if(keyCode != InputUtil.UNKNOWN_KEY) {
			int keyCodeCode = keyCode.getCode();
			keyModifiers.set(KeyModifier.fromKeyCode(keyCodeCode), true);
		}
	}

	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;setKeyCode(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V", ordinal = 0))
	public void clearKeyBinding(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		((IKeyBinding) focusedBinding).amecs$getKeyModifiers().unset();
	}

	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;setKeyCode(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V", ordinal = 1), cancellable = true)
	public void onKeyPressed(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(true);
		if(focusedBinding.isUnbound()) {
			gameOptions.setKeyCode(focusedBinding, InputUtil.fromKeyCode(keyCode, scanCode));
		} else {
			int mainKeyCode = ((IKeyBinding) focusedBinding).amecs$getKeyCode().getCode();
			KeyModifiers keyModifiers = ((IKeyBinding) focusedBinding).amecs$getKeyModifiers();
			KeyModifier mainKeyModifier = KeyModifier.fromKeyCode(mainKeyCode);
			KeyModifier keyModifier = KeyModifier.fromKeyCode(keyCode);
			if (mainKeyModifier != KeyModifier.NONE && keyModifier == KeyModifier.NONE) {
				keyModifiers.set(mainKeyModifier, true);
				gameOptions.setKeyCode(focusedBinding, InputUtil.fromKeyCode(keyCode, scanCode));
			} else {
				keyModifiers.set(keyModifier, true);
				keyModifiers.cleanup(focusedBinding);
			}
		}
		time = Util.getMeasuringTimeMs();
		KeyBinding.updateKeysByCode();
	}
}
