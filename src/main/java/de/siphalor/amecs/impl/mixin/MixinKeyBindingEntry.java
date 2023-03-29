/*
 * Copyright 2020-2023 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.siphalor.amecs.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBindingEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class MixinKeyBindingEntry implements IKeyBindingEntry {
	@Shadow
	@Final
	private KeyBinding binding;

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(
			method = "method_19870(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/option/ControlsListWidget;update()V")
	)
	public void onResetButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
		((IKeyBinding) binding).amecs$getKeyModifiers().unset();
		if (binding instanceof AmecsKeyBinding)
			((AmecsKeyBinding) binding).resetKeyBinding();
	}

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "method_19871(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("HEAD"))
	public void onEditButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
		((IKeyBinding) binding).amecs$getKeyModifiers().unset();
		binding.setBoundKey(InputUtil.UNKNOWN_KEY);
	}

	@Override
	public KeyBinding amecs$getKeyBinding() {
		return binding;
	}
}
