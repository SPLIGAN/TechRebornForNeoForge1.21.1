/*

 * This file is part of TechReborn, licensed under the MIT License (MIT).

 *

 * Copyright (c) 2026 TechReborn

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



package techreborn.world;



import net.minecraft.core.Holder;

import net.minecraft.tags.BiomeTags;

import net.minecraft.world.level.biome.Biome;

import net.minecraft.world.level.biome.Biomes;



import java.util.function.Predicate;



/**

 * Loader-agnostic biome predicates for ore dimensions and rubber patches.

 * Runtime feature injection uses NeoForge biome modifiers ({@link techreborn.world.compat.neoforge.NeoForgeBiomeModifierPack}).

 */

public final class WorldgenBridge {

	private WorldgenBridge() {

	}



	public static BiomeSelector forestTaigaOrSwampSelector() {

		return new BiomeSelector(h ->

				h.is(BiomeTags.IS_FOREST) || h.is(BiomeTags.IS_TAIGA) || h.is(Biomes.SWAMP));

	}



	public static BiomeSelector foundInOverworld() {

		return new BiomeSelector(h -> h.is(BiomeTags.IS_OVERWORLD));

	}



	public static BiomeSelector foundInNether() {

		return new BiomeSelector(h -> h.is(BiomeTags.IS_NETHER));

	}



	public static BiomeSelector foundInEnd() {

		return new BiomeSelector(h -> h.is(BiomeTags.IS_END));

	}



	public record BiomeSelector(Predicate<Holder<Biome>> predicate) {

		public boolean test(Holder<Biome> biome) {

			return predicate.test(biome);

		}

	}

}

