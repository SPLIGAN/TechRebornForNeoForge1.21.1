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

package techreborn.events;

import net.minecraft.world.level.block.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import reborncore.RebornRegistry;
import reborncore.common.powerSystem.RcEnergyTier;
import techreborn.TechReborn;
import techreborn.blocks.misc.*;
import techreborn.config.TechRebornConfig;
import techreborn.init.*;
import techreborn.init.TRContent.*;
import techreborn.items.*;
import techreborn.items.armor.*;
import techreborn.items.tool.*;
import techreborn.items.tool.advanced.AdvancedJackhammerItem;
import techreborn.items.tool.basic.ElectricTreetapItem;
import techreborn.items.tool.basic.RockCutterItem;
import techreborn.items.tool.industrial.*;
import techreborn.items.tool.vanilla.*;
import techreborn.utils.InitUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * @author drcrazy
 */

public class ModRegistry {

	public static void registerNeoForge(RegisterEvent event) {
		ResourceKey<? extends Registry<?>> key = event.getRegistryKey();
		if (key.equals(NeoForgeRegistries.Keys.FLUID_TYPES)) {
			registerFluidTypesPhase(event);
		} else if (key.equals(Registries.FLUID)) {
			registerFluidsPhase();
		} else if (key.equals(Registries.BLOCK)) {
			Properties itemGroup = new Properties();
			registerBlocksBlockPhase(itemGroup);
			registerFluidBlocksPhase();
		} else if (key.equals(Registries.ITEM)) {
			Properties itemGroup = new Properties();
			registerBlocksItemPhase(itemGroup);
			registerFluidBucketsPhase();
			registerItems(itemGroup);
		} else if (key.equals(Registries.SOUND_EVENT)) {
			registerSoundsPhase(event);
		} else if (key.equals(Registries.POINT_OF_INTEREST_TYPE)) {
			TRVillager.registerPoiTypes(event);
		} else if (key.equals(Registries.VILLAGER_PROFESSION)) {
			TRVillager.registerProfessions(event);
		} else if (key.equals(Registries.CREATIVE_MODE_TAB)) {
			TRItemGroup.register();
		} else if (key.equals(Registries.RECIPE_SERIALIZER) || key.equals(Registries.RECIPE_TYPE)) {
			ModRecipes.register(event);
		} else if (key.equals(Registries.BLOCK_ENTITY_TYPE)) {
			TRBlockEntities.register(event);
		}
	}

	public static void finishCommonSetup() {
		registerApis();
		TRVillager.registerVillagerTrades();
		TRVillager.registerWanderingTraderTrades();
		TRVillager.registerVillagerHouses();
	}

