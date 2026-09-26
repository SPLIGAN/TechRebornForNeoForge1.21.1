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
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.blockentity.SlotConfiguration;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.api.EnergyStorageUtil;
import reborncore.common.energy.api.base.SimpleEnergyItem;
import reborncore.common.energy.capability.EnergyHandlerEnergyStorage;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reborncore.common.transfer.RcTransaction;
import techreborn.blockentity.storage.energy.EnergyStorageBlockEntity;
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
