package de.siphalor.amecs.impl;

import de.siphalor.amecs.api.KeyModifier;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ModifierPrefixTextProvider {
	private static final Text SUFFIX = new LiteralText(" + ");
	private static final Text COMPRESSED_SUFFIX = new LiteralText("+");
	private final String translationKey;

	public ModifierPrefixTextProvider(KeyModifier modifier) {
		this(modifier.getTranslationKey());
	}

	public ModifierPrefixTextProvider(String translationKey) {
		this.translationKey = translationKey;
	}

	protected BaseText getBaseText(Variation variation) {
		switch (variation) {
			case NORMAL:
				return new TranslatableText(translationKey);
			case SHORT:
				return new TranslatableText(translationKey + ".short");
			case COMPRESSED:
			case TINY:
				return new TranslatableText(translationKey + ".tiny");
		}
		return null; // unreachable
	}

	public BaseText getText(Variation variation) {
		BaseText text = getBaseText(variation);
		if (variation == Variation.COMPRESSED) {
			text.append(COMPRESSED_SUFFIX);
		} else {
			text.append(SUFFIX);
		}
		return text;
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
