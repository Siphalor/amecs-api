package de.siphalor.amecs.impl;

import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.input.InputHandlerManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class AmecsAPI implements ClientModInitializer {

	public static Logger LOGGER = LogManager.getLogger();

	public static final String MOD_ID = "amecsapi";
	public static final String MOD_NAME = "Amecs API";

	public static Version MINECRAFT_VERSION = null;
	public static SemanticVersion SEMANTIC_MINECRAFT_VERSION = null;

	public static final KeyModifiers CURRENT_MODIFIERS = new KeyModifiers();

	private static final String INVENTORY_CATEGORY = "key.categories.inventory";

	public static String makeKeyID(String keyName) {
		return "key." + MOD_ID + "." + keyName;
	}

	public static HotbarScrollKeyBinding KEYBINDING_SCROLL_UP;
	public static HotbarScrollKeyBinding KEYBINDING_SCROLL_DOWN;

	public static DropEntireStackKeyBinding KEYBINDING_DROP_STACK;

	// is called in MixinGameOptions.load
	public static void registerHiddenScrollKeyBindings() {
		InputHandlerManager.registerInputEventHandler(KEYBINDING_SCROLL_UP);
		InputHandlerManager.registerInputEventHandler(KEYBINDING_SCROLL_DOWN);

		// we intentionally do not register the drop stack keybinding here because it is called from MixinMinecraftClient
		// InputHandlerManager.registerInputEventHandler(KEYBINDING_DROP_STACK);
	}

	private static void createKeyBindings() {
		KEYBINDING_SCROLL_UP = new HotbarScrollKeyBinding(makeKeyID("hotbar.scroll.up"), InputUtil.Type.MOUSE, KeyBindingUtils.MOUSE_SCROLL_UP, INVENTORY_CATEGORY, new KeyModifiers(), true);
		KEYBINDING_SCROLL_DOWN = new HotbarScrollKeyBinding(makeKeyID("hotbar.scroll.down"), InputUtil.Type.MOUSE, KeyBindingUtils.MOUSE_SCROLL_DOWN, INVENTORY_CATEGORY, new KeyModifiers(), false);

		KEYBINDING_DROP_STACK = new DropEntireStackKeyBinding(makeKeyID("drop.stack"), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Q, INVENTORY_CATEGORY, new KeyModifiers().setControl(true));
	}

	private static void getMinecraftVersion() {
		Optional<ModContainer> minecraftModContainer = FabricLoader.getInstance().getModContainer("minecraft");
		if (!minecraftModContainer.isPresent()) {
			throw new IllegalStateException("Minecraft not available?!?");
		}
		MINECRAFT_VERSION = minecraftModContainer.get().getMetadata().getVersion();
		if (MINECRAFT_VERSION instanceof SemanticVersion) {
			SEMANTIC_MINECRAFT_VERSION = (SemanticVersion) MINECRAFT_VERSION;
		} else {
			AmecsAPI.log(Level.WARN, "Minecraft version is no SemVer. This will cause problems!");
		}
	}

	@Override
	public void onInitializeClient() {
		getMinecraftVersion();

		VersionedLogicMethodHelper.initLogicMethodsForClasses(Arrays.asList(HotbarScrollKeyBinding.class, DropEntireStackKeyBinding.class));

		createKeyBindings();
	}

	public static void log(Level level, String message) {
		LOGGER.log(level, "[" + MOD_NAME + "] " + message);
	}

}
