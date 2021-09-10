package de.siphalor.amecs.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.impl.AmecsAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
@Mixin(InputUtil.Type.class)
public abstract class MixinInputUtilType {
	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void onRegisterKeyCodes(CallbackInfo callbackInfo) {
		InputUtil.Type.mapKey(InputUtil.Type.MOUSE, AmecsAPI.makeKeyID("mouse.scroll.up"), KeyBindingUtils.MOUSE_SCROLL_UP);
		InputUtil.Type.mapKey(InputUtil.Type.MOUSE, AmecsAPI.makeKeyID("mouse.scroll.down"), KeyBindingUtils.MOUSE_SCROLL_DOWN);
	}
}
