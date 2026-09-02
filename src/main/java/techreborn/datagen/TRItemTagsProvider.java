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
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import techreborn.TechReborn;
import techreborn.init.TRContent;

import java.util.concurrent.CompletableFuture;

/**
 * NeoForge port of Fabric {@code TRItemTagProvider} material / conventional item tags.
 * Generates the {@code c:} tags referenced by committed crafting recipes.
 */
public final class TRItemTagsProvider extends ItemTagsProvider {
	public TRItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, TechReborn.MOD_ID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		for (TRContent.Ores ore : TRContent.Ores.values()) {
			tag(ore.asTag()).add(ore.asItem());
			tag(TRContent.ItemTags.ORES).add(ore.asItem());
		}
		for (TRContent.StorageBlocks block : TRContent.StorageBlocks.values()) {
			tag(block.asTag()).add(block.asItem());
			tag(TRContent.ItemTags.STORAGE_BLOCK).add(block.asItem());
		}
		for (TRContent.Dusts dust : TRContent.Dusts.values()) {
			tag(dust.asTag()).add(dust.asItem());
			tag(TRContent.ItemTags.DUSTS).add(dust.asItem());
		}
		// Hand-authored Compatibility: c:dusts/coal includes charcoal dust as well (#3494).
		tag(cItem("dusts/coal")).add(TRContent.Dusts.CHARCOAL.asItem());

		for (TRContent.RawMetals raw : TRContent.RawMetals.values()) {
			tag(raw.asTag()).add(raw.asItem());
			tag(TRContent.ItemTags.RAW_METALS).add(raw.asItem());
		}
		for (TRContent.SmallDusts smallDust : TRContent.SmallDusts.values()) {
			tag(smallDust.asTag()).add(smallDust.asItem());
			tag(TRContent.ItemTags.SMALL_DUSTS).add(smallDust.asItem());
		}
		for (TRContent.Gems gem : TRContent.Gems.values()) {
			tag(gem.asTag()).add(gem.asItem());
			tag(TRContent.ItemTags.GEMS).add(gem.asItem());
		}
		for (TRContent.Ingots ingot : TRContent.Ingots.values()) {
			tag(ingot.asTag()).add(ingot.asItem());
			tag(TRContent.ItemTags.INGOTS).add(ingot.asItem());
		}
		tag(Tags.Items.INGOTS).addTag(TRContent.ItemTags.INGOTS);

		for (TRContent.Nuggets nugget : TRContent.Nuggets.values()) {
			tag(nugget.asTag()).add(nugget.asItem());
			tag(TRContent.ItemTags.NUGGETS).add(nugget.asItem());
		}
		for (TRContent.Plates plate : TRContent.Plates.values()) {
			tag(plate.asTag()).add(plate.asItem());
			tag(TRContent.ItemTags.PLATES).add(plate.asItem());
		}
		for (TRContent.StorageUnit unit : TRContent.StorageUnit.values()) {
			tag(TRContent.ItemTags.STORAGE_UNITS).add(unit.asItem());
		}

		tag(TRContent.ItemTags.RUBBER_LOGS)
			.add(TRContent.RUBBER_LOG.asItem())
			.add(TRContent.RUBBER_LOG_STRIPPED.asItem())
			.add(TRContent.RUBBER_WOOD.asItem())
			.add(TRContent.STRIPPED_RUBBER_WOOD.asItem());

		tag(TRContent.ItemTags.BRONZE_TOOL_MATERIALS).addTag(TRContent.Ingots.BRONZE.asTag());
		tag(TRContent.ItemTags.RUBY_TOOL_MATERIALS).addTag(TRContent.Gems.RUBY.asTag());
		tag(TRContent.ItemTags.SAPPHIRE_TOOL_MATERIALS).addTag(TRContent.Gems.SAPPHIRE.asTag());
		tag(TRContent.ItemTags.PERIDOT_TOOL_MATERIALS).addTag(TRContent.Gems.PERIDOT.asTag());

		tag(ItemTags.BEACON_PAYMENT_ITEMS).addTag(TRContent.ItemTags.INGOTS);
		tag(ItemTags.LEAVES).add(TRContent.RUBBER_LEAVES.asItem());
		tag(ItemTags.LOGS_THAT_BURN).addTag(TRContent.ItemTags.RUBBER_LOGS);
		tag(ItemTags.PLANKS).add(TRContent.RUBBER_PLANKS.asItem());
		tag(ItemTags.SAPLINGS).add(TRContent.RUBBER_SAPLING.asItem());
		tag(ItemTags.SLABS).add(TRContent.RUBBER_SLAB.asItem());
		tag(ItemTags.STAIRS).add(TRContent.RUBBER_STAIR.asItem());
		tag(ItemTags.FENCES)
			.add(TRContent.RUBBER_FENCE.asItem())
			.add(TRContent.REFINED_IRON_FENCE.asItem());
		tag(ItemTags.WOODEN_BUTTONS).add(TRContent.RUBBER_BUTTON.asItem());
		tag(ItemTags.WOODEN_DOORS).add(TRContent.RUBBER_DOOR.asItem());
		tag(ItemTags.WOODEN_FENCES).add(TRContent.RUBBER_FENCE.asItem());
		tag(ItemTags.WOODEN_PRESSURE_PLATES).add(TRContent.RUBBER_PRESSURE_PLATE.asItem());
		tag(ItemTags.WOODEN_SLABS).add(TRContent.RUBBER_SLAB.asItem());
		tag(ItemTags.WOODEN_STAIRS).add(TRContent.RUBBER_STAIR.asItem());
		tag(ItemTags.WOODEN_TRAPDOORS).add(TRContent.RUBBER_TRAPDOOR.asItem());

		for (TRContent.StorageBlocks block : TRContent.StorageBlocks.values()) {
			tag(ItemTags.SLABS).add(block.getSlabBlock().asItem());
			tag(ItemTags.STAIRS).add(block.getStairsBlock().asItem());
			tag(ItemTags.WALLS).add(block.getWallBlock().asItem());
		}
		tag(ItemTags.WALLS).add(TRContent.COPPER_WALL.asItem());
	}

	private static TagKey<Item> cItem(String path) {
		return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
	}
}
