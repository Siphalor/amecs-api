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

import de.siphalor.amecs.impl.AmecsAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBindingEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.options.ControlsListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class MixinKeyBindingEntry implements IKeyBindingEntry {
	private static final String DESCRIPTION_SUFFIX = "." + AmecsAPI.MOD_ID + ".description";
	@Shadow
	@Final
	private KeyBinding binding;
	@Shadow
	@Final
	private String bindingName;
	@Shadow
	@Final
	private ButtonWidget editButton;

	@Unique
	private List<String> description;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onConstructed(ControlsListWidget parent, KeyBinding keyBinding, CallbackInfo callbackInfo) {
		String descriptionKey = binding.getId() + DESCRIPTION_SUFFIX;
		if (I18n.hasTranslation(descriptionKey)) {
			description = Arrays.asList(StringUtils.split(I18n.translate(descriptionKey), '\n'));
		} else {
			description = null;
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	public void onRendered(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo callbackInfo) {
		if (description != null && mouseY >= y && mouseY < y + entryHeight && mouseX < editButton.x) {
			MinecraftClient.getInstance().currentScreen.renderTooltip(description, mouseX, mouseY);
		}
	}

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "method_19870(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("RETURN"))
	public void onResetButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
		((IKeyBinding) binding).amecs$getKeyModifiers().unset();
		if (binding instanceof AmecsKeyBinding)
			((AmecsKeyBinding) binding).resetKeyBinding();
	}

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "method_19871(Lnet/minecraft/client/options/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("HEAD"))
	public void onEditButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
		((IKeyBinding) binding).amecs$getKeyModifiers().unset();
		binding.setKeyCode(InputUtil.UNKNOWN_KEYCODE);
	}

	@Override
	public String amecs$getBindingName() {
		return bindingName;
	}

	@Override
	public KeyBinding amecs$getKeyBinding() {
		return binding;
	}
}
