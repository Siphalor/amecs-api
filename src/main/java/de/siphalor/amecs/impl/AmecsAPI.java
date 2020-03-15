package de.siphalor.amecs.impl;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class AmecsAPI {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "amecsapi";
    public static final String MOD_NAME = "Amecs API";

    public static final KeyModifiers CURRENT_MODIFIERS = new KeyModifiers();
    public static final String KEY_MODIFIER_GAME_OPTION = MOD_ID + "$key_modifier$";

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }
}
