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

package techreborn.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import reborncore.common.fluid.FluidValue;
import reborncore.common.fluid.container.FluidInstance;
import techreborn.config.TechRebornConfig;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import reborncore.common.RebornCoreConfig;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.blockentity.MachinePermissions;
import reborncore.common.blockentity.SlotConfiguration;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.api.EnergyStorageUtil;
import reborncore.common.energy.api.base.SimpleEnergyItem;
import reborncore.common.energy.capability.EnergyHandlerEnergyStorage;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reborncore.common.transfer.RcTransaction;
import techreborn.blockentity.storage.energy.EnergyStorageBlockEntity;
import techreborn.blockentity.storage.item.StorageUnitBaseBlockEntity;
import techreborn.init.TRContent;

/**
 * NeoForge interop: FE ({@link Capabilities.Energy}) and automated item I/O ({@link Capabilities.Item#BLOCK}).
 */
public final class InteropGameTests {
	private static final BlockPos MACHINE = new BlockPos(2, 1, 2);
	private static final int SETUP_DELAY = 5;

	private InteropGameTests() {
	}

	/** External FE producers can insert into, and FE consumers extract from, a TR machine; sides and rollback are honoured. */
	public static void feBlockInsertExtract(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.Machine.LOW_VOLTAGE_SU.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			EnergyStorageBlockEntity su = helper.getBlockEntity(MACHINE, EnergyStorageBlockEntity.class);
			Direction output = su.getFacing();
			Direction input = output.getOpposite();
			BlockPos abs = helper.absolutePos(MACHINE);
			EnergyHandler inputSide = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, abs, input);
			EnergyHandler outputSide = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, abs, output);
			check(helper, inputSide != null && outputSide != null, "FE capability missing on LV SU");
			check(helper, inputSide.getCapacityAsLong() == su.getMaxStoredPower(), "FE capacity mismatch");

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, inputSide.insert(10, tx) == 10, "FE insert did not accept energy");
			}
			check(helper, su.getStored() == 0, "Aborted FE insert was not rolled back: " + su.getStored());

			int maxInput = (int) su.getMaxInput(input);
			int inserted;
			try (Transaction tx = Transaction.openRoot()) {
				inserted = inputSide.insert(maxInput * 4, tx);
				try (Transaction nested = Transaction.open(tx)) {
					inputSide.insert(maxInput, nested);
				}
				tx.commit();
			}
			check(helper, inserted == maxInput, "FE insert ignored the per-operation input limit: " + inserted);
			check(helper, su.getStored() == inserted, "Committed FE insert not stored: " + su.getStored());

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, outputSide.insert(10, tx) == 0, "Output side accepted FE");
				check(helper, inputSide.extract(10, tx) == 0, "Input side provided FE");
				check(helper, outputSide.extract(5, tx) == 5, "Output side did not provide FE");
				tx.commit();
			}
			check(helper, su.getStored() == inserted - 5, "FE extract not applied: " + su.getStored());
			helper.succeed();
		});
	}

	/** TR energy storages push into / pull from FE-only handlers through the fallback wrapper, including rollback. */
	public static void feFallbackTransfer(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.Machine.LOW_VOLTAGE_SU.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			EnergyStorageBlockEntity su = helper.getBlockEntity(MACHINE, EnergyStorageBlockEntity.class);
			Direction output = su.getFacing();
			su.setStored(1000);
			SimpleEnergyHandler forge = new SimpleEnergyHandler(100_000, 1_000, 1_000);
			EnergyStorage forgeAsTr = new EnergyHandlerEnergyStorage(forge);

			try (RcTransaction tx = RcTransaction.openOuter()) {
				EnergyStorageUtil.move(su.getSideEnergyStorage(output), forgeAsTr, Long.MAX_VALUE, tx);
			}
			check(helper, forge.getAmountAsLong() == 0 && su.getStored() == 1000, "Aborted TR -> FE move was not rolled back");

			long pushed = EnergyStorageUtil.move(su.getSideEnergyStorage(output), forgeAsTr, Long.MAX_VALUE, null);
			check(helper, pushed > 0 && forge.getAmountAsLong() == pushed && su.getStored() == 1000 - pushed, "TR -> FE move failed: " + pushed);

			long pulled = EnergyStorageUtil.move(forgeAsTr, su.getSideEnergyStorage(output.getOpposite()), Long.MAX_VALUE, null);
			check(helper, pulled > 0 && forge.getAmountAsLong() == pushed - pulled, "FE -> TR move failed: " + pulled);
			helper.succeed();
		});
	}

	/** A TR machine next to an FE-only neighbour pushes energy into it on its own tick. */
	public static void feMachinePushesToNeighbour(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.SolarPanels.CREATIVE.block);
		helper.succeedWhen(() -> {
			PowerAcceptorBlockEntity solar = helper.getBlockEntity(MACHINE, PowerAcceptorBlockEntity.class);
			check(helper, solar.getStored() > 0, "Creative solar panel has no energy yet");
			SimpleEnergyHandler forge = new SimpleEnergyHandler(100_000);
			long moved = EnergyStorageUtil.move(solar.getSideEnergyStorage(Direction.UP), new EnergyHandlerEnergyStorage(forge), Long.MAX_VALUE, null);
			check(helper, moved > 0 && forge.getAmountAsLong() == moved, "Solar panel did not push FE");
		});
	}

	/** TR energy items expose {@link Capabilities.Energy#ITEM}. */
	public static void feItemCapability(GameTestHelper helper) {
		ItemStack battery = new ItemStack(TRContent.RED_CELL_BATTERY);
		EnergyHandler handler = ItemAccess.forStack(battery).getCapability(Capabilities.Energy.ITEM);
		check(helper, handler != null, "FE item capability missing on battery");
		int inserted;
		try (Transaction tx = Transaction.openRoot()) {
			inserted = handler.insert(Integer.MAX_VALUE, tx);
			tx.commit();
		}
		SimpleEnergyItem item = (SimpleEnergyItem) battery.getItem();
		check(helper, inserted > 0 && inserted <= item.getEnergyMaxInput(battery), "Battery FE insert out of range: " + inserted);
		check(helper, item.getStoredEnergy(battery) == inserted && handler.getAmountAsLong() == inserted, "Battery FE amount mismatch");
		helper.succeed();
	}

	/** A vanilla hopper feeds a TR machine through a side configured as input; unconfigured sides reject items. */
	public static void hopperInsertsIntoMachine(GameTestHelper helper) {
		BlockPos hopperPos = MACHINE.above();
		helper.setBlock(MACHINE, TRContent.Machine.ELECTRIC_FURNACE.block);
		helper.setBlock(hopperPos, Blocks.HOPPER);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity furnace = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			ResourceHandler<ItemResource> top = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(MACHINE), Direction.UP);
			check(helper, top != null, "Item capability missing on electric furnace");
			try (Transaction tx = Transaction.openRoot()) {
				check(helper, top.insert(ItemResource.of(Items.RAW_IRON), 1, tx) == 0, "Unconfigured side accepted items");
			}

			setSlotIo(furnace, 0, Direction.UP, SlotConfiguration.ExtractConfig.INPUT);
			try (Transaction tx = Transaction.openRoot()) {
				check(helper, top.insert(ItemResource.of(Items.RAW_IRON), 1, tx) == 1, "Configured side rejected items");
			}
			check(helper, furnace.getItem(0).isEmpty(), "Aborted item insert was not rolled back");

			helper.getBlockEntity(hopperPos, HopperBlockEntity.class).setItem(0, new ItemStack(Items.RAW_IRON, 3));
			helper.succeedWhen(() -> {
				ItemStack input = furnace.getItem(0);
				check(helper, input.is(Items.RAW_IRON) && input.getCount() == 3, "Hopper has not filled the furnace yet: " + input);
			});
		});
	}

	/** Automation can extract from a side configured as output, honouring transactions. */
	public static void machineItemExtract(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.Machine.ELECTRIC_FURNACE.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity furnace = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			furnace.setItem(1, new ItemStack(Items.IRON_INGOT, 8));
			ResourceHandler<ItemResource> bottom = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(MACHINE), Direction.DOWN);
			check(helper, bottom != null, "Item capability missing on electric furnace");
			try (Transaction tx = Transaction.openRoot()) {
				check(helper, bottom.extract(ItemResource.of(Items.IRON_INGOT), 8, tx) == 0, "Unconfigured side provided items");
			}

			setSlotIo(furnace, 1, Direction.DOWN, SlotConfiguration.ExtractConfig.OUTPUT);
			try (Transaction tx = Transaction.openRoot()) {
				check(helper, bottom.extract(ItemResource.of(Items.IRON_INGOT), 8, tx) == 8, "Configured side did not provide items");
			}
			check(helper, furnace.getItem(1).getCount() == 8, "Aborted item extract was not rolled back");
			try (Transaction tx = Transaction.openRoot()) {
				check(helper, bottom.extract(ItemResource.of(Items.IRON_INGOT), 5, tx) == 5, "Committed extract failed");
				tx.commit();
			}
			check(helper, furnace.getItem(1).getCount() == 3, "Committed extract not applied: " + furnace.getItem(1));
			helper.succeed();
		});
	}

	/** Storage unit item capability: insert/extract with commit, root abort and nested abort never create or lose items. */
	public static void storageUnitTransactions(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.StorageUnit.BASIC.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			StorageUnitBaseBlockEntity unit = helper.getBlockEntity(MACHINE, StorageUnitBaseBlockEntity.class);
			BlockPos abs = helper.absolutePos(MACHINE);
			ResourceHandler<ItemResource> top = helper.getLevel().getCapability(Capabilities.Item.BLOCK, abs, Direction.UP);
			ResourceHandler<ItemResource> bottom = helper.getLevel().getCapability(Capabilities.Item.BLOCK, abs, Direction.DOWN);
			check(helper, top != null && bottom != null, "Item capability missing on storage unit");
			ItemResource cobble = ItemResource.of(Items.COBBLESTONE);

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, top.insert(cobble, 10, tx) == 0, "Unconfigured side accepted items");
			}
			setSlotIo(unit, StorageUnitBaseBlockEntity.INPUT_SLOT, Direction.UP, SlotConfiguration.ExtractConfig.INPUT);
			setSlotIo(unit, StorageUnitBaseBlockEntity.OUTPUT_SLOT, Direction.DOWN, SlotConfiguration.ExtractConfig.OUTPUT);

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, top.insert(cobble, 100, tx) == 100, "Insert rejected");
			}
			check(helper, unit.getCurrentCapacity() == 0, "Aborted insert was not rolled back: " + unit.getCurrentCapacity());

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, top.insert(cobble, 100, tx) == 100, "Insert rejected");
				try (Transaction nested = Transaction.open(tx)) {
					check(helper, top.insert(cobble, 10, nested) == 10, "Nested insert rejected");
				}
				tx.commit();
			}
			check(helper, unit.getCurrentCapacity() == 100, "Committed insert with aborted nested insert: " + unit.getCurrentCapacity());

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, top.insert(ItemResource.of(Items.DIRT), 1, tx) == 0, "Different item accepted");
				check(helper, top.extract(cobble, 1, tx) == 0, "Input-only side provided items");
				check(helper, bottom.insert(cobble, 1, tx) == 0, "Output-only side accepted items");
			}

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, bottom.extract(cobble, 60, tx) == 60, "Extract failed");
			}
			check(helper, unit.getCurrentCapacity() == 100, "Aborted extract was not rolled back: " + unit.getCurrentCapacity());

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, bottom.extract(cobble, 30, tx) == 30, "Extract failed");
				try (Transaction nested = Transaction.open(tx)) {
					bottom.extract(cobble, 50, nested);
				}
				tx.commit();
			}
			check(helper, unit.getCurrentCapacity() == 70, "Committed extract with aborted nested extract: " + unit.getCurrentCapacity());

			// Let the unit tick (moving items between its internal stack and output slot), then re-check the total.
			helper.runAfterDelay(5, () -> {
				check(helper, unit.getCurrentCapacity() == 70, "Item count changed after ticking: " + unit.getCurrentCapacity());
				try (Transaction tx = Transaction.openRoot()) {
					check(helper, bottom.extract(cobble, 1000, tx) == 70, "Could not extract full contents");
				}
				check(helper, unit.getCurrentCapacity() == 70, "Aborted full extract was not rolled back");
				helper.succeed();
			});
		});
	}

	/** A vanilla hopper draining a storage unit conserves the total item count. */
	public static void storageUnitHopperConservation(GameTestHelper helper) {
		BlockPos unitPos = MACHINE.above();
		helper.setBlock(unitPos, TRContent.StorageUnit.BASIC.block);
		helper.setBlock(MACHINE, Blocks.HOPPER);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			StorageUnitBaseBlockEntity unit = helper.getBlockEntity(unitPos, StorageUnitBaseBlockEntity.class);
			setSlotIo(unit, StorageUnitBaseBlockEntity.OUTPUT_SLOT, Direction.DOWN, SlotConfiguration.ExtractConfig.OUTPUT);
			setSlotIo(unit, StorageUnitBaseBlockEntity.INPUT_SLOT, Direction.UP, SlotConfiguration.ExtractConfig.INPUT);
			ResourceHandler<ItemResource> top = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(unitPos), Direction.UP);
			try (Transaction tx = Transaction.openRoot()) {
				top.insert(ItemResource.of(Items.COBBLESTONE), 80, tx);
				tx.commit();
			}
			HopperBlockEntity hopper = helper.getBlockEntity(MACHINE, HopperBlockEntity.class);
			helper.succeedWhen(() -> {
				int inHopper = 0;
				for (int i = 0; i < hopper.getContainerSize(); i++) {
					inHopper += hopper.getItem(i).getCount();
				}
				check(helper, unit.getCurrentCapacity() + inHopper == 80, "Items not conserved: unit=" + unit.getCurrentCapacity() + " hopper=" + inHopper);
				check(helper, inHopper >= 3, "Hopper has not pulled enough yet: " + inHopper);
			});
		});
	}

	/** Tank capability: whole-millibucket transfers, rollback on abort, fluid type enforced. */
	public static void tankUnitFluidTransactions(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.TankUnit.BASIC.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity unit = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			ResourceHandler<FluidResource> handler = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(MACHINE), Direction.UP);
			check(helper, handler != null && unit.getTank() != null, "Fluid capability missing on tank unit");
			FluidResource water = FluidResource.of(Fluids.WATER);

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, handler.insert(water, 1000, tx) == 1000, "Fluid insert rejected");
			}
			check(helper, unit.getTank().getAmount() == 0, "Aborted fluid insert was not rolled back");

			try (Transaction tx = Transaction.openRoot()) {
				handler.insert(water, 1000, tx);
				try (Transaction nested = Transaction.open(tx)) {
					handler.insert(water, 500, nested);
				}
				tx.commit();
			}
			check(helper, handler.getAmountAsLong(0) == 1000, "Committed fluid insert: " + handler.getAmountAsLong(0));

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, handler.insert(FluidResource.of(Fluids.LAVA), 100, tx) == 0, "Different fluid accepted");
				check(helper, handler.extract(FluidResource.of(Fluids.LAVA), 100, tx) == 0, "Different fluid extracted");
				check(helper, handler.extract(water, 400, tx) == 400, "Fluid extract failed");
			}
			check(helper, handler.getAmountAsLong(0) == 1000, "Aborted fluid extract was not rolled back");

			try (Transaction tx = Transaction.openRoot()) {
				check(helper, handler.extract(water, 400, tx) == 400, "Fluid extract failed");
				tx.commit();
			}
			check(helper, handler.getAmountAsLong(0) == 600 && unit.getTank().getAmount() == 600 * 81, "Committed fluid extract: " + unit.getTank().getAmount());
			helper.succeed();
		});
	}

	/** Non-operators cannot place, open or break creative units unless the config allows it. */
	public static void creativeUnitsOpOnly(GameTestHelper helper) {
		BlockPos unitPos = MACHINE;
		helper.setBlock(unitPos, TRContent.StorageUnit.CREATIVE.block);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockPos abs = helper.absolutePos(unitPos);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
		boolean previous = TechRebornConfig.creativeUnitsOpOnly;
		try {
			for (Block block : new Block[] {TRContent.StorageUnit.CREATIVE.block, TRContent.TankUnit.CREATIVE.block}) {
				BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, new ItemStack(block), hit);
				TechRebornConfig.creativeUnitsOpOnly = true;
				check(helper, block.getStateForPlacement(context) == null, "Non-op could place " + block);
				TechRebornConfig.creativeUnitsOpOnly = false;
				check(helper, block.getStateForPlacement(context) != null, "Placement blocked with the restriction disabled: " + block);
			}
			BlockPlaceContext normal = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, new ItemStack(TRContent.StorageUnit.BASIC.block), hit);
			TechRebornConfig.creativeUnitsOpOnly = true;
			check(helper, TRContent.StorageUnit.BASIC.block.getStateForPlacement(normal) != null, "Non-creative unit placement blocked");

			BlockState state = helper.getBlockState(unitPos);
			check(helper, state.useWithoutItem(helper.getLevel(), player, hit) == InteractionResult.FAIL, "Non-op could use a creative unit");
			check(helper, state.getDestroyProgress(player, helper.getLevel(), abs) == 0, "Non-op could break a creative unit");
		} finally {
			TechRebornConfig.creativeUnitsOpOnly = previous;
		}
		helper.succeed();
	}

	/** Auto-input pulls from a neighbour without losing items, also when the target slot is almost full. */
	public static void autoSlotInputConservesItems(GameTestHelper helper) {
		BlockPos chestPos = MACHINE.above();
		helper.setBlock(MACHINE, TRContent.Machine.ELECTRIC_FURNACE.block);
		helper.setBlock(chestPos, Blocks.CHEST);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity furnace = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			ChestBlockEntity chest = helper.getBlockEntity(chestPos, ChestBlockEntity.class);
			chest.setItem(0, new ItemStack(Items.RAW_IRON, 10));
			furnace.setItem(0, new ItemStack(Items.RAW_IRON, 62));
			setSlotIo(furnace, 0, Direction.UP, SlotConfiguration.ExtractConfig.INPUT);
			furnace.getSlotConfiguration().getSlotDetails(0).setInput(true);
			helper.succeedWhen(() -> {
				int total = chest.getItem(0).getCount() + furnace.getItem(0).getCount() + furnace.getItem(1).getCount();
				check(helper, total == 72, "Items lost or duplicated by auto-input: " + total);
				check(helper, furnace.getItem(0).getCount() == 64, "Auto-input has not filled the slot yet: " + furnace.getItem(0));
			});
		});
	}

	/** Auto-output pushes into a neighbour without losing or duplicating items. */
	public static void autoSlotOutputConservesItems(GameTestHelper helper) {
		BlockPos chestPos = MACHINE.below();
		helper.setBlock(chestPos, Blocks.CHEST);
		helper.setBlock(MACHINE, TRContent.Machine.ELECTRIC_FURNACE.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity furnace = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			ChestBlockEntity chest = helper.getBlockEntity(chestPos, ChestBlockEntity.class);
			for (int i = 1; i < chest.getContainerSize(); i++) {
				chest.setItem(i, new ItemStack(Items.DIRT, 64));
			}
			chest.setItem(0, new ItemStack(Items.IRON_INGOT, 50));
			furnace.setItem(1, new ItemStack(Items.IRON_INGOT, 20));
			setSlotIo(furnace, 1, Direction.DOWN, SlotConfiguration.ExtractConfig.OUTPUT);
			furnace.getSlotConfiguration().getSlotDetails(1).setOutput(true);
			helper.succeedWhen(() -> {
				int total = chest.getItem(0).getCount() + furnace.getItem(1).getCount();
				check(helper, total == 70, "Items lost or duplicated by auto-output: " + total);
				check(helper, chest.getItem(0).getCount() == 64 && furnace.getItem(1).getCount() == 6, "Auto-output has not finished: " + furnace.getItem(1));
			});
		});
	}

	/** Cells expose Capabilities.Fluid.ITEM and are only filled or emptied a whole bucket at a time. */
	public static void fluidCellCapability(GameTestHelper helper) {
		// Container-backed access: a plain stack access cannot change the item type.
		ItemAccess emptyAccess = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(new SimpleContainer(new ItemStack(TRContent.Cells.EMPTY, 4))), 0);
		ResourceHandler<FluidResource> emptyHandler = emptyAccess.getCapability(Capabilities.Fluid.ITEM);
		check(helper, emptyHandler != null, "Fluid item capability missing on empty cell");
		FluidResource water = FluidResource.of(Fluids.WATER);
		try (Transaction tx = Transaction.openRoot()) {
			check(helper, emptyHandler.insert(water, 3500, tx) == 0, "Partial cell fill accepted");
			check(helper, emptyHandler.insert(water, 4000, tx) == 4000, "Cells not filled");
		}
		check(helper, emptyAccess.getResource().is(TRContent.Cells.EMPTY.asItem()), "Aborted fill changed the cells");

		ItemAccess fullAccess = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(new SimpleContainer(new ItemStack(TRContent.Cells.WATER, 2))), 0);
		ResourceHandler<FluidResource> fullHandler = fullAccess.getCapability(Capabilities.Fluid.ITEM);
		check(helper, fullHandler != null && fullHandler.getAmountAsLong(0) == 2000, "Water cells do not report 2000 mB");
		try (Transaction tx = Transaction.openRoot()) {
			check(helper, fullHandler.extract(FluidResource.of(Fluids.LAVA), 2000, tx) == 0, "Wrong fluid extracted");
			check(helper, fullHandler.extract(water, 2000, tx) == 2000, "Water not extracted");
			tx.commit();
		}
		check(helper, fullAccess.getResource().is(TRContent.Cells.EMPTY.asItem()) && fullAccess.getAmount() == 2, "Emptied cells not returned: " + fullAccess.getResource());
		helper.succeed();
	}

	/** A tank unit fills a stack of empty cells one at a time into its output slot; fluid and cells are conserved. */
	public static void tankUnitFillsCells(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.TankUnit.BASIC.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity unit = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			unit.getTank().setFluidInstance(new FluidInstance(Fluids.WATER, FluidValue.BUCKET.multiply(3)));
			unit.setItem(0, new ItemStack(TRContent.Cells.EMPTY, 16));
			helper.succeedWhen(() -> {
				ItemStack input = unit.getItem(0);
				ItemStack output = unit.getItem(1);
				long tankMb = unit.getTank().getAmount() / 81;
				check(helper, input.getCount() + output.getCount() == 16, "Cells lost or duplicated: " + input + " / " + output);
				check(helper, tankMb + 1000L * (output.is(TRContent.Cells.WATER.asItem()) ? output.getCount() : 0) == 3000, "Fluid not conserved: tank=" + tankMb + " output=" + output);
				check(helper, output.is(TRContent.Cells.WATER.asItem()) && output.getCount() == 3 && input.getCount() == 13, "Tank has not filled 3 cells yet: " + output);
			});
		});
	}

	/** A tank unit drains a stack of filled cells into its tank and puts the empty cells into its output slot. */
	public static void tankUnitDrainsCells(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.TankUnit.BASIC.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity unit = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			unit.setItem(0, new ItemStack(TRContent.Cells.LAVA, 5));
			helper.succeedWhen(() -> {
				ItemStack input = unit.getItem(0);
				ItemStack output = unit.getItem(1);
				long tankMb = unit.getTank().getAmount() / 81;
				int full = input.is(TRContent.Cells.LAVA.asItem()) ? input.getCount() : 0;
				check(helper, input.getCount() + output.getCount() == 5, "Cells lost or duplicated: " + input + " / " + output);
				check(helper, tankMb + 1000L * full == 5000, "Fluid not conserved: tank=" + tankMb + " cells=" + full);
				check(helper, tankMb == 5000 && output.is(TRContent.Cells.EMPTY.asItem()), "Tank has not drained all cells yet: " + tankMb);
			});
		});
	}

	/** Emptying cells into a non-empty tank unit by hand adds to the stored fluid. */
	public static void tankUnitHandFillAdds(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.TankUnit.BASIC.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity unit = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			unit.getTank().setFluidInstance(new FluidInstance(Fluids.WATER, FluidValue.BUCKET.multiply(2)));
			Player player = helper.makeMockPlayer(GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TRContent.Cells.WATER, 2));
			BlockPos abs = helper.absolutePos(MACHINE);
			helper.getBlockState(MACHINE).useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
			check(helper, unit.getTank().getAmount() == FluidValue.BUCKET.multiply(4).getRawValue(), "Tank amount after hand fill: " + unit.getTank().getAmount());
			ItemStack returned = player.getMainHandItem();
			check(helper, returned.is(TRContent.Cells.EMPTY.asItem()) && returned.getCount() == 2, "Empty cells not returned: " + returned);
			helper.succeed();
		});
	}

	/** Charging a machine from a battery in its slot moves energy without losing or creating any. */
	public static void batteryChargeConservesEnergy(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.Machine.LOW_VOLTAGE_SU.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			EnergyStorageBlockEntity su = helper.getBlockEntity(MACHINE, EnergyStorageBlockEntity.class);
			ItemStack battery = new ItemStack(TRContent.RED_CELL_BATTERY);
			SimpleEnergyItem item = (SimpleEnergyItem) battery.getItem();
			long initial = item.getEnergyCapacity(battery);
			item.setStoredEnergy(battery, initial);
			su.setItem(1, battery);
			helper.succeedWhen(() -> {
				ItemStack inSlot = su.getItem(1);
				check(helper, inSlot.is(TRContent.RED_CELL_BATTERY), "Battery disappeared from its slot: " + inSlot);
				long total = su.getStored() + item.getStoredEnergy(inSlot);
				check(helper, total == initial, "Energy not conserved: SU=" + su.getStored() + " battery=" + item.getStoredEnergy(inSlot));
				check(helper, su.getStored() >= 100, "SU has not been charged yet: " + su.getStored());
			});
		});
	}

	/** Creative units expose no automation access (capabilities or vanilla worldly-container faces) unless configured. */
	public static void creativeUnitsBlockAutomation(GameTestHelper helper) {
		BlockPos tankPos = MACHINE.east();
		helper.setBlock(MACHINE, TRContent.StorageUnit.CREATIVE.block);
		helper.setBlock(tankPos, TRContent.TankUnit.CREATIVE.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			StorageUnitBaseBlockEntity unit = helper.getBlockEntity(MACHINE, StorageUnitBaseBlockEntity.class);
			setSlotIo(unit, StorageUnitBaseBlockEntity.OUTPUT_SLOT, Direction.DOWN, SlotConfiguration.ExtractConfig.OUTPUT);
			boolean previous = TechRebornConfig.creativeUnitsBlockAutomation;
			try {
				TechRebornConfig.creativeUnitsBlockAutomation = true;
				check(helper, itemCap(helper, MACHINE, Direction.DOWN) == null, "Creative storage unit exposes an item capability");
				check(helper, unit.getSlotsForFace(Direction.DOWN).length == 0, "Creative storage unit exposes hopper slots");
				check(helper, fluidCap(helper, tankPos) == null, "Creative tank unit exposes a fluid capability");
				TechRebornConfig.creativeUnitsBlockAutomation = false;
				check(helper, itemCap(helper, MACHINE, Direction.DOWN) != null && unit.getSlotsForFace(Direction.DOWN).length > 0, "Automation still blocked with the option disabled");
				check(helper, fluidCap(helper, tankPos) != null, "Tank automation still blocked with the option disabled");
			} finally {
				TechRebornConfig.creativeUnitsBlockAutomation = previous;
			}
			helper.succeed();
		});
	}

	/** Machine configuration packets are limited to the owner (placer) or operators; ownerless machines are op-only by default. */
	public static void machineConfigPermissions(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.Machine.ELECTRIC_FURNACE.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity machine = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
			Player other = helper.makeMockPlayer(GameType.SURVIVAL);
			check(helper, !owner.getUUID().equals(other.getUUID()), "Mock players share a UUID");

			check(helper, machine.getOwner() == null && !MachinePermissions.canConfigure(owner, machine), "Ownerless machine configurable by a non-op");
			boolean previous = RebornCoreConfig.allowOwnerlessMachineConfig;
			try {
				RebornCoreConfig.allowOwnerlessMachineConfig = true;
				check(helper, MachinePermissions.canConfigure(other, machine), "allowOwnerlessMachineConfig not honoured");
			} finally {
				RebornCoreConfig.allowOwnerlessMachineConfig = previous;
			}

			BlockState state = helper.getBlockState(MACHINE);
			state.getBlock().setPlacedBy(helper.getLevel(), helper.absolutePos(MACHINE), state, owner, new ItemStack(state.getBlock()));
			check(helper, owner.getUUID().equals(machine.getOwner()), "Placer not recorded as owner");
			check(helper, MachinePermissions.canConfigure(owner, machine), "Owner cannot configure their machine");
			check(helper, !MachinePermissions.canConfigure(other, machine), "Another player can configure the machine");

			CompoundTag saved = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
			check(helper, saved.contains("owner"), "Owner not saved");
			helper.succeed();
		});
	}

	/** With the bucket mixin, buckets can be emptied into and filled from a tank unit by hand. */
	public static void tankUnitHandBucket(GameTestHelper helper) {
		helper.setBlock(MACHINE, TRContent.TankUnit.BASIC.block);
		helper.runAfterDelay(SETUP_DELAY, () -> {
			MachineBaseBlockEntity unit = helper.getBlockEntity(MACHINE, MachineBaseBlockEntity.class);
			unit.getTank().setFluidInstance(new FluidInstance(Fluids.WATER, FluidValue.BUCKET.multiply(2)));
			Player player = helper.makeMockPlayer(GameType.SURVIVAL);
			BlockPos abs = helper.absolutePos(MACHINE);
			BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);

			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
			helper.getBlockState(MACHINE).useWithoutItem(helper.getLevel(), player, hit);
			check(helper, unit.getTank().getAmount() == FluidValue.BUCKET.multiply(3).getRawValue(), "Bucket not emptied into tank: " + unit.getTank().getAmount());
			check(helper, player.getMainHandItem().is(Items.BUCKET), "Empty bucket not returned: " + player.getMainHandItem());

			helper.getBlockState(MACHINE).useWithoutItem(helper.getLevel(), player, hit);
			check(helper, unit.getTank().getAmount() == FluidValue.BUCKET.multiply(2).getRawValue(), "Bucket not filled from tank: " + unit.getTank().getAmount());
			check(helper, player.getMainHandItem().is(Items.WATER_BUCKET), "Water bucket not returned: " + player.getMainHandItem());
			helper.succeed();
		});
	}

	private static ResourceHandler<ItemResource> itemCap(GameTestHelper helper, BlockPos pos, Direction side) {
		return helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), side);
	}

	private static ResourceHandler<FluidResource> fluidCap(GameTestHelper helper, BlockPos pos) {
		return helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(pos), Direction.UP);
	}

	private static void setSlotIo(MachineBaseBlockEntity machine, int slot, Direction side, SlotConfiguration.ExtractConfig io) {
		machine.getSlotConfiguration().getSlotDetails(slot)
			.updateSlotConfig(new SlotConfiguration.SlotConfig(side, new SlotConfiguration.SlotIO(io), slot));
	}

	private static void check(GameTestHelper helper, boolean condition, String message) {
		if (!condition) {
			helper.fail(message);
		}
	}
}
