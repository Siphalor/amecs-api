package de.siphalor.amecs.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.options.ControlsOptionsScreen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(ControlsOptionsScreen.class)
public abstract class MixinControlsOptionsScreen extends Screen {
	@Shadow
	public KeyBinding focusedBinding;

	@Shadow
	public long time;

	@Shadow @Final private GameOptions options;

	protected MixinControlsOptionsScreen(Text title) {
		super(title);
	}

	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;setKeyCode(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/util/InputUtil$KeyCode;)V"))
	public void onClicked(double x, double y, int type, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		InputUtil.KeyCode key = ((IKeyBinding) focusedBinding).amecs$getBoundKey();
		KeyModifiers keyModifiers = ((IKeyBinding) focusedBinding).amecs$getKeyModifiers();
		if (!key.equals(InputUtil.UNKNOWN_KEYCODE)) {
			keyModifiers.set(KeyModifier.fromKey(key), true);
		}
	}

	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;setKeyCode(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/util/InputUtil$KeyCode;)V", ordinal = 0))
	public void clearKeyBinding(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		((IKeyBinding) focusedBinding).amecs$getKeyModifiers().unset();
	}

	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;setKeyCode(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/util/InputUtil$KeyCode;)V", ordinal = 1), cancellable = true)
	public void onKeyPressed(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(true);
		if (focusedBinding.isNotBound()) {
			options.setKeyCode(focusedBinding, InputUtil.getKeyCode(keyCode, scanCode));
		} else {
			InputUtil.KeyCode mainKey = ((IKeyBinding) focusedBinding).amecs$getBoundKey();
			KeyModifiers keyModifiers = ((IKeyBinding) focusedBinding).amecs$getKeyModifiers();
			KeyModifier mainKeyModifier = KeyModifier.fromKey(mainKey);
			KeyModifier keyModifier = KeyModifier.fromKeyCode(keyCode);
			if (mainKeyModifier != KeyModifier.NONE && keyModifier == KeyModifier.NONE) {
				keyModifiers.set(mainKeyModifier, true);
				options.setKeyCode(focusedBinding, InputUtil.getKeyCode(keyCode, scanCode));
			} else {
				keyModifiers.set(keyModifier, true);
				keyModifiers.cleanup(focusedBinding);
			}
		}
		time = Util.getMeasuringTimeMs();
		KeyBinding.updateKeysByCode();
	}
}
