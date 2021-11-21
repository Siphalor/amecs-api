package de.siphalor.amecs.impl.mixin;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.impl.AmecsAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InputUtil.Type.class)
public abstract class MixinInputUtilType {
	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void onRegisterKeyCodes(CallbackInfo callbackInfo) {
		createScrollKey("mouse.scroll.up", KeyBindingUtils.MOUSE_SCROLL_UP);
		createScrollKey("mouse.scroll.down", KeyBindingUtils.MOUSE_SCROLL_DOWN);
	}

	@Unique
	private static void createScrollKey(String name, int keyCode) {
		String keyName = AmecsAPI.makeKeyID(name);
		InputUtil.Type.mapKey(InputUtil.Type.MOUSE, keyName, keyCode);

		// Legacy compatibility (amecsapi <1.3)
		InputUtil.Key.KEYS.put("amecsapi.key." + name, InputUtil.fromTranslationKey(keyName));
	}
}
