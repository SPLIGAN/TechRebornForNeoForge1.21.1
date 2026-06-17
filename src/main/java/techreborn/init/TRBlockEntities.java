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

import org.apache.commons.lang3.Validate;
import techreborn.TechReborn;
import techreborn.blockentity.cable.CableBlockEntity;
import techreborn.blockentity.generator.LightningRodBlockEntity;
import techreborn.blockentity.generator.PlasmaGeneratorBlockEntity;
import techreborn.blockentity.generator.SolarPanelBlockEntity;
import techreborn.blockentity.generator.advanced.*;
import techreborn.blockentity.generator.basic.SolidFuelGeneratorBlockEntity;
import techreborn.blockentity.generator.basic.WaterMillBlockEntity;
import techreborn.blockentity.generator.basic.WindMillBlockEntity;
import techreborn.blockentity.generator.nuclear.NuclearReactorBlockEntity;
import techreborn.blockentity.generator.nuclear.ReactorChamberBlockEntity;
import techreborn.blockentity.lighting.LampBlockEntity;
import techreborn.blockentity.machine.iron.IronAlloyFurnaceBlockEntity;
import techreborn.blockentity.machine.iron.IronFurnaceBlockEntity;
import techreborn.blockentity.machine.misc.AlarmBlockEntity;
import techreborn.blockentity.machine.misc.ChargeOMatBlockEntity;
import techreborn.blockentity.machine.misc.DrainBlockEntity;
import techreborn.blockentity.machine.tier2.PumpBlockEntity;
import techreborn.blockentity.machine.multiblock.*;
import techreborn.blockentity.machine.multiblock.casing.MachineCasingBlockEntity;
import techreborn.blockentity.machine.tier0.block.BlockBreakerBlockEntity;
import techreborn.blockentity.machine.tier0.block.BlockPlacerBlockEntity;
import techreborn.blockentity.machine.tier1.*;
import techreborn.blockentity.machine.tier2.FishingStationBlockEntity;
import techreborn.blockentity.machine.tier2.LaunchpadBlockEntity;
import techreborn.blockentity.machine.tier3.ChunkLoaderBlockEntity;
import techreborn.blockentity.machine.tier3.IndustrialCentrifugeBlockEntity;
import techreborn.blockentity.machine.tier3.MatterFabricatorBlockEntity;
import techreborn.blockentity.storage.energy.AdjustableSUBlockEntity;
import techreborn.blockentity.storage.energy.HighVoltageSUBlockEntity;
import techreborn.blockentity.storage.energy.LowVoltageSUBlockEntity;
import techreborn.blockentity.storage.energy.MediumVoltageSUBlockEntity;
import techreborn.blockentity.storage.energy.idsu.InterdimensionalSUBlockEntity;
import techreborn.blockentity.storage.energy.lesu.LSUStorageBlockEntity;
import techreborn.blockentity.storage.energy.lesu.LapotronicSUBlockEntity;
import techreborn.blockentity.storage.fluid.TankUnitBaseBlockEntity;
import techreborn.blockentity.storage.item.StorageUnitBaseBlockEntity;
import techreborn.blockentity.transformers.EVTransformerBlockEntity;
import techreborn.blockentity.transformers.HVTransformerBlockEntity;
import techreborn.blockentity.transformers.LVTransformerBlockEntity;
import techreborn.blockentity.transformers.MVTransformerBlockEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.RegisterEvent;

public class TRBlockEntities {
	private static final List<BlockEntityType<?>> TYPES = new ArrayList<>();
	private static volatile boolean prepared;

	public static List<BlockEntityType<?>> allRegisteredTypes() {
		return List.copyOf(TYPES);
	}

