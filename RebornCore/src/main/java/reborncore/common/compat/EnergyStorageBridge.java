/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2026 TeamReborn
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

package reborncore.common.compat;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.api.SlotEnergyContext;
import reborncore.common.energy.api.base.DelegatingEnergyStorage;
import reborncore.common.energy.api.base.SimpleEnergyItem;

import java.util.function.LongSupplier;

public final class EnergyStorageBridge {
	private EnergyStorageBridge() {
	}

	public static EnergyStorage delegatingWithCappedIo(EnergyStorage backing, LongSupplier maxInsert, LongSupplier maxExtract) {
		return new DelegatingEnergyStorage(backing, null) {
			@Override
			public long insert(long maxAmount, reborncore.common.transfer.RcTransactionContext transaction) {
				return backingStorage.get().insert(Math.min(maxAmount, maxInsert.getAsLong()), transaction);
			}

			@Override
			public long extract(long maxAmount, reborncore.common.transfer.RcTransactionContext transaction) {
				return backingStorage.get().extract(Math.min(maxAmount, maxExtract.getAsLong()), transaction);
			}
		};
	}

	public static EnergyStorage itemEnergyStorageInInventorySlot(Container inventory, @Nullable Direction inventoryFacing, int slotIndex) {
		ItemStack stack = inventory.getItem(slotIndex);
		if (!(stack.getItem() instanceof SimpleEnergyItem sei)) {
			return null;
		}
		var slot = TransferApiBridge.inventorySlot(inventory, inventoryFacing, slotIndex);
		return SimpleEnergyItem.createStorage(new SlotEnergyContext(slot), sei.getEnergyCapacity(stack), sei.getEnergyMaxInput(stack), sei.getEnergyMaxOutput(stack));
	}

	public static EnergyStorage itemEnergyStorageFromPlayerSlot(Player player, int inventorySlotIndex) {
		return itemEnergyStorageInInventorySlot(player.getInventory(), null, inventorySlotIndex);
	}
}
