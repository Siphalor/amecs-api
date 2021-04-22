package de.siphalor.amecs.impl.mixin;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.KeyBindingManager;
import de.siphalor.amecs.impl.ModifierPrefixTextProvider;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding implements IKeyBinding {
	@Shadow
	private InputUtil.Key boundKey;

	@Shadow
	private int timesPressed;

	@Shadow
	@Final
	private static Map<InputUtil.Key, KeyBinding> keyToBindings;

	@Shadow
	@Final
	private static Map<String, KeyBinding> keysById;

	@Unique
	private final KeyModifiers keyModifiers = new KeyModifiers();

	@Override
	public InputUtil.Key amecs$getKeyCode() {
		return boundKey;
	}

	@Override
	public int amecs$getTimesPressed() {
		return timesPressed;
	}

	@Override
	public void amecs$setTimesPressed(int timesPressed) {
		this.timesPressed = timesPressed;
	}

	@Override
	public KeyModifiers amecs$getKeyModifiers() {
		return keyModifiers;
	}

	@Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V", at = @At("RETURN"))
	private void onConstructed(String id, InputUtil.Type type, int defaultCode, String category, CallbackInfo callbackInfo) {
		keyToBindings.remove(boundKey);
		KeyBindingManager.register((KeyBinding) (Object) this);
	}

	@Inject(method = "getBoundKeyLocalizedText", at = @At("TAIL"), cancellable = true)
	public void getLocalizedName(CallbackInfoReturnable<Text> callbackInfoReturnable) {
		Text name = boundKey.getLocalizedText();
		Text fullName;
		BaseText temp;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		ModifierPrefixTextProvider.Variation variation = ModifierPrefixTextProvider.Variation.WIDEST;
		do {
			fullName = name;
			if (keyModifiers.getControl()) {
				temp = AmecsAPI.CONTROL_PREFIX.getText(variation);
				temp.append(fullName);
				fullName = temp;
			}
			if (keyModifiers.getShift()) {
				temp = AmecsAPI.SHIFT_PREFIX.getText(variation);
				temp.append(fullName);
				fullName = temp;
			}
			if (keyModifiers.getAlt()) {
				temp = AmecsAPI.ALT_PREFIX.getText(variation);
				temp.append(fullName);
				fullName = temp;
			}
		} while ((variation = variation.shorter) != null && textRenderer.getWidth(fullName) > 70);

		callbackInfoReturnable.setReturnValue(fullName);
	}

	@Inject(method = "matchesKey", at = @At("RETURN"), cancellable = true)
	public void matchesKey(int keyCode, int scanCode, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if(!keyModifiers.isUnset() && !keyModifiers.isPressed()) callbackInfoReturnable.setReturnValue(false);
	}

	@Inject(method = "matchesMouse", at = @At("RETURN"), cancellable = true)
	public void matchesMouse(int mouse, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(!keyModifiers.isUnset() && !keyModifiers.isPressed()) callbackInfoReturnable.setReturnValue(false);
	}

	@Inject(method = "equals", at = @At("RETURN"), cancellable = true)
	public void equals(KeyBinding other, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (!keyModifiers.equals(((IKeyBinding) other).amecs$getKeyModifiers())) callbackInfoReturnable.setReturnValue(false);
	}

	@Inject(method = "onKeyPressed", at = @At("HEAD"), cancellable = true)
	private static void onKeyPressed(InputUtil.Key keyCode, CallbackInfo callbackInfo) {
		KeyBindingManager.onKeyPressed(keyCode);
		callbackInfo.cancel();
	}

	@Inject(method = "setKeyPressed", at = @At("HEAD"))
	private static void setKeyPressed(InputUtil.Key keyCode, boolean pressed, CallbackInfo callbackInfo) {
		KeyBindingManager.setKeyPressed(keyCode, pressed);
	}

	@Inject(method = "updatePressedStates", at = @At("HEAD"), cancellable = true)
	private static void updatePressedStates(CallbackInfo callbackInfo) {
		KeyBindingManager.updatePressedStates();
		callbackInfo.cancel();
	}

	@Inject(method = "updateKeysByCode", at = @At("HEAD"), cancellable = true)
	private static void updateKeyBindings(CallbackInfo callbackInfo) {
		KeyBindingManager.keysById.clear();
		keysById.values().forEach(KeyBindingManager::register);
		callbackInfo.cancel();
	}

	@Inject(method = "unpressAll", at = @At("HEAD"), cancellable = true)
	private static void unpressAll(CallbackInfo callbackInfo) {
		KeyBindingManager.unpressAll();
		callbackInfo.cancel();
	}

	@Inject(method = "isDefault", at = @At("HEAD"), cancellable = true)
	public void isDefault(CallbackInfoReturnable<Boolean> cir) {
		//noinspection ConstantConditions
		if (!((Object) this instanceof AmecsKeyBinding)) {
			if (!keyModifiers.isUnset()) {
				cir.setReturnValue(false);
			}
		}
	}

	@SuppressWarnings("unused")
	private static Map<String, KeyBinding> amecs$getIdToKeyBindingMap() {
		return keysById;
	}
}
