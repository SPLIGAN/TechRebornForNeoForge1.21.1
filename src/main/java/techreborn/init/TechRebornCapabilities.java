/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.init;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reborncore.common.transfer.LegacyFluidHandlerResourceHandler;
import reborncore.common.transfer.LegacyItemHandlerResourceHandler;
import reborncore.common.transfer.RcStorageItemHandler;
import reborncore.common.transfer.TankFluidHandler;
import reborncore.common.util.Tank;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.capability.TeamRebornEnergyCapabilities;
import techreborn.blockentity.cable.CableBlockEntity;
import techreborn.blockentity.generator.nuclear.ReactorChamberBlockEntity;
import techreborn.blockentity.storage.item.StorageUnitBaseBlockEntity;

public final class TechRebornCapabilities {
	private TechRebornCapabilities() {
	}

	public static void register(RegisterCapabilitiesEvent event) {
		for (BlockEntityType<?> type : TRBlockEntities.allRegisteredTypes()) {
			registerSidedEnergy(event, type);
			registerFluidTank(event, type);
		}

		event.registerBlockEntity(Capabilities.Item.BLOCK, TRBlockEntities.STORAGE_UNIT, (be, side) -> {
			if (be instanceof StorageUnitBaseBlockEntity storageUnit) {
				return new LegacyItemHandlerResourceHandler(new RcStorageItemHandler(storageUnit.getExposedStorage(side)));
			}
			return null;
		});
	}

	private static <T extends BlockEntity> void registerSidedEnergy(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
		event.registerBlockEntity(TeamRebornEnergyCapabilities.BLOCK_SIDED, type, TechRebornCapabilities::sidedEnergyForBlockEntity);
	}

	private static <T extends BlockEntity> void registerFluidTank(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, TechRebornCapabilities::fluidHandlerForBlockEntity);
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

	private static @Nullable ResourceHandler<FluidResource> fluidHandlerForBlockEntity(BlockEntity be, @SuppressWarnings("unused") @Nullable Direction face) {
		if (be instanceof MachineBaseBlockEntity machine) {
			Tank tank = machine.getTank();
			if (tank != null) {
				return new LegacyFluidHandlerResourceHandler(new TankFluidHandler(tank));
			}
		}
		return null;
	}
}
