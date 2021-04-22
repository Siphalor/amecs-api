package de.siphalor.amecs.impl.mixin;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBindingEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.options.ControlsListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class MixinKeyBindingEntry implements IKeyBindingEntry {
	@Shadow
	@Final
	private KeyBinding binding;

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "method_19870(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("RETURN"))
	public void onResetButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
		((IKeyBinding) binding).amecs$getKeyModifiers().unset();
		if (binding instanceof AmecsKeyBinding) ((AmecsKeyBinding) binding).resetKeyBinding();
	}

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "method_19871(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("RETURN"))
	public void onEditButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
		((IKeyBinding) binding).amecs$getKeyModifiers().unset();
		binding.setBoundKey(InputUtil.UNKNOWN_KEY);
	}

	@Override
	public KeyBinding amecs$getKeyBinding() {
		return binding;
	}
}
