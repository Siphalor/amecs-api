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

package de.siphalor.amecs.testmod;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.util.function.BooleanSupplier;

public class TestPriorityKeybinding extends AmecsKeyBinding implements PriorityKeyBinding {
	private final BooleanSupplier action;

	public TestPriorityKeybinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers, BooleanSupplier action) {
		super(id, type, code, category, defaultModifiers);
		this.action = action;
	}

	@Override
	public boolean onPressedPriority() {
		return action.getAsBoolean();
	}
}
