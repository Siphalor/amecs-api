package de.siphalor.amecs.api.input;

import java.util.LinkedHashSet;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;

public class InputHandlerManager {

	// all methods and fields in this class must be used from main thread only or manual synchronization is required
	private static final LinkedHashSet<InputEventHandler> inputHandler = new LinkedHashSet<>();

	/**
	 * This method is called from MinecraftClient.handleInputEvents()
	 * <br> It calls all registered InputEventHandler
	 * @param client 
	 */
	@ApiStatus.Internal
	public static void handleInputEvents(MinecraftClient client) {
		for (InputEventHandler handler : inputHandler) {
			handler.handleInput(client);
		}
	}

	public static boolean registerInputEventHandler(InputEventHandler handler) {
		return inputHandler.add(handler);
	}

	public static boolean removeInputEventHandler(InputEventHandler handler) {
		return inputHandler.remove(handler);
	}

}
