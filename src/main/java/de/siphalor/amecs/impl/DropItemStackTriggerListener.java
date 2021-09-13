package de.siphalor.amecs.impl;

import net.minecraft.client.MinecraftClient;

public interface DropItemStackTriggerListener {

	public boolean handleDropItemStackEvent(MinecraftClient client);

}
