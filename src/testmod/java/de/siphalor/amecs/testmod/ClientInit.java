package de.siphalor.amecs.testmod;

import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClientInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(new TestPriorityKeybinding(new Identifier("amecsapi-testmod", "priority"), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.misc", new KeyModifiers(), () -> {
			System.out.println("priority");
			return true;
		}));
	}
}
