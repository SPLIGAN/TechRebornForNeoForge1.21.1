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

package techreborn.init;

import techreborn.config.TechRebornConfig;
import techreborn.init.TRContent.Ingots;
import techreborn.init.TRContent.Parts;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import reborncore.common.event.EventBridge;

public class ModLoot {

	public static void init() {
		EventBridge.registerLootModify((key, table, source) -> {
			String stringId = key.location().toString();
			if (!stringId.startsWith("minecraft:gameplay") && !stringId.startsWith("minecraft:chests")) {
				return;
			}

			if (TechRebornConfig.enableOverworldLoot) {
				switch (stringId) {
					case "minecraft:chests/abandoned_mineshaft",
						"minecraft:chests/desert_pyramid",
						"minecraft:chests/igloo_chest",
						"minecraft:chests/jungle_temple",
						"minecraft:chests/simple_dungeon",
						"minecraft:chests/shipwreck_treasure",
						"minecraft:chests/underwater_ruin_small",
						"minecraft:chests/village/village_weaponsmith",
						"minecraft:chests/village/village_armorer",
						"minecraft:chests/village/village_toolsmith"
						-> addBasicTechRebornPool(table);
					case "minecraft:chests/stronghold_corridor",
						"minecraft:chests/stronghold_crossing",
						"minecraft:chests/stronghold_library",
						"minecraft:chests/underwater_ruin_big",
						"minecraft:chests/pillager_outpost"
						-> addAdvancedTechRebornPool(table);
					case "minecraft:chests/woodland_mansion",
						"minecraft:chests/ancient_city"
						-> addIndustrialTechRebornPool(table);
					case "minecraft:archeology/trail_ruins_common"
						-> table.addPool(LootPool.lootPool()
								.add(LootItem.lootTableItem(Parts.RUBBER))
								.setRolls(ConstantValue.exactly(1))
								.build());
					case "minecraft:gameplay/cat_morning_gift"
						-> table.addPool(LootPool.lootPool()
								.add(LootItem.lootTableItem(Parts.SCRAP).setWeight(5))
								.setRolls(ConstantValue.exactly(1))
								.build());
					default -> {
					}
				}
			}

			if (TechRebornConfig.enableNetherLoot) {
				if (stringId.equals("minecraft:chests/nether_bridge") ||
						stringId.equals("minecraft:chests/bastion_bridge") ||
						stringId.equals("minecraft:chests/bastion_hoglin_stable") ||
						stringId.equals("minecraft:chests/bastion_treasure") ||
						stringId.equals("minecraft:chests/bastion_other")) {
					addAdvancedTechRebornPool(table);
				}
			}

			if (TechRebornConfig.enableEndLoot) {
				if (stringId.equals("minecraft:chests/end_city_treasure")) {
					addIndustrialTechRebornPool(table);
				}
			}

			if (TechRebornConfig.enableFishingJunkLoot) {
				if (stringId.equals("minecraft:gameplay/fishing/junk")) {
					table.addPool(LootPool.lootPool()
							.add(LootItem.lootTableItem(Parts.RUBBER).setWeight(10))
							.add(LootItem.lootTableItem(TRContent.TREE_TAP).setWeight(10))
							.add(LootItem.lootTableItem(Parts.SCRAP).setWeight(10))
							.setRolls(ConstantValue.exactly(1))
							.build());
				}
			}
		});
	}

	private static void addBasicTechRebornPool(LootTable table) {
		table.addPool(LootPool.lootPool()
				.add(makeEntry(Items.COPPER_INGOT))
				.add(makeEntry(Ingots.TIN))
				.add(makeEntry(Ingots.LEAD))
				.add(makeEntry(Ingots.SILVER))
				.add(makeEntry(Ingots.REFINED_IRON))
				.add(makeEntry(Ingots.ADVANCED_ALLOY))
				.add(makeEntry(TRContent.MachineBlocks.BASIC.frame.asItem()))
				.add(makeEntry(Parts.ELECTRONIC_CIRCUIT))
				.add(makeEntry(TRContent.RUBBER_SAPLING, 25))
				.setRolls(UniformGenerator.between(1.0f, 2.0f))
				.build());
	}

	private static void addAdvancedTechRebornPool(LootTable table) {
		table.addPool(LootPool.lootPool()
				.add(makeEntry(Ingots.ALUMINUM))
				.add(makeEntry(Ingots.ELECTRUM))
				.add(makeEntry(Ingots.INVAR))
				.add(makeEntry(Ingots.NICKEL))
				.add(makeEntry(Ingots.STEEL))
				.add(makeEntry(Ingots.ZINC))
				.add(makeEntry(TRContent.MachineBlocks.ADVANCED.frame.asItem()))
				.add(makeEntry(Parts.ADVANCED_CIRCUIT))
				.add(makeEntry(Parts.DATA_STORAGE_CHIP))
				.setRolls(UniformGenerator.between(1.0f, 3.0f))
				.build());
	}

	private static void addIndustrialTechRebornPool(LootTable table) {
		table.addPool(LootPool.lootPool()
				.add(makeEntry(Ingots.CHROME))
				.add(makeEntry(Ingots.IRIDIUM))
				.add(makeEntry(Ingots.PLATINUM))
				.add(makeEntry(Ingots.TITANIUM))
				.add(makeEntry(Ingots.TUNGSTEN))
				.add(makeEntry(Ingots.TUNGSTENSTEEL))
				.add(makeEntry(TRContent.MachineBlocks.INDUSTRIAL.frame.asItem()))
				.add(makeEntry(Parts.INDUSTRIAL_CIRCUIT))
				.add(makeEntry(Parts.ENERGY_FLOW_CHIP))
				.setRolls(UniformGenerator.between(1.0f, 3.0f))
				.build());
	}

	private static LootPoolSingletonContainer.Builder<?> makeEntry(ItemLike item) {
		return makeEntry(item, 5);
	}

	private static LootPoolSingletonContainer.Builder<?> makeEntry(ItemLike item, int weight) {
		return LootItem.lootTableItem(item).setWeight(weight)
				.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)));
	}
}
