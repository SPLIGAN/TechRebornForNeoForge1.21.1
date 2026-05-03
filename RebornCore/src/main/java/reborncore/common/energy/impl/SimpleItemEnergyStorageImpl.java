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

package reborncore.common.energy.impl;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import reborncore.common.transfer.RcItemVariant;
import reborncore.common.transfer.RcTransaction;
import reborncore.common.transfer.RcTransactionContext;
import reborncore.common.transfer.RcStoragePreconditions;
import reborncore.common.energy.api.EnergyItemContext;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.api.base.DelegatingEnergyStorage;
import reborncore.common.energy.api.base.SimpleEnergyItem;

@ApiStatus.Internal
public class SimpleItemEnergyStorageImpl implements EnergyStorage {
	public static EnergyStorage createSimpleStorage(EnergyItemContext ctx, long capacity, long maxInsert, long maxExtract) {
		RcStoragePreconditions.notNegative(capacity);
		RcStoragePreconditions.notNegative(maxInsert);
		RcStoragePreconditions.notNegative(maxExtract);

		Item startingItem = ctx.getItemVariant().item();

		return new DelegatingEnergyStorage(
			new SimpleItemEnergyStorageImpl(ctx, capacity, maxInsert, maxExtract),
			() -> ctx.getItemVariant().item() == startingItem && ctx.getAmount() > 0
		);
	}

	private final EnergyItemContext ctx;
	private final long capacity;
	private final long maxInsert, maxExtract;

	private SimpleItemEnergyStorageImpl(EnergyItemContext ctx, long capacity, long maxInsert, long maxExtract) {
		this.ctx = ctx;
		this.capacity = capacity;
		this.maxInsert = maxInsert;
		this.maxExtract = maxExtract;
	}

	private boolean trySetEnergy(long energyAmountPerCount, long count, RcTransactionContext transaction) {
		ItemStack newStack = ctx.getItemVariant().toStack(1);
		SimpleEnergyItem.setStoredEnergyUnchecked(newStack, energyAmountPerCount);
		RcItemVariant newVariant = RcItemVariant.of(newStack);

		try (RcTransaction nested = transaction.openNested()) {
			if (ctx.extract(ctx.getItemVariant(), count, nested) == count && ctx.insert(newVariant, count, nested) == count) {
				nested.commit();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean supportsInsertion() {
		return maxInsert > 0;
	}

	@Override
	public long insert(long maxAmount, RcTransactionContext transaction) {
		long count = ctx.getAmount();

		long maxAmountPerCount = maxAmount / count;
		long currentAmountPerCount = getAmount() / count;
		long insertedPerCount = Math.min(maxInsert, Math.min(maxAmountPerCount, capacity - currentAmountPerCount));

		if (insertedPerCount > 0) {
			if (trySetEnergy(currentAmountPerCount + insertedPerCount, count, transaction)) {
				return insertedPerCount * count;
			}
		}

		return 0;
	}

	@Override
	public boolean supportsExtraction() {
		return maxExtract > 0;
	}

	@Override
	public long extract(long maxAmount, RcTransactionContext transaction) {
		long count = ctx.getAmount();

		long maxAmountPerCount = maxAmount / count;
		long currentAmountPerCount = getAmount() / count;
		long extractedPerCount = Math.min(maxExtract, Math.min(maxAmountPerCount, currentAmountPerCount));

		if (extractedPerCount > 0) {
			if (trySetEnergy(currentAmountPerCount - extractedPerCount, count, transaction)) {
				return extractedPerCount * count;
			}
		}

		return 0;
	}

	@Override
	public long getAmount() {
		return ctx.getAmount() * SimpleEnergyItem.getStoredEnergyUnchecked(ctx.getItemVariant());
	}

	@Override
	public long getCapacity() {
		return ctx.getAmount() * capacity;
	}
}
