package de.siphalor.amecs.impl.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.KeyBindingManager;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.options.ControlsOptionsScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

// TODO: Fix the priority when Mixin 0.8 is a thing and try again (-> MaLiLib causes incompatibilities)
@Environment(EnvType.CLIENT)
@Mixin(value = Mouse.class, priority = -2000)
public class MixinMouse {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private double eventDeltaWheel;

	@Inject(method = "onMouseButton", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0), cancellable = true)
	private void onMouseButtonPriority(long window, int type, int state, int int_3, CallbackInfo callbackInfo) {
		if (state == 1 && KeyBindingManager.onKeyPressedPriority(InputUtil.Type.MOUSE.createFromCode(type))) {
			callbackInfo.cancel();
		}
	}

	private void onScrollReceived(double deltaY) {
		// from minecraft but patched
		if (eventDeltaWheel != 0.0D && Math.signum(deltaY) != Math.signum(eventDeltaWheel)) {
			eventDeltaWheel = 0.0D;
		}

		eventDeltaWheel += deltaY;
		int scrollCount = (int) eventDeltaWheel;
		if (scrollCount == 0) {
			return;
		}

		eventDeltaWheel -= scrollCount;
		// -from minecraft

		InputUtil.KeyCode keyCode = getKeyFromScroll(scrollCount);

		KeyBinding.setKeyPressed(keyCode, true);
		scrollCount = Math.abs(scrollCount);

		while (scrollCount > 0) {
			KeyBinding.onKeyPressed(keyCode);
			scrollCount--;
		}
		KeyBinding.setKeyPressed(keyCode, false);

		// default minecrafts scroll logic is in HotbarScrollKeyBinding
	}

	@Redirect(method = "onMouseScroll", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;", ordinal = 0))
	private ClientPlayerEntity getPlayer_onMouseScroll(MinecraftClient client) {
		// we are here in the else branch of "this.client.currentScreen != null" meaning currentScreen == null
		onScrollReceived(KeyBindingUtils.getLastScrollAmount());
		// to cancel the whole else if branch
		return null;
	}

	// //TODO: Can not redirect or inject mouseScrolled??? fix later
	// @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ParentElement;mouseScrolled(DDD)Z"))
	// private boolean mouseScrolled_onMouseScroll(double mouseX, double mouseY, double amount) {
	// //following is ensured
	// // client.currentScreen != null
	//
	// //here comes the controls gui check if fixed
	//
	// boolean mouseScrolled_eventUsed = client.currentScreen.mouseScrolled(mouseX, mouseY, amount);
	// if(!mouseScrolled_eventUsed && client.currentScreen.passEvents) {
	// onScrollReceived(KeyBindingUtils.getLastScrollAmount());
	// }
	// return mouseScrolled_eventUsed;
	// }

	@Inject(method = "onMouseScroll", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private void onMouseScroll(long window, double rawX, double rawY, CallbackInfo callbackInfo, double deltaY) {
		InputUtil.KeyCode keyCode = getKeyFromScroll(deltaY);

		// check if we have scroll input for the options screen
		if (client.currentScreen instanceof ControlsOptionsScreen) {
			KeyBinding focusedBinding = ((ControlsOptionsScreen) client.currentScreen).focusedBinding;
			if (focusedBinding != null) {
				if (!focusedBinding.isNotBound()) {
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

		KeyBindingUtils.setLastScrollAmount((float) deltaY);
		if (KeyBindingManager.onKeyPressedPriority(keyCode)) {
			callbackInfo.cancel();
		}
	}

	private static InputUtil.KeyCode getKeyFromScroll(double deltaY) {
		return InputUtil.Type.MOUSE.createFromCode(deltaY > 0 ? KeyBindingUtils.MOUSE_SCROLL_UP : KeyBindingUtils.MOUSE_SCROLL_DOWN);
	}
}
