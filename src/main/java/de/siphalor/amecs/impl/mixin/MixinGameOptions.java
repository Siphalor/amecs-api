package de.siphalor.amecs.impl.mixin;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;

@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
@Mixin(GameOptions.class)
public class MixinGameOptions {
	@Unique
	private static final String KEY_MODIFIERS_PREFIX = "key_modifiers_";

	@Shadow @Final public KeyBinding[] keysAll;
	@Unique
	private File amecsOptionsFile;

	@Inject(method = "write", at = @At("RETURN"))
	public void write(CallbackInfo callbackInfo) {
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(amecsOptionsFile))) {
			for (KeyBinding binding : keysAll) {
				//noinspection deprecation
				writer.println(KEY_MODIFIERS_PREFIX + binding.getId() + ":" + ((IKeyBinding) binding).amecs$getKeyModifiers().serializeValue());
			}
		} catch (FileNotFoundException e) {
			AmecsAPI.log(Level.ERROR, "Failed to save Amecs API modifiers to options file:");
			e.printStackTrace();
		}
	}

	@Inject(method = "load", at = @At("RETURN"))
	public void load(CallbackInfo callbackInfo) {
		if (amecsOptionsFile == null) {
			amecsOptionsFile = new File(MinecraftClient.getInstance().runDirectory, "options." + AmecsAPI.MOD_ID + ".txt");
		}
		if (!amecsOptionsFile.exists()) {
			return;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(amecsOptionsFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					int colon = line.indexOf(':');
					if (colon <= 0) {
						AmecsAPI.log(Level.WARN, "Invalid line in Amecs API options file: " + line);
						continue;
					}
					String id = line.substring(0, colon);
					if (!id.startsWith(KEY_MODIFIERS_PREFIX)) {
						AmecsAPI.log(Level.WARN, "Invalid entry in Amecs API options file: " + id);
						continue;
					}
					id = id.substring(KEY_MODIFIERS_PREFIX.length());
					KeyBinding keyBinding = KeyBindingUtils.getIdToKeyBindingMap().get(id);
					if (keyBinding == null) {
						AmecsAPI.log(Level.WARN, "Unknown keybinding identifier in Amecs API options file: " + id);
						continue;
					}
					//noinspection deprecation
					((IKeyBinding) keyBinding).amecs$getKeyModifiers().setValue(KeyModifiers.deserializeValue(line.substring(colon + 1)));
				} catch (Throwable e) {
					AmecsAPI.log(Level.ERROR, "Invalid line in Amecs API options file: " + line);
				}
			}
		} catch (IOException e) {
			AmecsAPI.log(Level.ERROR, "Failed to load Amecs API options file:");
			e.printStackTrace();
		}
	}
}
