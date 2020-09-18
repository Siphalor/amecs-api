package de.siphalor.amecs.impl.mixin;

import de.siphalor.amecs.api.KeyBindingUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InputUtil.Type.class)
@Environment(EnvType.CLIENT)
public abstract class MixinInputUtilType {
	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void onRegisterKeyCodes(CallbackInfo callbackInfo) {
		InputUtil.Type.mapKey(InputUtil.Type.MOUSE, "amecsapi.key.mouse.scroll.up", KeyBindingUtils.MOUSE_SCROLL_UP);
		InputUtil.Type.mapKey(InputUtil.Type.MOUSE, "amecsapi.key.mouse.scroll.down", KeyBindingUtils.MOUSE_SCROLL_DOWN);
	}
}
