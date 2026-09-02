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

package techreborn.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import techreborn.TechReborn;
import techreborn.init.TRVillager;

import java.util.concurrent.CompletableFuture;

/**
 * NeoForge port of Fabric {@code TRPointOfInterestTagProvider}.
 */
public final class TRPointOfInterestTagsProvider extends KeyTagProvider<PoiType> {
	public TRPointOfInterestTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, Registries.POINT_OF_INTEREST_TYPE, lookupProvider, TechReborn.MOD_ID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(PoiTypeTags.ACQUIRABLE_JOB_SITE)
			.addOptional(ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, TRVillager.METALLURGIST_ID))
			.addOptional(ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, TRVillager.ELECTRICIAN_ID));
	}
}
