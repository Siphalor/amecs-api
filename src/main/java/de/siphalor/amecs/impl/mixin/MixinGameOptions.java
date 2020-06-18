package de.siphalor.amecs.impl.mixin;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.PrintWriter;
import java.util.Iterator;

@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(GameOptions.class)
public class MixinGameOptions {

	@Inject(
		method = "write",
		at = @At(value = "INVOKE", target = "Ljava/io/PrintWriter;println(Ljava/lang/String;)V", ordinal = 0),
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;keysAll:[Lnet/minecraft/client/options/KeyBinding;")
		),
		locals = LocalCapture.CAPTURE_FAILSOFT,
		require = 0
	)
	public void onKeyBindingWritten(CallbackInfo callbackInfo, PrintWriter printWriter, KeyBinding[] keyBindings, int keyBindingsCount, int index, KeyBinding keyBinding) {
		//noinspection deprecation
		printWriter.println(AmecsAPI.KEY_MODIFIER_GAME_OPTION + keyBinding.getTranslationKey() + ":" + ((IKeyBinding) keyBinding).amecs$getKeyModifiers().serializeValue());
	}

	@Inject(
		method = "load",
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;keysAll:[Lnet/minecraft/client/options/KeyBinding;", shift = At.Shift.BEFORE),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	public void onLoad(CallbackInfo callbackInfo, CompoundTag ct1, CompoundTag ct2, Iterator<?> iterator, String key, String value) {
        if(key.startsWith(AmecsAPI.KEY_MODIFIER_GAME_OPTION)) {
			key = key.substring(AmecsAPI.KEY_MODIFIER_GAME_OPTION.length());
			KeyBinding keyBinding = KeyBindingUtils.getIdToKeyBindingMap().get(key);
			if(keyBinding != null) {
				//noinspection deprecation
				((IKeyBinding) keyBinding).amecs$getKeyModifiers().setValue(KeyModifiers.deserializeValue(value));
			}
		}
	}
}
