package de.siphalor.amecs.impl;

import de.siphalor.amecs.api.KeyModifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

@Environment(EnvType.CLIENT)
public class ModifierPrefixTextProvider {
	private static final Text SUFFIX = Text.literal(" + ");
	private static final Text COMPRESSED_SUFFIX = Text.literal("+");
	private final String translationKey;

	public ModifierPrefixTextProvider(KeyModifier modifier) {
		this(modifier.getTranslationKey());
	}

	public ModifierPrefixTextProvider(String translationKey) {
		this.translationKey = translationKey;
	}

	protected MutableText getBaseText(Variation variation) {
		return MutableText.of(variation.getTranslatableText(translationKey));
	}

	public MutableText getText(Variation variation) {
		MutableText text = getBaseText(variation);
		if (variation == Variation.COMPRESSED) {
			text.append(COMPRESSED_SUFFIX);
		} else {
			text.append(SUFFIX);
		}
		return text;
	}

	public static enum Variation {
		COMPRESSED(".tiny"),
		TINY(".tiny"),
		SHORT(".short"),
		NORMAL("");

		// using this array for the values because it is faster than calling values() every time
		public static final Variation[] VALUES = Variation.values();

		public static final Variation WIDEST = NORMAL;
		public static final Variation SMALLEST = COMPRESSED;

		public final String translateKeySuffix;

		private Variation(String translateKeySuffix) {
			this.translateKeySuffix = translateKeySuffix;
		}

		public TranslatableTextContent getTranslatableText(String translationKey) {
			return new TranslatableTextContent(translationKey + translateKeySuffix);
		}

		public Variation getNextVariation(int amount) {
			int targetOrdinal = ordinal() + amount;
			if (targetOrdinal < 0 || targetOrdinal >= VALUES.length) {
				return null;
			}
			return VALUES[targetOrdinal];
		}

		public Variation getSmaller() {
			return getNextVariation(-1);
		}
	}
}