	public static BlockEntityType<StorageUnitBaseBlockEntity> STORAGE_UNIT;
	public static BlockEntityType<TankUnitBaseBlockEntity> TANK_UNIT;
	public static BlockEntityType<DrainBlockEntity> DRAIN;
	public static BlockEntityType<ThermalGeneratorBlockEntity> THERMAL_GEN;
	public static BlockEntityType<IndustrialCentrifugeBlockEntity> INDUSTRIAL_CENTRIFUGE;
	public static BlockEntityType<RollingMachineBlockEntity> ROLLING_MACHINE;
	public static BlockEntityType<IndustrialBlastFurnaceBlockEntity> INDUSTRIAL_BLAST_FURNACE;
	public static BlockEntityType<AlloySmelterBlockEntity> ALLOY_SMELTER;
	public static BlockEntityType<IndustrialGrinderBlockEntity> INDUSTRIAL_GRINDER;
	public static BlockEntityType<ImplosionCompressorBlockEntity> IMPLOSION_COMPRESSOR;
	public static BlockEntityType<MatterFabricatorBlockEntity> MATTER_FABRICATOR;
	public static BlockEntityType<ChunkLoaderBlockEntity> CHUNK_LOADER;
	public static BlockEntityType<ChargeOMatBlockEntity> CHARGE_O_MAT;
	public static BlockEntityType<PlayerDetectorBlockEntity> PLAYER_DETECTOR;
	public static BlockEntityType<CableBlockEntity> CABLE;
	public static BlockEntityType<MachineCasingBlockEntity> MACHINE_CASINGS;
	public static BlockEntityType<DragonEggSyphonBlockEntity> DRAGON_EGG_SYPHON;
	public static BlockEntityType<AssemblingMachineBlockEntity> ASSEMBLY_MACHINE;
	public static BlockEntityType<DieselGeneratorBlockEntity> DIESEL_GENERATOR;
	public static BlockEntityType<IndustrialElectrolyzerBlockEntity> INDUSTRIAL_ELECTROLYZER;
	public static BlockEntityType<SemiFluidGeneratorBlockEntity> SEMI_FLUID_GENERATOR;
	public static BlockEntityType<GasTurbineBlockEntity> GAS_TURBINE;
	public static BlockEntityType<IronAlloyFurnaceBlockEntity> IRON_ALLOY_FURNACE;
	public static BlockEntityType<ChemicalReactorBlockEntity> CHEMICAL_REACTOR;
	public static BlockEntityType<InterdimensionalSUBlockEntity> INTERDIMENSIONAL_SU;
	public static BlockEntityType<AdjustableSUBlockEntity> ADJUSTABLE_SU;
	public static BlockEntityType<LapotronicSUBlockEntity> LAPOTRONIC_SU;
	public static BlockEntityType<LSUStorageBlockEntity> LSU_STORAGE;
	public static BlockEntityType<DistillationTowerBlockEntity> DISTILLATION_TOWER;
	public static BlockEntityType<VacuumFreezerBlockEntity> VACUUM_FREEZER;
	public static BlockEntityType<FusionControlComputerBlockEntity> FUSION_CONTROL_COMPUTER;
	public static BlockEntityType<LightningRodBlockEntity> LIGHTNING_ROD;
	public static BlockEntityType<IndustrialSawmillBlockEntity> INDUSTRIAL_SAWMILL;
	public static BlockEntityType<GrinderBlockEntity> GRINDER;
	public static BlockEntityType<SolidFuelGeneratorBlockEntity> SOLID_FUEL_GENERATOR;
	public static BlockEntityType<ExtractorBlockEntity> EXTRACTOR;
	public static BlockEntityType<ResinBasinBlockEntity> RESIN_BASIN;
	public static BlockEntityType<CompressorBlockEntity> COMPRESSOR;
	public static BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE;
	public static BlockEntityType<SolarPanelBlockEntity> SOLAR_PANEL;
	public static BlockEntityType<WaterMillBlockEntity> WATER_MILL;
	public static BlockEntityType<WindMillBlockEntity> WIND_MILL;
	public static BlockEntityType<NuclearReactorBlockEntity> NUCLEAR_REACTOR;
	public static BlockEntityType<ReactorChamberBlockEntity> REACTOR_CHAMBER;
	public static BlockEntityType<RecyclerBlockEntity> RECYCLER;
	public static BlockEntityType<LowVoltageSUBlockEntity> LOW_VOLTAGE_SU;
	public static BlockEntityType<MediumVoltageSUBlockEntity> MEDIUM_VOLTAGE_SU;
	public static BlockEntityType<HighVoltageSUBlockEntity> HIGH_VOLTAGE_SU;
	public static BlockEntityType<LVTransformerBlockEntity> LV_TRANSFORMER;
	public static BlockEntityType<MVTransformerBlockEntity> MV_TRANSFORMER;
	public static BlockEntityType<HVTransformerBlockEntity> HV_TRANSFORMER;
	public static BlockEntityType<EVTransformerBlockEntity> EV_TRANSFORMER;
	public static BlockEntityType<AutoCraftingTableBlockEntity> AUTO_CRAFTING_TABLE;
	public static BlockEntityType<IronFurnaceBlockEntity> IRON_FURNACE;
	public static BlockEntityType<ScrapboxinatorBlockEntity> SCRAPBOXINATOR;
	public static BlockEntityType<PlasmaGeneratorBlockEntity> PLASMA_GENERATOR;
	public static BlockEntityType<LampBlockEntity> LAMP;
	public static BlockEntityType<AlarmBlockEntity> ALARM;
	public static BlockEntityType<FluidReplicatorBlockEntity> FLUID_REPLICATOR;
	public static BlockEntityType<SolidCanningMachineBlockEntity> SOLID_CANNING_MACHINE;
	public static BlockEntityType<WireMillBlockEntity> WIRE_MILL;
	public static BlockEntityType<GreenhouseControllerBlockEntity> GREENHOUSE_CONTROLLER;
	public static BlockEntityType<BlockBreakerBlockEntity> BLOCK_BREAKER;
	public static BlockEntityType<BlockPlacerBlockEntity> BLOCK_PLACER;
	public static BlockEntityType<LaunchpadBlockEntity> LAUNCHPAD;
	public static BlockEntityType<ElevatorBlockEntity> ELEVATOR;
	public static BlockEntityType<FishingStationBlockEntity> FISHING_STATION;
	public static BlockEntityType<PumpBlockEntity> PUMP;

