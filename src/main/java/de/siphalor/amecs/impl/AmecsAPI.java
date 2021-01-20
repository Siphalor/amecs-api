package de.siphalor.amecs.impl;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class AmecsAPI {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "amecsapi";
    public static final String MOD_NAME = "Amecs API";

    public static final KeyModifiers CURRENT_MODIFIERS = new KeyModifiers();
    public static final ModifierPrefixTextProvider ALT_PREFIX = new ModifierPrefixTextProvider(KeyModifier.ALT);
    public static final ModifierPrefixTextProvider CONTROL_PREFIX = new ModifierPrefixTextProvider(KeyModifier.CONTROL);
    public static final ModifierPrefixTextProvider SHIFT_PREFIX = new ModifierPrefixTextProvider(KeyModifier.SHIFT);

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }
}
