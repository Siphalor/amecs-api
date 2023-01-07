/*
 * Copyright 2020-2022 Siphalor
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.KeyBindingManager;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.duck.IMouse;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

// TODO: Fix the priority when Mixin 0.8 is a thing and try again (-> MaLiLib causes incompatibilities)
@Environment(EnvType.CLIENT)
@Mixin(value = Mouse.class, priority = -2000)
public class MixinMouse implements IMouse {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private double eventDeltaWheel;

	@Unique
	private boolean mouseScrolled_eventUsed;

	@Override
	public boolean amecs$getMouseScrolledEventUsed() {
		return mouseScrolled_eventUsed;
	}

	@Inject(method = "onMouseButton", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0), cancellable = true)
	private void onMouseButtonPriority(long window, int type, int state, int int_3, CallbackInfo callbackInfo) {
		if (state == 1 && KeyBindingManager.onKeyPressedPriority(InputUtil.Type.MOUSE.createFromCode(type))) {
			callbackInfo.cancel();
		}
	}

	// If this method changes make sure to also change the corresponding code in KTIG
	private void onScrollReceived(double deltaY, boolean manualDeltaWheel, float g) {
		int scrollCount;
		if (manualDeltaWheel) {
			// from minecraft but patched
			// this code might be wrong when the vanilla mc code changes
			if (eventDeltaWheel != 0.0D && Math.signum(deltaY) != Math.signum(eventDeltaWheel)) {
				eventDeltaWheel = 0.0D;
			}

			eventDeltaWheel += deltaY;
			scrollCount = (int) eventDeltaWheel;
			if (scrollCount == 0) {
				return;
			}

			eventDeltaWheel -= scrollCount;
			// -from minecraft
		} else {
			scrollCount = (int) g;
		}

		InputUtil.Key keyCode = KeyBindingUtils.getKeyFromScroll(scrollCount);

		KeyBinding.setKeyPressed(keyCode, true);
		scrollCount = Math.abs(scrollCount);

		while (scrollCount > 0) {
			KeyBinding.onKeyPressed(keyCode);
			scrollCount--;
		}
		KeyBinding.setKeyPressed(keyCode, false);

		// default minecraft scroll logic is in HotbarScrollKeyBinding in amecs
	}

	@Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void isSpectator_onMouseScroll(long window, double rawX, double rawY, CallbackInfo callbackInfo, double deltaY, float g) {
		// we are here in the else branch of "this.client.currentScreen != null" meaning currentScreen == null
		if (AmecsAPI.TRIGGER_KEYBINDING_ON_SCROLL) {
			onScrollReceived(KeyBindingUtils.getLastScrollAmount(), false, g);
		}
	}

	@SuppressWarnings("unused")
	private boolean amecs$onMouseScrolledScreen(boolean handled) {
		this.mouseScrolled_eventUsed = handled;
		if (handled) {
			return true;
		}

		if (this.client.currentScreen.passEvents) {
			if (AmecsAPI.TRIGGER_KEYBINDING_ON_SCROLL) {
				this.onScrollReceived(KeyBindingUtils.getLastScrollAmount(), true, 0);
			}
		}
		return false;
	}

	@Inject(method = "onMouseScroll", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void onMouseScroll(long window, double rawX, double rawY, CallbackInfo callbackInfo, double deltaY) {
		InputUtil.Key keyCode = KeyBindingUtils.getKeyFromScroll(deltaY);

		// check if we have scroll input for the options screen
		if (client.currentScreen instanceof ControlsOptionsScreen) {
			KeyBinding focusedBinding = ((ControlsOptionsScreen) client.currentScreen).focusedBinding;
			if (focusedBinding != null) {
				if (!focusedBinding.isUnbound()) {
					KeyModifiers keyModifiers = ((IKeyBinding) focusedBinding).amecs$getKeyModifiers();
					keyModifiers.set(KeyModifier.fromKey(((IKeyBinding) focusedBinding).amecs$getBoundKey()), true);
				}
				client.options.setKeyCode(focusedBinding, keyCode);
				KeyBinding.updateKeysByCode();
				((ControlsOptionsScreen) client.currentScreen).focusedBinding = null;
				// if we do we cancel the method because we do not want the current screen to get the scroll event
				callbackInfo.cancel();
				return;
			}
		}

		KeyBindingUtils.setLastScrollAmount(deltaY);
		if (KeyBindingManager.onKeyPressedPriority(keyCode)) {
			callbackInfo.cancel();
		}
	}
}
