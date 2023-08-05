/*
 * Copyright 2020-2023 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.siphalor.amecs.impl;

import de.siphalor.amecs.api.KeyModifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ModifierPrefixTextProvider {
	private static final String SUFFIX = " + ";
	private static final String COMPRESSED_SUFFIX = "+";
	private static final Text SUFFIX_TEXT = new LiteralText(SUFFIX);
	private static final Text COMPRESSED_SUFFIX_TEXT = new LiteralText(COMPRESSED_SUFFIX);
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

	public String getTranslation(Variation variation) {
		String text = variation.getTranslation(translationKey);
		if (variation == Variation.COMPRESSED) {
			text += COMPRESSED_SUFFIX;
		} else {
			text += SUFFIX;
		}
		return text;
	}

	public enum Variation {
		COMPRESSED(".tiny"),
		TINY(".tiny"),
		SHORT(".short"),
		NORMAL("");

		// using this array for the values because it is faster than calling values() every time
		public static final Variation[] VALUES = Variation.values();

		public static final Variation WIDEST = NORMAL;
		public static final Variation SMALLEST = COMPRESSED;

		public final String translateKeySuffix;

		Variation(String translateKeySuffix) {
			this.translateKeySuffix = translateKeySuffix;
		}

		public TranslatableText getTranslatableText(String translationKey) {
			return new TranslatableText(translationKey + translateKeySuffix);
		}

		public String getTranslation(String translationKey) {
			return I18n.translate(translationKey + translateKeySuffix);
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
