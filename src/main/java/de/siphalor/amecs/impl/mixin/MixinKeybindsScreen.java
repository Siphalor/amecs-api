package de.siphalor.amecs.impl.mixin;

import net.minecraft.client.gui.screen.option.KeybindsScreen;
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
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(KeybindsScreen.class)
public abstract class MixinKeybindsScreen extends GameOptionsScreen {
	@Shadow
	public KeyBinding selectedKeyBinding;

	@Shadow
	public long lastKeyCodeUpdateTime;

	public MixinKeybindsScreen(Screen screen, GameOptions gameOptions, Text text) {
		super(screen, gameOptions, text);
	}

	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V"))
	public void onClicked(double x, double y, int type, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		InputUtil.Key key = ((IKeyBinding) selectedKeyBinding).amecs$getBoundKey();
		KeyModifiers keyModifiers = ((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers();
		if (!key.equals(InputUtil.UNKNOWN_KEY)) {
			keyModifiers.set(KeyModifier.fromKey(key), true);
		}
	}

	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V", ordinal = 0))
	public void clearKeyBinding(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers().unset();
	}

	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V", ordinal = 1), cancellable = true)
	public void onKeyPressed(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(true);
		if (selectedKeyBinding.isUnbound()) {
			gameOptions.setKeyCode(selectedKeyBinding, InputUtil.fromKeyCode(keyCode, scanCode));
		} else {
			InputUtil.Key mainKey = ((IKeyBinding) selectedKeyBinding).amecs$getBoundKey();
			KeyModifiers keyModifiers = ((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers();
			KeyModifier mainKeyModifier = KeyModifier.fromKey(mainKey);
			KeyModifier keyModifier = KeyModifier.fromKeyCode(keyCode);
			if (mainKeyModifier != KeyModifier.NONE && keyModifier == KeyModifier.NONE) {
				keyModifiers.set(mainKeyModifier, true);
				gameOptions.setKeyCode(selectedKeyBinding, InputUtil.fromKeyCode(keyCode, scanCode));
			} else {
				keyModifiers.set(keyModifier, true);
				keyModifiers.cleanup(selectedKeyBinding);
			}
		}
		lastKeyCodeUpdateTime = Util.getMeasuringTimeMs();
		KeyBinding.updateKeysByCode();
	}
}