	private static void registerBlocksBlockPhase(Properties itemGroup) {
		Arrays.stream(Ores.values()).forEach(value -> RebornRegistry.registerBlockOnly(value.block));
		StorageBlocks.blockStream().forEach(RebornRegistry::registerBlockOnly);
		Arrays.stream(MachineBlocks.values()).forEach(value -> {
			RebornRegistry.registerBlockOnly(value.frame);
			RebornRegistry.registerBlockOnly(value.casing);
		});
		Arrays.stream(SolarPanels.values()).forEach(value -> RebornRegistry.registerBlockOnly(value.block));
		Arrays.stream(StorageUnit.values()).forEach(value -> RebornRegistry.registerBlockOnly(value.block));
		Arrays.stream(TankUnit.values()).forEach(value -> RebornRegistry.registerBlockOnly(value.block));
		Arrays.stream(Cables.values()).forEach(value -> RebornRegistry.registerBlockOnly(value.block));
		Arrays.stream(Machine.values()).forEach(value -> RebornRegistry.registerBlockOnly(value.block));

		RebornRegistry.registerBlockOnly(TRContent.COMPUTER_CUBE = InitUtils.setup(new BlockComputerCube(), "computer_cube"));
		RebornRegistry.registerBlockOnly(TRContent.NUKE = InitUtils.setup(new BlockNuke(), "nuke"));
		RebornRegistry.registerBlockOnly(TRContent.REFINED_IRON_FENCE = InitUtils.setup(new BlockRefinedIronFence(), "refined_iron_fence"));
		RebornRegistry.registerBlockOnly(TRContent.REINFORCED_GLASS = InitUtils.setup(new BlockReinforcedGlass(), "reinforced_glass"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_LEAVES = InitUtils.setup(new BlockRubberLeaves(), "rubber_leaves"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_LOG = InitUtils.setup(new BlockRubberLog(), "rubber_log"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_LOG_STRIPPED = InitUtils.setup(new RotatedPillarBlock(TRBlockSettings.rubberLogStripped()), "rubber_log_stripped"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_WOOD = InitUtils.setup(new RotatedPillarBlock(TRBlockSettings.rubberWoodStripped()), "rubber_wood"));
		RebornRegistry.registerBlockOnly(TRContent.STRIPPED_RUBBER_WOOD = InitUtils.setup(new RotatedPillarBlock(TRBlockSettings.rubberWoodStripped()), "stripped_rubber_wood"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_PLANKS = InitUtils.setup(new BlockRubberPlank(), "rubber_planks"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_SAPLING = InitUtils.setup(new BlockRubberSapling(), "rubber_sapling"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_SLAB = InitUtils.setup(new SlabBlock(TRBlockSettings.rubberSlab()), "rubber_slab"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_FENCE = InitUtils.setup(new FenceBlock(TRBlockSettings.rubberFence()), "rubber_fence"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_FENCE_GATE = InitUtils.setup(new FenceGateBlock(TRContent.RUBBER_WOOD_TYPE, TRBlockSettings.rubberFenceGate()), "rubber_fence_gate"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_STAIR = InitUtils.setup(new BlockRubberPlankStair(), "rubber_stair"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_TRAPDOOR = InitUtils.setup(new RubberTrapdoorBlock(), "rubber_trapdoor"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_BUTTON = InitUtils.setup(new RubberButtonBlock(), "rubber_button"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_PRESSURE_PLATE = InitUtils.setup(new RubberPressurePlateBlock(), "rubber_pressure_plate"));
		RebornRegistry.registerBlockOnly(TRContent.RUBBER_DOOR = InitUtils.setup(new RubberDoorBlock(), "rubber_door"));
		RebornRegistry.registerBlockNoItem(TRContent.POTTED_RUBBER_SAPLING = InitUtils.setup(new FlowerPotBlock(TRContent.RUBBER_SAPLING, TRBlockSettings.pottedRubberSapling()), "potted_rubber_sapling"));
		RebornRegistry.registerBlockOnly(TRContent.COPPER_WALL = InitUtils.setup(new WallBlock(TRBlockSettings.copperWall()), "copper_wall"));

		TechReborn.LOGGER.debug("TechReborn's Blocks Loaded");
	}

	private static void registerBlocksItemPhase(Properties itemGroup) {
		Arrays.stream(Ores.values()).forEach(value -> RebornRegistry.registerBlockItem(value.block, itemGroup));
		StorageBlocks.blockStream().forEach(block -> RebornRegistry.registerBlockItem(block, itemGroup));
		Arrays.stream(MachineBlocks.values()).forEach(value -> {
			RebornRegistry.registerBlockItem(value.frame, itemGroup);
			RebornRegistry.registerBlockItem(value.casing, itemGroup);
		});
		Arrays.stream(SolarPanels.values()).forEach(value -> RebornRegistry.registerBlockItem(value.block, itemGroup));
		Arrays.stream(StorageUnit.values()).forEach(value -> RebornRegistry.registerBlockItem(value.block, itemGroup));
		Arrays.stream(TankUnit.values()).forEach(value -> RebornRegistry.registerBlockItem(value.block, itemGroup));
		Arrays.stream(Cables.values()).forEach(value -> RebornRegistry.registerBlockItem(value.block, itemGroup));
		Arrays.stream(Machine.values()).forEach(value -> RebornRegistry.registerBlockItem(value.block, itemGroup));

		RebornRegistry.registerBlockItem(TRContent.COMPUTER_CUBE, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.NUKE, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.REFINED_IRON_FENCE, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.REINFORCED_GLASS, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_LEAVES, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_LOG, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_LOG_STRIPPED, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_WOOD, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.STRIPPED_RUBBER_WOOD, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_PLANKS, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_SAPLING, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_SLAB, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_FENCE, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_FENCE_GATE, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_STAIR, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_TRAPDOOR, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_BUTTON, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_PRESSURE_PLATE, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.RUBBER_DOOR, itemGroup);
		RebornRegistry.registerBlockItem(TRContent.COPPER_WALL, itemGroup);
	}

	private static void registerFluidTypesPhase(RegisterEvent event) {
		Arrays.stream(ModFluids.values()).forEach(fluid -> fluid.registerFluidTypeOnly(event));
	}

	private static void registerFluidsPhase() {
		Arrays.stream(ModFluids.values()).forEach(ModFluids::registerFluidsOnly);
	}

	private static void registerFluidBlocksPhase() {
		Arrays.stream(ModFluids.values()).forEach(ModFluids::registerFluidBlockOnly);
	}

	private static void registerFluidBucketsPhase() {
		Arrays.stream(ModFluids.values()).forEach(ModFluids::registerFluidBucketOnly);
	}

	private static void registerSoundsPhase(RegisterEvent event) {
		registerSound(event, "alarm", v -> ModSounds.ALARM = v);
		registerSound(event, "alarm_2", v -> ModSounds.ALARM_2 = v);
		registerSound(event, "alarm_3", v -> ModSounds.ALARM_3 = v);
		registerSound(event, "auto_crafting", v -> ModSounds.AUTO_CRAFTING = v);
		registerSound(event, "block_dismantle", v -> ModSounds.BLOCK_DISMANTLE = v);
		registerSound(event, "cable_shock", v -> ModSounds.CABLE_SHOCK = v);
		registerSound(event, "machine_run", v -> ModSounds.MACHINE_RUN = v);
		registerSound(event, "machine_start", v -> ModSounds.MACHINE_START = v);
		registerSound(event, "sap_extract", v -> ModSounds.SAP_EXTRACT = v);
	}

	private static void registerSound(RegisterEvent event, String path, Consumer<SoundEvent> assign) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TechReborn.MOD_ID, path);
		event.register(Registries.SOUND_EVENT, id, () -> {
			SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(id);
			assign.accept(soundEvent);
			return soundEvent;
		});
	}

	private static void registerItems(Properties itemGroup) {
		Arrays.stream(StorageUnit.values()).map(StorageUnit::getUpgrader).filter(Optional::isPresent).forEach(value -> RebornRegistry.registerItem(value.get()));
		Arrays.stream(Ingots.values()).forEach(value -> RebornRegistry.registerItem(value.asItem()));
		Arrays.stream(Nuggets.values()).forEach(value -> RebornRegistry.registerItem(value.asItem()));
		Arrays.stream(Gems.values()).forEach(value -> RebornRegistry.registerItem(value.asItem()));
		Arrays.stream(Dusts.values()).forEach(value -> RebornRegistry.registerItem(value.asItem()));
		Arrays.stream(RawMetals.values()).forEach(value -> RebornRegistry.registerItem(value.asItem()));
		Arrays.stream(SmallDusts.values()).forEach(value -> RebornRegistry.registerItem(value.asItem()));
		Arrays.stream(Plates.values()).forEach(value -> RebornRegistry.registerItem(value.asItem()));
		Arrays.stream(Parts.values()).forEach(value -> RebornRegistry.registerItem(value.asItem()));
		Arrays.stream(Upgrades.values()).forEach(value -> RebornRegistry.registerItem(value.asItem()));

		RebornRegistry.registerItem(TRContent.QUANTUM_HELMET = InitUtils.setup(new QuantumSuitItem(TRArmorMaterials.QUANTUM, ArmorItem.Type.HELMET), "quantum_helmet"));
		RebornRegistry.registerItem(TRContent.QUANTUM_CHESTPLATE = InitUtils.setup(new QuantumSuitItem(TRArmorMaterials.QUANTUM, ArmorItem.Type.CHESTPLATE), "quantum_chestplate"));
		RebornRegistry.registerItem(TRContent.QUANTUM_LEGGINGS = InitUtils.setup(new QuantumSuitItem(TRArmorMaterials.QUANTUM, ArmorItem.Type.LEGGINGS), "quantum_leggings"));
		RebornRegistry.registerItem(TRContent.QUANTUM_BOOTS = InitUtils.setup(new QuantumSuitItem(TRArmorMaterials.QUANTUM, ArmorItem.Type.BOOTS), "quantum_boots"));

		RebornRegistry.registerItem(TRContent.NANO_HELMET = InitUtils.setup(new NanoSuitItem(TRArmorMaterials.NANO, ArmorItem.Type.HELMET), "nano_helmet"));
		RebornRegistry.registerItem(TRContent.NANO_CHESTPLATE = InitUtils.setup(new NanoSuitItem(TRArmorMaterials.NANO, ArmorItem.Type.CHESTPLATE), "nano_chestplate"));
		RebornRegistry.registerItem(TRContent.NANO_LEGGINGS = InitUtils.setup(new NanoSuitItem(TRArmorMaterials.NANO, ArmorItem.Type.LEGGINGS), "nano_leggings"));
		RebornRegistry.registerItem(TRContent.NANO_BOOTS = InitUtils.setup(new NanoSuitItem(TRArmorMaterials.NANO, ArmorItem.Type.BOOTS), "nano_boots"));

		// Gem armor & tools
		// Todo: repair with tags
		RebornRegistry.registerItem(TRContent.BRONZE_SWORD = InitUtils.setup(new TRSwordItem(TRToolTier.BRONZE), "bronze_sword"));
		RebornRegistry.registerItem(TRContent.BRONZE_PICKAXE = InitUtils.setup(new TRPickaxeItem(TRToolTier.BRONZE), "bronze_pickaxe"));
		RebornRegistry.registerItem(TRContent.BRONZE_SPADE = InitUtils.setup(new TRSpadeItem(TRToolTier.BRONZE), "bronze_spade"));
		RebornRegistry.registerItem(TRContent.BRONZE_AXE = InitUtils.setup(new TRAxeItem(TRToolTier.BRONZE), "bronze_axe"));
		RebornRegistry.registerItem(TRContent.BRONZE_HOE = InitUtils.setup(new TRHoeItem(TRToolTier.BRONZE), "bronze_hoe"));

		RebornRegistry.registerItem(TRContent.BRONZE_HELMET = InitUtils.setup(new ArmorItem(TRArmorMaterials.BRONZE, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(17))), "bronze_helmet"));
		RebornRegistry.registerItem(TRContent.BRONZE_CHESTPLATE = InitUtils.setup(new ArmorItem(TRArmorMaterials.BRONZE, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(17))), "bronze_chestplate"));
		RebornRegistry.registerItem(TRContent.BRONZE_LEGGINGS = InitUtils.setup(new ArmorItem(TRArmorMaterials.BRONZE, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(17))), "bronze_leggings"));
		RebornRegistry.registerItem(TRContent.BRONZE_BOOTS = InitUtils.setup(new ArmorItem(TRArmorMaterials.BRONZE, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(17))), "bronze_boots"));

		RebornRegistry.registerItem(TRContent.RUBY_SWORD = InitUtils.setup(new TRSwordItem(TRToolTier.RUBY), "ruby_sword"));
		RebornRegistry.registerItem(TRContent.RUBY_PICKAXE = InitUtils.setup(new TRPickaxeItem(TRToolTier.RUBY), "ruby_pickaxe"));
		RebornRegistry.registerItem(TRContent.RUBY_SPADE = InitUtils.setup(new TRSpadeItem(TRToolTier.RUBY), "ruby_spade"));
		RebornRegistry.registerItem(TRContent.RUBY_AXE = InitUtils.setup(new TRAxeItem(TRToolTier.RUBY), "ruby_axe"));
		RebornRegistry.registerItem(TRContent.RUBY_HOE = InitUtils.setup(new TRHoeItem(TRToolTier.RUBY), "ruby_hoe"));

		RebornRegistry.registerItem(TRContent.RUBY_HELMET = InitUtils.setup(new ArmorItem(TRArmorMaterials.RUBY, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(16)).stacksTo(1)), "ruby_helmet"));
		RebornRegistry.registerItem(TRContent.RUBY_CHESTPLATE = InitUtils.setup(new ArmorItem(TRArmorMaterials.RUBY, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(16)).stacksTo(1)), "ruby_chestplate"));
		RebornRegistry.registerItem(TRContent.RUBY_LEGGINGS = InitUtils.setup(new ArmorItem(TRArmorMaterials.RUBY, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(16)).stacksTo(1)), "ruby_leggings"));
		RebornRegistry.registerItem(TRContent.RUBY_BOOTS = InitUtils.setup(new ArmorItem(TRArmorMaterials.RUBY, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(16)).stacksTo(1)), "ruby_boots"));

		RebornRegistry.registerItem(TRContent.SAPPHIRE_SWORD = InitUtils.setup(new TRSwordItem(TRToolTier.SAPPHIRE), "sapphire_sword"));
		RebornRegistry.registerItem(TRContent.SAPPHIRE_PICKAXE = InitUtils.setup(new TRPickaxeItem(TRToolTier.SAPPHIRE), "sapphire_pickaxe"));
		RebornRegistry.registerItem(TRContent.SAPPHIRE_SPADE = InitUtils.setup(new TRSpadeItem(TRToolTier.SAPPHIRE), "sapphire_spade"));
		RebornRegistry.registerItem(TRContent.SAPPHIRE_AXE = InitUtils.setup(new TRAxeItem(TRToolTier.SAPPHIRE), "sapphire_axe"));
		RebornRegistry.registerItem(TRContent.SAPPHIRE_HOE = InitUtils.setup(new TRHoeItem(TRToolTier.SAPPHIRE), "sapphire_hoe"));

		RebornRegistry.registerItem(TRContent.SAPPHIRE_HELMET = InitUtils.setup(new ArmorItem(TRArmorMaterials.SAPPHIRE, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(19)).stacksTo(1)), "sapphire_helmet"));
		RebornRegistry.registerItem(TRContent.SAPPHIRE_CHESTPLATE = InitUtils.setup(new ArmorItem(TRArmorMaterials.SAPPHIRE, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(19)).stacksTo(1)), "sapphire_chestplate"));
		RebornRegistry.registerItem(TRContent.SAPPHIRE_LEGGINGS = InitUtils.setup(new ArmorItem(TRArmorMaterials.SAPPHIRE, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(19)).stacksTo(1)), "sapphire_leggings"));
		RebornRegistry.registerItem(TRContent.SAPPHIRE_BOOTS = InitUtils.setup(new ArmorItem(TRArmorMaterials.SAPPHIRE, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(19)).stacksTo(1)), "sapphire_boots"));

