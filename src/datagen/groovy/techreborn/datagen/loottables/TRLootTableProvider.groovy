/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2022 TechReborn
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

package techreborn.datagen.loottables

import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator
import techreborn.init.TRContent

import java.util.concurrent.CompletableFuture
import java.util.function.Function

class TRLootTableProvider extends LootTableProvider {
	TRLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, Set.of(), List.of(
			new LootTableProvider.SubProviderEntry(BlockLootTables::new, LootContextParamSets.BLOCK)
		), registries)
	}

	static class BlockLootTables extends BlockLootSubProvider {
		private final List<Block> knownBlocks = new ArrayList<>()

		BlockLootTables(HolderLookup.Provider registries) {
			super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries)
		}

		@Override
		protected Iterable<Block> getKnownBlocks() {
			return knownBlocks
		}

		@Override
		void generate() {
			TRContent.StorageBlocks.values().each {
				dropSelfTracked(it.getBlock())
				dropSelfTracked(it.getSlabBlock())
				dropSelfTracked(it.getStairsBlock())
				dropSelfTracked(it.getWallBlock())
			}
			TRContent.Cables.values().each {
				dropSelfTracked(it.block)
			}
			TRContent.Machine.values().each {
				dropSelfTracked(it.block)
			}
			TRContent.SolarPanels.values().each {
				dropSelfTracked(it.block)
			}
			TRContent.StorageUnit.values().each {
				dropSelfTracked(it.block)
			}
			TRContent.TankUnit.values().each {
				dropSelfTracked(it.block)
			}
			TRContent.MachineBlocks.values().each {
				dropSelfTracked(it.getFrame())
				dropSelfTracked(it.getCasing())
			}
			dropSelfTracked(TRContent.RUBBER_BUTTON)
			addTracked(TRContent.RUBBER_DOOR, createDoorTable(TRContent.RUBBER_DOOR))
			dropSelfTracked(TRContent.RUBBER_FENCE)
			dropSelfTracked(TRContent.RUBBER_FENCE_GATE)
			dropSelfTracked(TRContent.RUBBER_LOG)
			dropSelfTracked(TRContent.RUBBER_LOG_STRIPPED)
			dropSelfTracked(TRContent.RUBBER_PLANKS)
			dropSelfTracked(TRContent.RUBBER_PRESSURE_PLATE)
			dropSelfTracked(TRContent.RUBBER_SAPLING)
			dropSelfTracked(TRContent.RUBBER_SLAB)
			dropSelfTracked(TRContent.RUBBER_STAIR)
			dropSelfTracked(TRContent.RUBBER_TRAPDOOR)
			dropSelfTracked(TRContent.RUBBER_WOOD)
			dropSelfTracked(TRContent.STRIPPED_RUBBER_WOOD)
			addTracked(TRContent.RUBBER_LEAVES, createLeavesDrops(
				TRContent.RUBBER_LEAVES,
				TRContent.RUBBER_SAPLING,
				NORMAL_LEAVES_SAPLING_CHANCES)
			)
			addTracked(TRContent.POTTED_RUBBER_SAPLING, createPotFlowerItemTable(TRContent.RUBBER_SAPLING))
			dropSelfTracked(TRContent.NUKE)
			dropSelfTracked(TRContent.REFINED_IRON_FENCE)
			dropSelfTracked(TRContent.REINFORCED_GLASS)
			dropSelfTracked(TRContent.COMPUTER_CUBE)
			dropSelfTracked(TRContent.COPPER_WALL)

			addOreDrop(TRContent.Ores.BAUXITE)
			addOreDrop(TRContent.Ores.GALENA)
			addOreDrop(TRContent.Ores.SHELDONITE)
			addOreDrop(TRContent.Ores.IRIDIUM, block -> createOreDrop(block, TRContent.RawMetals.IRIDIUM.asItem()))
			addOreDrop(TRContent.Ores.LEAD, block -> createOreDrop(block, TRContent.RawMetals.LEAD.asItem()))
			addOreDrop(TRContent.Ores.SILVER, block -> createOreDrop(block, TRContent.RawMetals.SILVER.asItem()))
			addOreDrop(TRContent.Ores.TIN, block -> createOreDrop(block, TRContent.RawMetals.TIN.asItem()))
			addOreDrop(TRContent.Ores.TUNGSTEN, block -> createOreDrop(block, TRContent.RawMetals.TUNGSTEN.asItem()))
			addOreDrop(TRContent.Ores.URANIUM, block -> createOreDrop(block, TRContent.RawMetals.URANIUM.asItem()))
			addOreDrop(TRContent.Ores.CINNABAR, this::cinnabarOreDrops)
			addOreDrop(TRContent.Ores.RUBY, this::rubyOreDrops)
			addOreDrop(TRContent.Ores.SAPPHIRE, this::sapphireOreDrops)
			addOreDrop(TRContent.Ores.SODALITE, this::sodaliteOreDrops)
			addOreDrop(TRContent.Ores.SPHALERITE, this::sphaleriteOreDrops)
			addOreDrop(TRContent.Ores.PYRITE, block -> createOreDrop(block, TRContent.Dusts.PYRITE.asItem()))
			addOreDrop(TRContent.Ores.PERIDOT, block -> createSingleItemTableWithSilkTouch(block, TRContent.Gems.PERIDOT.asItem(), UniformGenerator.between(1.0F, 2.0F)))
		}

		private void dropSelfTracked(Block block) {
			knownBlocks.add(block)
			dropSelf(block)
		}

		private void addTracked(Block block, LootTable.Builder builder) {
			knownBlocks.add(block)
			add(block, builder)
		}

		private void addOreDrop(TRContent.Ores ore) {
			dropSelfTracked(ore.block)
			def deepslate = ore.getDeepslate()
			if (deepslate != null) {
				dropSelfTracked(deepslate.block)
			}
		}

		private void addOreDrop(TRContent.Ores ore, Function<Block, LootTable.Builder> lootTableFunction) {
			addTracked(ore.block, lootTableFunction.apply(ore.block))
			def deepslate = ore.getDeepslate()
			if (deepslate != null) {
				addTracked(deepslate.block, lootTableFunction.apply(deepslate.block))
			}
		}

		private LootTable.Builder cinnabarOreDrops(Block drop) {
			HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT)
			return this.applyExplosionDecay(
				drop,
				LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(LootItem.lootTableItem(TRContent.Dusts.CINNABAR.asItem()))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(UniformGenerator.between(0.0F, 1.0F))
							.add(LootItem.lootTableItem(Items.REDSTONE))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(LootItem.lootTableItem(drop))
							.when(this.hasSilkTouch())
					)
			)
		}

		private LootTable.Builder rubyOreDrops(Block drop) {
			HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT)
			return this.applyExplosionDecay(
				drop,
				LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(UniformGenerator.between(1.0F, 2.0F))
							.add(LootItem.lootTableItem(TRContent.Gems.RUBY.asItem()))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(UniformGenerator.between(0.0F, 1.0F))
							.add(LootItem.lootTableItem(TRContent.Gems.RED_GARNET.asItem()))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(LootItem.lootTableItem(drop))
							.when(this.hasSilkTouch())
					)
			)
		}

		private LootTable.Builder sapphireOreDrops(Block drop) {
			HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT)
			return this.applyExplosionDecay(
				drop,
				LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(UniformGenerator.between(1.0F, 2.0F))
							.add(LootItem.lootTableItem(TRContent.Gems.SAPPHIRE.asItem()))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(UniformGenerator.between(0.0F, 1.0F))
							.add(LootItem.lootTableItem(TRContent.Gems.PERIDOT.asItem()))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(LootItem.lootTableItem(drop))
							.when(this.hasSilkTouch())
					)
			)
		}

		private LootTable.Builder sodaliteOreDrops(Block drop) {
			HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT)
			return this.applyExplosionDecay(
				drop,
				LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(LootItem.lootTableItem(TRContent.Dusts.SODALITE.asItem()))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(UniformGenerator.between(0.0F, 1.0F))
							.add(LootItem.lootTableItem(TRContent.Dusts.ALUMINUM.asItem()))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(LootItem.lootTableItem(drop))
							.when(this.hasSilkTouch())
					)
			)
		}

		private LootTable.Builder sphaleriteOreDrops(Block drop) {
			HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT)
			return this.applyExplosionDecay(
				drop,
				LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(LootItem.lootTableItem(TRContent.Dusts.SPHALERITE.asItem()))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(UniformGenerator.between(0.0F, 1.0F))
							.add(LootItem.lootTableItem(TRContent.Gems.YELLOW_GARNET.asItem()))
							.apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
							.when(this.hasSilkTouch().invert())
					)
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1.0F))
							.add(LootItem.lootTableItem(drop))
							.when(this.hasSilkTouch())
					)
			)
		}
	}
}
