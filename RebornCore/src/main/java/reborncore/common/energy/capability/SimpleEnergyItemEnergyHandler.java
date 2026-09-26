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

package reborncore.common.energy.capability;

import com.google.common.primitives.Ints;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.api.base.SimpleEnergyItem;

/**
 * NeoForge FE view of a {@link SimpleEnergyItem} (batteries, tools, armour), 1 E = 1 FE, honouring the item's
 * per-operation input/output limits. Energy is stored in {@link EnergyStorage#ENERGY_COMPONENT}.
 */
public final class SimpleEnergyItemEnergyHandler implements EnergyHandler {
	private final ItemAccess itemAccess;
	private final SimpleEnergyItem energyItem;
	private final Item validItem;

	public SimpleEnergyItemEnergyHandler(ItemAccess itemAccess, SimpleEnergyItem energyItem) {
		this.itemAccess = itemAccess;
		this.energyItem = energyItem;
		this.validItem = itemAccess.getResource().getItem();
	}

	private static long storedIn(ItemResource resource) {
		return resource.getOrDefault(EnergyStorage.ENERGY_COMPONENT, 0L);
	}

	private static ItemResource withStored(ItemResource resource, long amount) {
		return amount <= 0 ? resource.without(EnergyStorage.ENERGY_COMPONENT) : resource.with(EnergyStorage.ENERGY_COMPONENT, amount);
	}

	private ItemStack stackOf(ItemResource resource) {
		return resource.toStack(1);
	}

	@Override
	public long getAmountAsLong() {
		ItemResource resource = itemAccess.getResource();
		if (!resource.is(validItem)) {
			return 0;
		}
		return itemAccess.getAmount() * storedIn(resource);
	}

	@Override
	public long getCapacityAsLong() {
		ItemResource resource = itemAccess.getResource();
		if (!resource.is(validItem)) {
			return 0;
		}
		return itemAccess.getAmount() * energyItem.getEnergyCapacity(stackOf(resource));
	}

	@Override
	public int insert(int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonNegative(amount);
		int count = itemAccess.getAmount();
		ItemResource resource = itemAccess.getResource();
		if (count == 0 || !resource.is(validItem)) {
			return 0;
		}
		ItemStack stack = stackOf(resource);
		long stored = storedIn(resource);
		long perItem = Math.min(Math.min(amount / count, energyItem.getEnergyMaxInput(stack)), energyItem.getEnergyCapacity(stack) - stored);
		if (perItem <= 0) {
			return 0;
		}
		int exchanged = itemAccess.exchange(withStored(resource, stored + perItem), count, transaction);
		return Ints.saturatedCast(perItem * exchanged);
	}

	@Override
	public int extract(int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonNegative(amount);
		int count = itemAccess.getAmount();
		ItemResource resource = itemAccess.getResource();
		if (count == 0 || !resource.is(validItem)) {
			return 0;
		}
		ItemStack stack = stackOf(resource);
		long stored = storedIn(resource);
		long perItem = Math.min(Math.min(amount / count, energyItem.getEnergyMaxOutput(stack)), stored);
		if (perItem <= 0) {
			return 0;
		}
		int exchanged = itemAccess.exchange(withStored(resource, stored - perItem), count, transaction);
		return Ints.saturatedCast(perItem * exchanged);
	}
}
