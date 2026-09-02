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
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import techreborn.TechReborn;
import techreborn.init.ModFluids;
import techreborn.init.TRContent;

import java.util.concurrent.CompletableFuture;

/**
 * NeoForge port of Fabric {@code TRBlockTagProvider}.
 */
public final class TRBlockTagsProvider extends BlockTagsProvider {
	public TRBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, TechReborn.MOD_ID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(TRContent.BlockTags.DRILL_MINEABLE)
			.addOptionalTag(BlockTags.MINEABLE_WITH_PICKAXE)
			.addOptionalTag(BlockTags.MINEABLE_WITH_SHOVEL);

		tag(TRContent.BlockTags.JACKHAMMER_MINEABLE)
			.addOptionalTag(BlockTags.BASE_STONE_NETHER)
			.addOptionalTag(BlockTags.BASE_STONE_OVERWORLD)
			.addOptionalTag(BlockTags.DIRT)
			.addOptionalTag(BlockTags.ICE)
			.addOptionalTag(BlockTags.SNOW)
			.addOptionalTag(BlockTags.NYLIUM)
			.addOptionalTag(BlockTags.WART_BLOCKS)
			.addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "stone")))
			.add(Blocks.END_STONE)
			.add(Blocks.SAND)
			.add(Blocks.RED_SAND)
			.add(Blocks.SANDSTONE)
			.add(Blocks.RED_SANDSTONE)
			.add(Blocks.GRAVEL)
			.add(Blocks.CALCITE)
			.add(Blocks.SNOW)
			.add(Blocks.SOUL_SAND)
			.add(Blocks.SOUL_SOIL);

		tag(TRContent.BlockTags.OMNI_TOOL_MINEABLE)
			.addTag(TRContent.BlockTags.DRILL_MINEABLE)
			.addOptionalTag(BlockTags.MINEABLE_WITH_AXE);

		tag(BlockTags.MINEABLE_WITH_HOE).add(TRContent.RUBBER_LEAVES);

		for (TRContent.Ores ore : TRContent.Ores.values()) {
			tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ore.block);
			tag(Tags.Blocks.ORES).add(ore.block);
		}

		for (TRContent.StorageBlocks storage : TRContent.StorageBlocks.values()) {
			tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(storage.getBlock(), storage.getStairsBlock(), storage.getSlabBlock(), storage.getWallBlock());
			tag(BlockTags.SLABS).add(storage.getSlabBlock());
			tag(BlockTags.STAIRS).add(storage.getStairsBlock());
			tag(BlockTags.WALLS).add(storage.getWallBlock());
		}

		for (TRContent.MachineBlocks machine : TRContent.MachineBlocks.values()) {
			tag(BlockTags.MINEABLE_WITH_PICKAXE).add(machine.casing);
		}

		tag(BlockTags.FENCES)
			.add(TRContent.RUBBER_FENCE)
			.add(TRContent.REFINED_IRON_FENCE);

		tag(BlockTags.GUARDED_BY_PIGLINS).add(TRContent.StorageBlocks.ELECTRUM.getBlock());

		tag(BlockTags.LEAVES).add(TRContent.RUBBER_LEAVES);

		tag(TRContent.BlockTags.RUBBER_LOGS)
			.add(TRContent.RUBBER_LOG)
			.add(TRContent.RUBBER_LOG_STRIPPED)
			.add(TRContent.RUBBER_WOOD)
			.add(TRContent.STRIPPED_RUBBER_WOOD);

		tag(BlockTags.LOGS_THAT_BURN).addTag(TRContent.BlockTags.RUBBER_LOGS);
		tag(BlockTags.PLANKS).add(TRContent.RUBBER_PLANKS);
		tag(BlockTags.SAPLINGS).add(TRContent.RUBBER_SAPLING);
		tag(BlockTags.SLABS).add(TRContent.RUBBER_SLAB);
		tag(BlockTags.STAIRS).add(TRContent.RUBBER_STAIR);
		tag(BlockTags.WALLS).add(TRContent.COPPER_WALL);

		tag(BlockTags.WOODEN_BUTTONS).add(TRContent.RUBBER_BUTTON);
		tag(BlockTags.WOODEN_DOORS).add(TRContent.RUBBER_DOOR);
		tag(BlockTags.WOODEN_FENCES).add(TRContent.RUBBER_FENCE);
		tag(BlockTags.WOODEN_PRESSURE_PLATES).add(TRContent.RUBBER_PRESSURE_PLATE);
		tag(BlockTags.WOODEN_SLABS).add(TRContent.RUBBER_SLAB);
		tag(BlockTags.WOODEN_STAIRS).add(TRContent.RUBBER_STAIR);
		tag(BlockTags.WOODEN_TRAPDOORS).add(TRContent.RUBBER_TRAPDOOR);

		for (ModFluids fluid : ModFluids.values()) {
			Block block = fluid.getBlock();
			if (block != null) {
				tag(BlockTags.REPLACEABLE).add(block);
			}
		}

		tag(TRContent.BlockTags.NONE_SOLID_COVERS)
			.addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("ae2", "whitelisted/facades")))
			.addTag(Tags.Blocks.GLASS_BLOCKS);
	}
}
