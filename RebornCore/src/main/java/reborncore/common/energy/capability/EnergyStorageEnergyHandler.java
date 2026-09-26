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

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.transfer.RcNeoTransactionBridge;

/**
 * Exposes a RebornCore {@link EnergyStorage} as a NeoForge {@link EnergyHandler} (FE), 1 E = 1 FE.
 * Side and I/O limits are those of the wrapped (usually side-specific) storage.
 */
public record EnergyStorageEnergyHandler(EnergyStorage storage) implements EnergyHandler {
	@Override
	public long getAmountAsLong() {
		return storage.getAmount();
	}

	@Override
	public long getCapacityAsLong() {
		return storage.getCapacity();
	}

	@Override
	public int insert(int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonNegative(amount);
		if (amount == 0 || !storage.supportsInsertion()) {
			return 0;
		}
		return (int) storage.insert(amount, RcNeoTransactionBridge.openBoundTo(transaction));
	}

	@Override
	public int extract(int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonNegative(amount);
		if (amount == 0 || !storage.supportsExtraction()) {
			return 0;
		}
		return (int) storage.extract(amount, RcNeoTransactionBridge.openBoundTo(transaction));
	}
}
