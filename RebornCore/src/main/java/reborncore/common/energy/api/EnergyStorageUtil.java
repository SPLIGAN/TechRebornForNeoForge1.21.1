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

package reborncore.common.energy.api;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import reborncore.common.transfer.RcTransaction;
import reborncore.common.transfer.RcTransactionContext;
import reborncore.common.transfer.RcStoragePreconditions;
import reborncore.common.energy.api.base.SimpleEnergyItem;
import reborncore.common.energy.capability.TeamRebornEnergyCapabilities;

@SuppressWarnings({"unused"})
public class EnergyStorageUtil {
	public static long move(@Nullable EnergyStorage from, @Nullable EnergyStorage to, long maxAmount, @Nullable RcTransactionContext transaction) {
		if (from == null || to == null) {
			return 0;
		}

		RcStoragePreconditions.notNegative(maxAmount);

		long maxExtracted;

		try (RcTransaction extractionTestTransaction = RcTransaction.openNested(transaction)) {
			maxExtracted = from.extract(maxAmount, extractionTestTransaction);
		}

		try (RcTransaction moveTransaction = RcTransaction.openNested(transaction)) {
			long accepted = to.insert(maxExtracted, moveTransaction);

			if (from.extract(accepted, moveTransaction) == accepted) {
				moveTransaction.commit();
				return accepted;
			}
		}

		return 0;
	}

	public static boolean isEnergyStorage(ItemStack stack) {
		return stack.getItem() instanceof SimpleEnergyItem || TeamRebornEnergyCapabilities.findItem(stack) != null;
	}

	private EnergyStorageUtil() {
	}
}
