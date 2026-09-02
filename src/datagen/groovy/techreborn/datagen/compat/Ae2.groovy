/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.datagen.compat

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item

/**
 * AE2 item references for recipe datagen. Stubs are registered from main during
 * {@code RegisterEvent} when running datagen without AE2 (see ModRegistry).
 */
class Ae2 {
	static String AE2_MOD_ID = "ae2"
	static Identifier CERTUS_QUARTZ_DUST = Identifier.fromNamespaceAndPath(AE2_MOD_ID, "certus_quartz_dust")
	static Identifier CERTUS_QUARTZ_CRYSTAL = Identifier.fromNamespaceAndPath(AE2_MOD_ID, "certus_quartz_crystal")
	static Identifier FLUIX_CRYSTAL = Identifier.fromNamespaceAndPath(AE2_MOD_ID, "fluix_crystal")
	static Identifier FLUIX_DUST = Identifier.fromNamespaceAndPath(AE2_MOD_ID, "fluix_dust")

	static void setup() {
		// Registration happens in ModRegistry.registerAe2DatagenStubs during RegisterEvent.
	}

	static Item getCertusQuartzDust() {
		return getItem(CERTUS_QUARTZ_DUST)
	}

	static Item getCertusQuartzCrystal() {
		return getItem(CERTUS_QUARTZ_CRYSTAL)
	}

	static Item getFluixCrystal() {
		return getItem(FLUIX_CRYSTAL)
	}

	static Item getFluixDust() {
		return getItem(FLUIX_DUST)
	}

	private static Item getItem(Identifier name) {
		if (!BuiltInRegistries.ITEM.containsKey(name)) {
			throw new IllegalArgumentException("Item not found: " + name)
		}

		return BuiltInRegistries.ITEM.getValue(name)
	}
}
