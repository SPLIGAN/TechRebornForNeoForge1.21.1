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
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.transfer.RcNeoTransactionBridge;
import reborncore.common.transfer.RcStoragePreconditions;
import reborncore.common.transfer.RcTransactionContext;

/**
 * Exposes a NeoForge {@link EnergyHandler} (FE) as a RebornCore {@link EnergyStorage}, 1 FE = 1 E.
 * Used as a fallback so that Tech Reborn machines and cables can push into / pull from FE-only neighbours.
 */
public record EnergyHandlerEnergyStorage(EnergyHandler handler) implements EnergyStorage {
	@Override
	public long insert(long maxAmount, @Nullable RcTransactionContext transaction) {
		RcStoragePreconditions.notNegative(maxAmount);
		return transfer(maxAmount, transaction, handler::insert);
	}

	@Override
	public long extract(long maxAmount, @Nullable RcTransactionContext transaction) {
		RcStoragePreconditions.notNegative(maxAmount);
		return transfer(maxAmount, transaction, handler::extract);
	}

	private static long transfer(long maxAmount, @Nullable RcTransactionContext transaction, Operation operation) {
		int amount = Ints.saturatedCast(maxAmount);
		if (amount == 0) {
			return 0;
		}
		if (transaction != null) {
			// Stays open until the RebornCore transaction closes.
			return operation.apply(amount, RcNeoTransactionBridge.openNeoBoundTo(transaction));
		}
		try (Transaction neoTransaction = RcNeoTransactionBridge.openNeoBoundTo(null)) {
			int moved = operation.apply(amount, neoTransaction);
			neoTransaction.commit();
			return moved;
		}
	}

	@FunctionalInterface
	private interface Operation {
		int apply(int amount, Transaction transaction);
	}

	@Override
	public long getAmount() {
		return handler.getAmountAsLong();
	}

	@Override
	public long getCapacity() {
		return handler.getCapacityAsLong();
	}
}