		RebornRegistry.registerItem(TRContent.PERIDOT_SWORD = InitUtils.setup(new TRSwordItem(TRToolTier.PERIDOT), "peridot_sword"));
		RebornRegistry.registerItem(TRContent.PERIDOT_PICKAXE = InitUtils.setup(new TRPickaxeItem(TRToolTier.PERIDOT), "peridot_pickaxe"));
		RebornRegistry.registerItem(TRContent.PERIDOT_SPADE = InitUtils.setup(new TRSpadeItem(TRToolTier.PERIDOT), "peridot_spade"));
		RebornRegistry.registerItem(TRContent.PERIDOT_AXE = InitUtils.setup(new TRAxeItem(TRToolTier.PERIDOT), "peridot_axe"));
		RebornRegistry.registerItem(TRContent.PERIDOT_HOE = InitUtils.setup(new TRHoeItem(TRToolTier.PERIDOT), "peridot_hoe"));

		RebornRegistry.registerItem(TRContent.PERIDOT_HELMET = InitUtils.setup(new ArmorItem(TRArmorMaterials.PERIDOT, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(17)).stacksTo(1)), "peridot_helmet"));
		RebornRegistry.registerItem(TRContent.PERIDOT_CHESTPLATE = InitUtils.setup(new ArmorItem(TRArmorMaterials.PERIDOT, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(17)).stacksTo(1)), "peridot_chestplate"));
		RebornRegistry.registerItem(TRContent.PERIDOT_LEGGINGS = InitUtils.setup(new ArmorItem(TRArmorMaterials.PERIDOT, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(17)).stacksTo(1)), "peridot_leggings"));
		RebornRegistry.registerItem(TRContent.PERIDOT_BOOTS = InitUtils.setup(new ArmorItem(TRArmorMaterials.PERIDOT, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(17)).stacksTo(1)), "peridot_boots"));

		RebornRegistry.registerItem(TRContent.SILVER_HELMET = InitUtils.setup(new ArmorItem(TRArmorMaterials.SILVER, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(14)).stacksTo(1)), "silver_helmet"));
		RebornRegistry.registerItem(TRContent.SILVER_CHESTPLATE = InitUtils.setup(new ArmorItem(TRArmorMaterials.SILVER, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(14)).stacksTo(1)), "silver_chestplate"));
		RebornRegistry.registerItem(TRContent.SILVER_LEGGINGS = InitUtils.setup(new ArmorItem(TRArmorMaterials.SILVER, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(14)).stacksTo(1)), "silver_leggings"));
		RebornRegistry.registerItem(TRContent.SILVER_BOOTS = InitUtils.setup(new ArmorItem(TRArmorMaterials.SILVER, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(14)).stacksTo(1)), "silver_boots"));

		RebornRegistry.registerItem(TRContent.STEEL_HELMET = InitUtils.setup(new ArmorItem(TRArmorMaterials.STEEL, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(24)).stacksTo(1)), "steel_helmet"));
		RebornRegistry.registerItem(TRContent.STEEL_CHESTPLATE = InitUtils.setup(new ArmorItem(TRArmorMaterials.STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(24)).stacksTo(1)), "steel_chestplate"));
		RebornRegistry.registerItem(TRContent.STEEL_LEGGINGS = InitUtils.setup(new ArmorItem(TRArmorMaterials.STEEL, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(24)).stacksTo(1)), "steel_leggings"));
		RebornRegistry.registerItem(TRContent.STEEL_BOOTS = InitUtils.setup(new ArmorItem(TRArmorMaterials.STEEL, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(24)).stacksTo(1)), "steel_boots"));

		// Battery
		RebornRegistry.registerItem(TRContent.RED_CELL_BATTERY = InitUtils.setup(new BatteryItem(TechRebornConfig.redCellBatteryMaxCharge, RcEnergyTier.LOW), "red_cell_battery"));
		RebornRegistry.registerItem(TRContent.LITHIUM_ION_BATTERY = InitUtils.setup(new BatteryItem(TechRebornConfig.lithiumIonBatteryMaxCharge, RcEnergyTier.MEDIUM), "lithium_ion_battery"));
		RebornRegistry.registerItem(TRContent.LITHIUM_ION_BATPACK = InitUtils.setup(new BatpackItem(TechRebornConfig.lithiumBatpackCharge, TRArmorMaterials.LITHIUM_BATPACK, RcEnergyTier.MEDIUM), "lithium_ion_batpack"));
		RebornRegistry.registerItem(TRContent.ENERGY_CRYSTAL = InitUtils.setup(new BatteryItem(TechRebornConfig.energyCrystalMaxCharge, RcEnergyTier.HIGH), "energy_crystal"));
		RebornRegistry.registerItem(TRContent.LAPOTRON_CRYSTAL = InitUtils.setup(new BatteryItem(TechRebornConfig.lapotronCrystalMaxCharge, RcEnergyTier.EXTREME), "lapotron_crystal"));
		RebornRegistry.registerItem(TRContent.LAPOTRONIC_ORB = InitUtils.setup(new BatteryItem(TechRebornConfig.lapotronicOrbMaxCharge, RcEnergyTier.INSANE), "lapotronic_orb"));
		RebornRegistry.registerItem(TRContent.LAPOTRONIC_ORBPACK = InitUtils.setup(new BatpackItem(TechRebornConfig.lapotronPackCharge, TRArmorMaterials.LAPOTRONIC_ORBPACK, RcEnergyTier.INSANE), "lapotronic_orbpack"));

		// Tools
		RebornRegistry.registerItem(TRContent.TREE_TAP = InitUtils.setup(new TreeTapItem(), "treetap"));
		RebornRegistry.registerItem(TRContent.WRENCH = InitUtils.setup(new WrenchItem(), "wrench"));
		RebornRegistry.registerItem(TRContent.PAINTING_TOOL = InitUtils.setup(new PaintingToolItem(), "painting_tool"));

		RebornRegistry.registerItem(TRContent.BASIC_DRILL = InitUtils.setup(new DrillItem(TRToolMaterials.BASIC_DRILL, TechRebornConfig.basicDrillCharge, RcEnergyTier.MEDIUM, TechRebornConfig.basicDrillCost, 8F), "basic_drill"));
		RebornRegistry.registerItem(TRContent.BASIC_CHAINSAW = InitUtils.setup(new ChainsawItem(TRToolMaterials.BASIC_CHAINSAW, TechRebornConfig.basicChainsawCharge, RcEnergyTier.MEDIUM, TechRebornConfig.basicChainsawCost, 8F), "basic_chainsaw"));
		RebornRegistry.registerItem(TRContent.BASIC_JACKHAMMER = InitUtils.setup(new JackhammerItem(TRToolMaterials.BASIC_JACKHAMMER, TechRebornConfig.basicJackhammerCharge, RcEnergyTier.MEDIUM, TechRebornConfig.basicJackhammerCost), "basic_jackhammer"));
		RebornRegistry.registerItem(TRContent.ELECTRIC_TREE_TAP = InitUtils.setup(new ElectricTreetapItem(), "electric_treetap"));

		RebornRegistry.registerItem(TRContent.ADVANCED_DRILL = InitUtils.setup(new DrillItem(TRToolMaterials.ADVANCED_DRILL, TechRebornConfig.advancedDrillCharge, RcEnergyTier.EXTREME, TechRebornConfig.advancedDrillCost, 12F), "advanced_drill"));
		RebornRegistry.registerItem(TRContent.ADVANCED_CHAINSAW = InitUtils.setup(new ChainsawItem(TRToolMaterials.ADVANCED_CHAINSAW, TechRebornConfig.advancedChainsawCharge, RcEnergyTier.EXTREME, TechRebornConfig.advancedChainsawCost, 12F), "advanced_chainsaw"));
		RebornRegistry.registerItem(TRContent.ADVANCED_JACKHAMMER = InitUtils.setup(new AdvancedJackhammerItem(), "advanced_jackhammer"));
		RebornRegistry.registerItem(TRContent.ROCK_CUTTER = InitUtils.setup(new RockCutterItem(), "rock_cutter"));

		RebornRegistry.registerItem(TRContent.INDUSTRIAL_DRILL = InitUtils.setup(new IndustrialDrillItem(), "industrial_drill"));
		RebornRegistry.registerItem(TRContent.INDUSTRIAL_CHAINSAW = InitUtils.setup(new IndustrialChainsawItem(), "industrial_chainsaw"));
		RebornRegistry.registerItem(TRContent.INDUSTRIAL_JACKHAMMER = InitUtils.setup(new IndustrialJackhammerItem(), "industrial_jackhammer"));
		RebornRegistry.registerItem(TRContent.NANOSABER = InitUtils.setup(new NanosaberItem(), "nanosaber"));
		RebornRegistry.registerItem(TRContent.OMNI_TOOL = InitUtils.setup(new OmniToolItem(), "omni_tool"));

		// Armor
		RebornRegistry.registerItem(TRContent.CLOAKING_DEVICE = InitUtils.setup(new CloakingDeviceItem(), "cloaking_device"));

		// Other
		RebornRegistry.registerItem(TRContent.GPS = InitUtils.setup(new GpsItem(), "gps"));
		RebornRegistry.registerItem(TRContent.FREQUENCY_TRANSMITTER = InitUtils.setup(new FrequencyTransmitterItem(), "frequency_transmitter"));
		RebornRegistry.registerItem(TRContent.SCRAP_BOX = InitUtils.setup(new ScrapBoxItem(), "scrap_box"));
		RebornRegistry.registerItem(TRContent.MANUAL = InitUtils.setup(new ManualItem(), "manual"));
		RebornRegistry.registerItem(TRContent.DEBUG_TOOL = InitUtils.setup(new DebugToolItem(), "debug_tool"));
		RebornRegistry.registerItem(TRContent.CELL = InitUtils.setup(new DynamicCellItem(), "cell"));
		TRContent.CELL.registerFluidApi();

		TechReborn.LOGGER.debug("TechReborn's Items Loaded");
	}

	private static void registerApis() {
		// Fluid/item/energy capabilities: NeoForge RegisterCapabilitiesEvent (TechRebornCapabilities).
	}
}
