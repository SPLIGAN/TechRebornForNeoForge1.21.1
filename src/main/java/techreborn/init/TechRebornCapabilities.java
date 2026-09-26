/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.init;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.jetbrains.annotations.Nullable;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.energy.api.base.SimpleEnergyItem;
import reborncore.common.energy.capability.EnergyStorageEnergyHandler;
import reborncore.common.energy.capability.SimpleEnergyItemEnergyHandler;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reborncore.common.transfer.RcTankResourceHandler;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.capability.TeamRebornEnergyCapabilities;
import techreborn.TechReborn;
import techreborn.blockentity.cable.CableBlockEntity;
import techreborn.blockentity.generator.nuclear.ReactorChamberBlockEntity;
import techreborn.blockentity.storage.item.StorageUnitBaseBlockEntity;
import techreborn.items.CellFluidHandler;

public final class TechRebornCapabilities {
	private TechRebornCapabilities() {
	}

	public static void register(RegisterCapabilitiesEvent event) {
		for (BlockEntityType<?> type : TRBlockEntities.allRegisteredTypes()) {
			registerSidedEnergy(event, type);
			registerFluidTank(event, type);
			if (type != TRBlockEntities.STORAGE_UNIT) {
				registerMachineItemHandler(event, type);
			}
		}

		event.registerBlockEntity(Capabilities.Item.BLOCK, TRBlockEntities.STORAGE_UNIT, (be, side) -> {
			if (be instanceof StorageUnitBaseBlockEntity storageUnit) {
				return storageUnit.getItemHandler(side);
			}
			return null;
		});

		registerItemEnergy(event);
		event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new CellFluidHandler(access), TRContent.Cells.values());
	}

	private static <T extends BlockEntity> void registerSidedEnergy(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
		event.registerBlockEntity(TeamRebornEnergyCapabilities.BLOCK_SIDED, type, TechRebornCapabilities::sidedEnergyForBlockEntity);
		event.registerBlockEntity(Capabilities.Energy.BLOCK, type, TechRebornCapabilities::forgeEnergyForBlockEntity);
	}

	private static <T extends BlockEntity> void registerFluidTank(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, TechRebornCapabilities::fluidHandlerForBlockEntity);
	}

	private static <T extends BlockEntity> void registerMachineItemHandler(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, type, TechRebornCapabilities::itemHandlerForBlockEntity);
	}

	private static void registerItemEnergy(RegisterCapabilitiesEvent event) {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof SimpleEnergyItem energyItem && TechReborn.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) {
				event.registerItem(Capabilities.Energy.ITEM, (stack, access) -> new SimpleEnergyItemEnergyHandler(access, energyItem), item);
			}
		}
	}

	private static @Nullable EnergyStorage sidedEnergyForBlockEntity(BlockEntity be, @Nullable Direction face) {
		if (be instanceof PowerAcceptorBlockEntity pa) {
			return pa.getSideEnergyStorage(face);
		}
		if (be instanceof ReactorChamberBlockEntity chamber) {
			return chamber.getSideEnergyStorage(face);
		}
		if (be instanceof CableBlockEntity cable) {
			return cable.getSideEnergyStorage(face);
		}
		return null;
	}

	private static @Nullable EnergyHandler forgeEnergyForBlockEntity(BlockEntity be, @Nullable Direction face) {
		EnergyStorage storage = sidedEnergyForBlockEntity(be, face);
		return storage == null ? null : new EnergyStorageEnergyHandler(storage);
	}

	/**
	 * Automation access (hoppers, pipes) goes through the machine's {@link net.minecraft.world.WorldlyContainer}
	 * implementation, i.e. the per-side slot configuration. The {@code null} side is not exposed because slot
	 * configuration has no notion of it.
	 */
	private static @Nullable ResourceHandler<ItemResource> itemHandlerForBlockEntity(BlockEntity be, @Nullable Direction side) {
		if (side == null || !(be instanceof MachineBaseBlockEntity machine) || machine.getOptionalInventory().isEmpty()) {
			return null;
		}
		return new WorldlyContainerWrapper(machine, side);
	}

	private static @Nullable ResourceHandler<FluidResource> fluidHandlerForBlockEntity(BlockEntity be, @SuppressWarnings("unused") @Nullable Direction face) {
		if (be instanceof MachineBaseBlockEntity machine && machine.getTank() != null) {
			return new RcTankResourceHandler(machine::getTank);
		}
		return null;
	}
}
