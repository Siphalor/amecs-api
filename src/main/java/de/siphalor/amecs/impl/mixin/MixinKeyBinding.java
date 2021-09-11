package de.siphalor.amecs.impl.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.KeyBindingManager;
import de.siphalor.amecs.impl.ModifierPrefixTextProvider;
import de.siphalor.amecs.impl.NOPMap;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

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
	private static Map<String, KeyBinding> keysById;

	// set it to a NOPMap meaning everything done with this map is ignored. Because setting it to null would cause problems
	// ... even if we remove the put in the KeyBinding constructor. Because maybe in the future this map is used elsewhere or a other mod uses it
	@Shadow
	@Final
	private static Map<InputUtil.Key, KeyBinding> keyToBindings = NOPMap.nopMap();

	@Unique
	private final KeyModifiers keyModifiers = new KeyModifiers();

	@Override
	public InputUtil.Key amecs$getBoundKey() {
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
	public void amecs$incrementTimesPressed() {
		timesPressed++;
	}
	
	@Invoker("reset")
	@Override
	public abstract void amecs$reset();
	
	@Override
	public KeyModifiers amecs$getKeyModifiers() {
		return keyModifiers;
	}

	@Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V", at = @At("RETURN"))
	private void onConstructed(String id, InputUtil.Type type, int defaultCode, String category, CallbackInfo callbackInfo) {
		KeyBindingManager.register((KeyBinding) (Object) this);
	}

	@Inject(method = "getBoundKeyLocalizedText", at = @At("TAIL"), cancellable = true)
	public void getLocalizedName(CallbackInfoReturnable<Text> callbackInfoReturnable) {
		Text name = boundKey.getLocalizedText();
		Text fullName;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		ModifierPrefixTextProvider.Variation variation = ModifierPrefixTextProvider.Variation.WIDEST;
		do {
			fullName = name;
			for(KeyModifier keyModifier : KeyModifier.VALUES) {
				if(keyModifier == KeyModifier.NONE) {
					continue;
				}
				
				if(keyModifiers.get(keyModifier)) {
					fullName = keyModifier.textProvider.getText(variation).append(fullName);
				}
			}
		} while ((variation = variation.getSmaller()) != null && textRenderer.getWidth(fullName) > 70);

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

	@Inject(method = "setKeyPressed", at = @At("HEAD"), cancellable = true)
	private static void setKeyPressed(InputUtil.Key keyCode, boolean pressed, CallbackInfo callbackInfo) {
		KeyBindingManager.setKeyPressed(keyCode, pressed);
		callbackInfo.cancel();
	}

	@Inject(method = "updatePressedStates", at = @At("HEAD"), cancellable = true)
	private static void updatePressedStates(CallbackInfo callbackInfo) {
		KeyBindingManager.updatePressedStates();
		callbackInfo.cancel();
	}

	@Inject(method = "updateKeysByCode", at = @At("HEAD"), cancellable = true)
	private static void updateKeysByCode(CallbackInfo callbackInfo) {
		KeyBindingManager.updateKeysByCode();
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