	private TRBlockEntities() {
	}

	private static <T extends BlockEntity> BlockEntityType<T> create(BiFunction<BlockPos, BlockState, T> supplier, ItemLike... items) {
		return create(supplier, Arrays.stream(items).map(itemConvertible -> Block.byItem(itemConvertible.asItem())).toArray(Block[]::new));
	}

	private static <T extends BlockEntity> BlockEntityType<T> create(BiFunction<BlockPos, BlockState, T> supplier, Block... blocks) {
		Validate.isTrue(blocks.length > 0, "no blocks for blockEntity entity type!");
		return new BlockEntityType<>(supplier::apply, Set.of(blocks));
	}

	private static void prepare() {
		synchronized (TRBlockEntities.class) {
			if (prepared) {
				return;
			}
			STORAGE_UNIT = create(StorageUnitBaseBlockEntity::new, TRContent.StorageUnit.values());
			TANK_UNIT = create(TankUnitBaseBlockEntity::new, TRContent.TankUnit.values());
			DRAIN = create(DrainBlockEntity::new, TRContent.Machine.DRAIN);
			THERMAL_GEN = create(ThermalGeneratorBlockEntity::new, TRContent.Machine.THERMAL_GENERATOR);
			INDUSTRIAL_CENTRIFUGE = create(IndustrialCentrifugeBlockEntity::new, TRContent.Machine.INDUSTRIAL_CENTRIFUGE);
			ROLLING_MACHINE = create(RollingMachineBlockEntity::new, TRContent.Machine.ROLLING_MACHINE);
			INDUSTRIAL_BLAST_FURNACE = create(IndustrialBlastFurnaceBlockEntity::new, TRContent.Machine.INDUSTRIAL_BLAST_FURNACE);
			ALLOY_SMELTER = create(AlloySmelterBlockEntity::new, TRContent.Machine.ALLOY_SMELTER);
			INDUSTRIAL_GRINDER = create(IndustrialGrinderBlockEntity::new, TRContent.Machine.INDUSTRIAL_GRINDER);
			IMPLOSION_COMPRESSOR = create(ImplosionCompressorBlockEntity::new, TRContent.Machine.IMPLOSION_COMPRESSOR);
			MATTER_FABRICATOR = create(MatterFabricatorBlockEntity::new, TRContent.Machine.MATTER_FABRICATOR);
			CHUNK_LOADER = create(ChunkLoaderBlockEntity::new, TRContent.Machine.CHUNK_LOADER);
			CHARGE_O_MAT = create(ChargeOMatBlockEntity::new, TRContent.Machine.CHARGE_O_MAT);
			PLAYER_DETECTOR = create(PlayerDetectorBlockEntity::new, TRContent.Machine.PLAYER_DETECTOR);
			CABLE = create(CableBlockEntity::new, TRContent.Cables.values());
			MACHINE_CASINGS = create(MachineCasingBlockEntity::new, TRContent.MachineBlocks.getCasings());
			DRAGON_EGG_SYPHON = create(DragonEggSyphonBlockEntity::new, TRContent.Machine.DRAGON_EGG_SYPHON);
			ASSEMBLY_MACHINE = create(AssemblingMachineBlockEntity::new, TRContent.Machine.ASSEMBLY_MACHINE);
			DIESEL_GENERATOR = create(DieselGeneratorBlockEntity::new, TRContent.Machine.DIESEL_GENERATOR);
			INDUSTRIAL_ELECTROLYZER = create(IndustrialElectrolyzerBlockEntity::new, TRContent.Machine.INDUSTRIAL_ELECTROLYZER);
			SEMI_FLUID_GENERATOR = create(SemiFluidGeneratorBlockEntity::new, TRContent.Machine.SEMI_FLUID_GENERATOR);
			GAS_TURBINE = create(GasTurbineBlockEntity::new, TRContent.Machine.GAS_TURBINE);
			IRON_ALLOY_FURNACE = create(IronAlloyFurnaceBlockEntity::new, TRContent.Machine.IRON_ALLOY_FURNACE);
			CHEMICAL_REACTOR = create(ChemicalReactorBlockEntity::new, TRContent.Machine.CHEMICAL_REACTOR);
			INTERDIMENSIONAL_SU = create(InterdimensionalSUBlockEntity::new, TRContent.Machine.INTERDIMENSIONAL_SU);
			ADJUSTABLE_SU = create(AdjustableSUBlockEntity::new, TRContent.Machine.ADJUSTABLE_SU);
			LAPOTRONIC_SU = create(LapotronicSUBlockEntity::new, TRContent.Machine.LAPOTRONIC_SU);
			LSU_STORAGE = create(LSUStorageBlockEntity::new, TRContent.Machine.LSU_STORAGE);
			DISTILLATION_TOWER = create(DistillationTowerBlockEntity::new, TRContent.Machine.DISTILLATION_TOWER);
			VACUUM_FREEZER = create(VacuumFreezerBlockEntity::new, TRContent.Machine.VACUUM_FREEZER);
			FUSION_CONTROL_COMPUTER = create(FusionControlComputerBlockEntity::new, TRContent.Machine.FUSION_CONTROL_COMPUTER);
			LIGHTNING_ROD = create(LightningRodBlockEntity::new, TRContent.Machine.LIGHTNING_ROD);
			INDUSTRIAL_SAWMILL = create(IndustrialSawmillBlockEntity::new, TRContent.Machine.INDUSTRIAL_SAWMILL);
			GRINDER = create(GrinderBlockEntity::new, TRContent.Machine.GRINDER);
			SOLID_FUEL_GENERATOR = create(SolidFuelGeneratorBlockEntity::new, TRContent.Machine.SOLID_FUEL_GENERATOR);
			EXTRACTOR = create(ExtractorBlockEntity::new, TRContent.Machine.EXTRACTOR);
			RESIN_BASIN = create(ResinBasinBlockEntity::new, TRContent.Machine.RESIN_BASIN);
			COMPRESSOR = create(CompressorBlockEntity::new, TRContent.Machine.COMPRESSOR);
			ELECTRIC_FURNACE = create(ElectricFurnaceBlockEntity::new, TRContent.Machine.ELECTRIC_FURNACE);
			SOLAR_PANEL = create(SolarPanelBlockEntity::new, TRContent.SolarPanels.values());
			WATER_MILL = create(WaterMillBlockEntity::new, TRContent.Machine.WATER_MILL);
			WIND_MILL = create(WindMillBlockEntity::new, TRContent.Machine.WIND_MILL);
			NUCLEAR_REACTOR = create(NuclearReactorBlockEntity::new, TRContent.Machine.NUCLEAR_REACTOR);
			REACTOR_CHAMBER = create(ReactorChamberBlockEntity::new, TRContent.Machine.REACTOR_CHAMBER);
			RECYCLER = create(RecyclerBlockEntity::new, TRContent.Machine.RECYCLER);
			LOW_VOLTAGE_SU = create(LowVoltageSUBlockEntity::new, TRContent.Machine.LOW_VOLTAGE_SU);
			MEDIUM_VOLTAGE_SU = create(MediumVoltageSUBlockEntity::new, TRContent.Machine.MEDIUM_VOLTAGE_SU);
			HIGH_VOLTAGE_SU = create(HighVoltageSUBlockEntity::new, TRContent.Machine.HIGH_VOLTAGE_SU);
			LV_TRANSFORMER = create(LVTransformerBlockEntity::new, TRContent.Machine.LV_TRANSFORMER);
			MV_TRANSFORMER = create(MVTransformerBlockEntity::new, TRContent.Machine.MV_TRANSFORMER);
			HV_TRANSFORMER = create(HVTransformerBlockEntity::new, TRContent.Machine.HV_TRANSFORMER);
			EV_TRANSFORMER = create(EVTransformerBlockEntity::new, TRContent.Machine.EV_TRANSFORMER);
			AUTO_CRAFTING_TABLE = create(AutoCraftingTableBlockEntity::new, TRContent.Machine.AUTO_CRAFTING_TABLE);
			IRON_FURNACE = create(IronFurnaceBlockEntity::new, TRContent.Machine.IRON_FURNACE);
			SCRAPBOXINATOR = create(ScrapboxinatorBlockEntity::new, TRContent.Machine.SCRAPBOXINATOR);
			PLASMA_GENERATOR = create(PlasmaGeneratorBlockEntity::new, TRContent.Machine.PLASMA_GENERATOR);
			LAMP = create(LampBlockEntity::new, TRContent.Machine.LAMP_INCANDESCENT, TRContent.Machine.LAMP_LED);
			ALARM = create(AlarmBlockEntity::new, TRContent.Machine.ALARM);
			FLUID_REPLICATOR = create(FluidReplicatorBlockEntity::new, TRContent.Machine.FLUID_REPLICATOR);
			SOLID_CANNING_MACHINE = create(SolidCanningMachineBlockEntity::new, TRContent.Machine.SOLID_CANNING_MACHINE);
			WIRE_MILL = create(WireMillBlockEntity::new, TRContent.Machine.WIRE_MILL);
			GREENHOUSE_CONTROLLER = create(GreenhouseControllerBlockEntity::new, TRContent.Machine.GREENHOUSE_CONTROLLER);
			BLOCK_BREAKER = create(BlockBreakerBlockEntity::new, TRContent.Machine.BLOCK_BREAKER);
			BLOCK_PLACER = create(BlockPlacerBlockEntity::new, TRContent.Machine.BLOCK_PLACER);
			LAUNCHPAD = create(LaunchpadBlockEntity::new, TRContent.Machine.LAUNCHPAD);
			ELEVATOR = create(ElevatorBlockEntity::new, TRContent.Machine.ELEVATOR);
			FISHING_STATION = create(FishingStationBlockEntity::new, TRContent.Machine.FISHING_STATION);
			PUMP = create(PumpBlockEntity::new, TRContent.Machine.PUMP);

			TYPES.clear();
			TYPES.addAll(Arrays.asList(
				STORAGE_UNIT, TANK_UNIT, DRAIN, THERMAL_GEN, INDUSTRIAL_CENTRIFUGE, ROLLING_MACHINE,
				INDUSTRIAL_BLAST_FURNACE, ALLOY_SMELTER, INDUSTRIAL_GRINDER, IMPLOSION_COMPRESSOR, MATTER_FABRICATOR,
				CHUNK_LOADER, CHARGE_O_MAT, PLAYER_DETECTOR, CABLE, MACHINE_CASINGS, DRAGON_EGG_SYPHON,
				ASSEMBLY_MACHINE, DIESEL_GENERATOR, INDUSTRIAL_ELECTROLYZER, SEMI_FLUID_GENERATOR, GAS_TURBINE,
				IRON_ALLOY_FURNACE, CHEMICAL_REACTOR, INTERDIMENSIONAL_SU, ADJUSTABLE_SU, LAPOTRONIC_SU,
				LSU_STORAGE, DISTILLATION_TOWER, VACUUM_FREEZER, FUSION_CONTROL_COMPUTER, LIGHTNING_ROD,
				INDUSTRIAL_SAWMILL, GRINDER, SOLID_FUEL_GENERATOR, EXTRACTOR, RESIN_BASIN, COMPRESSOR,
				ELECTRIC_FURNACE, SOLAR_PANEL, WATER_MILL, WIND_MILL, NUCLEAR_REACTOR, REACTOR_CHAMBER, RECYCLER, LOW_VOLTAGE_SU, MEDIUM_VOLTAGE_SU,
				HIGH_VOLTAGE_SU, LV_TRANSFORMER, MV_TRANSFORMER, HV_TRANSFORMER, EV_TRANSFORMER,
				AUTO_CRAFTING_TABLE, IRON_FURNACE, SCRAPBOXINATOR, PLASMA_GENERATOR, LAMP, ALARM,
				FLUID_REPLICATOR, SOLID_CANNING_MACHINE, WIRE_MILL, GREENHOUSE_CONTROLLER, BLOCK_BREAKER,
				BLOCK_PLACER, LAUNCHPAD, ELEVATOR, FISHING_STATION, PUMP));
			prepared = true;
		}
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, path);
	}

	private static void registerType(RegisterEvent event, String path, BlockEntityType<?> type) {
		event.register(Registries.BLOCK_ENTITY_TYPE, id(path), () -> type);
	}

	public static void register(RegisterEvent event) {
		ResourceKey<? extends Registry<?>> key = event.getRegistryKey();
		if (!key.equals(Registries.BLOCK_ENTITY_TYPE)) {
			return;
		}
		prepare();
		registerType(event, "storage_unit", STORAGE_UNIT);
		registerType(event, "tank_unit", TANK_UNIT);
		registerType(event, "drain", DRAIN);
		registerType(event, "thermal_generator", THERMAL_GEN);
		registerType(event, "industrial_centrifuge", INDUSTRIAL_CENTRIFUGE);
		registerType(event, "rolling_machine", ROLLING_MACHINE);
		registerType(event, "industrial_blast_furnace", INDUSTRIAL_BLAST_FURNACE);
		registerType(event, "alloy_smelter", ALLOY_SMELTER);
		registerType(event, "industrial_grinder", INDUSTRIAL_GRINDER);
		registerType(event, "implosion_compressor", IMPLOSION_COMPRESSOR);
		registerType(event, "matter_fabricator", MATTER_FABRICATOR);
		registerType(event, "chunk_loader", CHUNK_LOADER);
		registerType(event, "charge_o_mat", CHARGE_O_MAT);
		registerType(event, "player_detector", PLAYER_DETECTOR);
		registerType(event, "cable", CABLE);
		registerType(event, "machine_casing", MACHINE_CASINGS);
		registerType(event, "dragon_egg_syphon", DRAGON_EGG_SYPHON);
		registerType(event, "assembly_machine", ASSEMBLY_MACHINE);
		registerType(event, "diesel_generator", DIESEL_GENERATOR);
		registerType(event, "industrial_electrolyzer", INDUSTRIAL_ELECTROLYZER);
		registerType(event, "semi_fluid_generator", SEMI_FLUID_GENERATOR);
		registerType(event, "gas_turbine", GAS_TURBINE);
		registerType(event, "iron_alloy_furnace", IRON_ALLOY_FURNACE);
		registerType(event, "chemical_reactor", CHEMICAL_REACTOR);
		registerType(event, "interdimensional_su", INTERDIMENSIONAL_SU);
		registerType(event, "adjustable_su", ADJUSTABLE_SU);
		registerType(event, "lapotronic_su", LAPOTRONIC_SU);
		registerType(event, "lsu_storage", LSU_STORAGE);
		registerType(event, "distillation_tower", DISTILLATION_TOWER);
		registerType(event, "vacuum_freezer", VACUUM_FREEZER);
		registerType(event, "fusion_control_computer", FUSION_CONTROL_COMPUTER);
		registerType(event, "lightning_rod", LIGHTNING_ROD);
		registerType(event, "industrial_sawmill", INDUSTRIAL_SAWMILL);
		registerType(event, "grinder", GRINDER);
		registerType(event, "solid_fuel_generator", SOLID_FUEL_GENERATOR);
		registerType(event, "extractor", EXTRACTOR);
		registerType(event, "resin_basin", RESIN_BASIN);
		registerType(event, "compressor", COMPRESSOR);
		registerType(event, "electric_furnace", ELECTRIC_FURNACE);
		registerType(event, "solar_panel", SOLAR_PANEL);
		registerType(event, "water_mill", WATER_MILL);
		registerType(event, "wind_mill", WIND_MILL);
		registerType(event, "nuclear_reactor", NUCLEAR_REACTOR);
		registerType(event, "reactor_chamber", REACTOR_CHAMBER);
		registerType(event, "recycler", RECYCLER);
		registerType(event, "low_voltage_su", LOW_VOLTAGE_SU);
		registerType(event, "medium_voltage_su", MEDIUM_VOLTAGE_SU);
		registerType(event, "high_voltage_su", HIGH_VOLTAGE_SU);
		registerType(event, "lv_transformer", LV_TRANSFORMER);
		registerType(event, "mv_transformer", MV_TRANSFORMER);
		registerType(event, "hv_transformer", HV_TRANSFORMER);
		registerType(event, "ev_transformer", EV_TRANSFORMER);
		registerType(event, "auto_crafting_table", AUTO_CRAFTING_TABLE);
		registerType(event, "iron_furnace", IRON_FURNACE);
		registerType(event, "scrapboxinator", SCRAPBOXINATOR);
		registerType(event, "plasma_generator", PLASMA_GENERATOR);
		registerType(event, "lamp", LAMP);
		registerType(event, "alarm", ALARM);
		registerType(event, "fluid_replicator", FLUID_REPLICATOR);
		registerType(event, "solid_canning_machine", SOLID_CANNING_MACHINE);
		registerType(event, "wire_mill", WIRE_MILL);
		registerType(event, "greenhouse_controller", GREENHOUSE_CONTROLLER);
		registerType(event, "block_breaker", BLOCK_BREAKER);
		registerType(event, "block_placer", BLOCK_PLACER);
		registerType(event, "launchpad", LAUNCHPAD);
		registerType(event, "elevator", ELEVATOR);
		registerType(event, "fishing_station", FISHING_STATION);
		registerType(event, "pump", PUMP);
	}
}
