package de.siphalor.amecs.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.Level;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.input.InputEventHandler;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;

public class HotbarScrollKeyBinding extends AmecsKeyBinding implements InputEventHandler {
	private static final String SCROLLLOGIC_METHOD_PREFIX = "scrollLogic$";
	private static Method scrollLogicMethod;

	private static Method getLogicMethod() {
		TreeMap<SemanticVersion, Method> methodAndVersions = new TreeMap<>();
		for (Method m : HotbarScrollKeyBinding.class.getDeclaredMethods()) {
			String methodName = m.getName();
			if (methodName.startsWith(SCROLLLOGIC_METHOD_PREFIX)) {
				String versionString = methodName.substring(SCROLLLOGIC_METHOD_PREFIX.length()).replace('_', '.');
				try {
					SemanticVersion version = SemanticVersion.parse(versionString);
					methodAndVersions.put(version, m);
				} catch (VersionParsingException e) {
					AmecsAPI.log(Level.ERROR, "Could not parse semantic version for logic method: " + methodName);
				}
			}
		}
		if (AmecsAPI.SEMANTIC_MINECRAFT_VERSION == null) {
			return methodAndVersions.firstEntry().getValue();
		}
		Entry<SemanticVersion, Method> suitable = methodAndVersions.floorEntry(AmecsAPI.SEMANTIC_MINECRAFT_VERSION);
		if (suitable != null) {
			return suitable.getValue();
		}
		return null;
	}

	static void initLogicMethod() {
		scrollLogicMethod = getLogicMethod();
		if (scrollLogicMethod == null) {
			throw new IllegalStateException("No scrollLogic Function available for minecraft Version: " + AmecsAPI.SEMANTIC_MINECRAFT_VERSION.getFriendlyString());
		}
	}

	public static double SCROLL_SPEED = 1;
	// vanilla updates directly on the scroll callback. We do it on the handleInputEvent method to ensure a usual state when evaluating this keybinding event
	// because we might get trigged from a keyboard key when binding is changed
	public static double SCROLL_SPEED_LIMIT = Double.POSITIVE_INFINITY;

	public final boolean scrollUp;

	public HotbarScrollKeyBinding(String id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers, boolean scrollUp) {
		super(id, type, code, category, defaultModifiers);
		this.scrollUp = scrollUp;
	}

	// TODO: check if it is really equal for all versions between 1.8 - 1.17.1
	// from minecraft code: Mouse
	@SuppressWarnings("unused") // it is used via reflection
	private void scrollLogic$1_8(MinecraftClient client, int scrollCount) {
		if (client.player.isSpectator()) {
			if (client.inGameHud.getSpectatorHud().isOpen()) {
				client.inGameHud.getSpectatorHud().cycleSlot(-scrollCount);
			} else {
				float h = MathHelper.clamp(client.player.getAbilities().getFlySpeed() + scrollCount * 0.005F, 0.0F, 0.2F);
				client.player.getAbilities().setFlySpeed(h);
			}
		} else {
			client.player.getInventory().scrollInHotbar(scrollCount);
		}
	}

	// TODO: copy the byteCode from Mouse in order to remove this version check
	private void scrollLogic_currentVersion(MinecraftClient client, int scrollCount) {
		try {
			scrollLogicMethod.invoke(this, client, scrollCount);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			AmecsAPI.log(Level.ERROR, "Error while executing: " + scrollLogicMethod.getName());
			e.printStackTrace();
		}
	}

	@Override
	public void handleInput(MinecraftClient client) {
		int scrollCount = ((IKeyBinding) this).amecs$getTimesPressed();
		((IKeyBinding) this).amecs$setTimesPressed(0);

		scrollCount = (int) Math.min(SCROLL_SPEED * scrollCount, SCROLL_SPEED_LIMIT);

		scrollCount = scrollUp ? scrollCount : -scrollCount;

		// this is really necessary. Removing this will lead to an infinity loop when in spectator mode and using the command hotbar
		if (scrollCount == 0) {
			return;
		}

		scrollLogic_currentVersion(client, scrollCount);
	}
}
