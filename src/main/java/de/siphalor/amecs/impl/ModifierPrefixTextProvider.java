package de.siphalor.amecs.impl;

import de.siphalor.amecs.api.KeyModifier;
import net.minecraft.client.resource.language.I18n;

public class ModifierPrefixTextProvider {
	private final String translationKey;

	public ModifierPrefixTextProvider(KeyModifier modifier) {
		this(modifier.getTranslationKey());
	}

	public ModifierPrefixTextProvider(String translationKey) {
		this.translationKey = translationKey;
	}

	public String getText(Variation variation) {
		switch (variation) {
			case NORMAL:
				return I18n.translate(translationKey) + " + ";
			case SHORT:
				return I18n.translate(translationKey + ".short") + " + ";
			case TINY:
				return I18n.translate(translationKey + ".tiny") + " + ";
			case COMPRESSED:
				return I18n.translate(translationKey + ".tiny") + "+";
		}
		return null; // unreachable
	}

	public enum Variation {
		COMPRESSED(null), TINY(COMPRESSED), SHORT(TINY), NORMAL(SHORT);

		public static Variation WIDEST = NORMAL;

		public final Variation shorter;

		Variation(Variation shorter) {
			this.shorter = shorter;
		}
	}
}
