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
		return variation.getTranslatableText(translationKey);
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

	public static enum Variation {
		COMPRESSED(".tiny"),
		TINY(".tiny"),
		SHORT(".short"),
		NORMAL("");

		public static final Variation WIDEST = NORMAL;

		public final String translateKeySuffix;

		private Variation(String translateKeySuffix) {
			this.translateKeySuffix = translateKeySuffix;
		}
		
		public TranslatableText getTranslatableText(String translationKey) {
			return new TranslatableText(translationKey + translateKeySuffix);
		}
		
		public Variation getSmaller(int amount) {
			return Variation.values()[Math.max(this.ordinal() - amount, 0)];
		}
		
		public Variation getSmaller() {
			return getSmaller(1);
		}
	}
}
