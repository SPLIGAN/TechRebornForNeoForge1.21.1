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

import techreborn.init.TRContent;

import java.util.Arrays;
import java.util.List;

/** Lists of {@link TROreFeatureConfig} derived from {@link TRContent.Ores} (helpers live here instead of on the {@code record}). */
public final class TROreFeatureQueries {
	private TROreFeatureQueries() {
	}

	/** Every ore entry that owns worldgen distribution data (datagen registers configured/placed features for all of these). */
	public static List<TROreFeatureConfig> allWithDistribution() {
		return Arrays.stream(TRContent.Ores.values())
				.filter(o -> o.distribution != null)
				.map(TROreFeatureConfig::of)
				.toList();
	}

	/** Subset respected by biome modifiers / runtime spawning ({@link OreDistribution#isGenerating()}). */
	public static List<TROreFeatureConfig> spawnEnabledByConfig() {
		return Arrays.stream(TRContent.Ores.values())
				.filter(o -> o.distribution != null)
				.filter(o -> o.distribution.isGenerating())
				.map(TROreFeatureConfig::of)
				.toList();
	}
}
