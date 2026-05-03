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

package reborncore.common.transfer;

import net.minecraft.world.item.ItemStack;
import reborncore.common.compat.TransferApiBridge;

public class RcSingleStackItemStorage extends RcSnapshotParticipant<ItemStack> implements RcStorage<RcItemVariant> {
	private final TransferApiBridge.SingleStackHooks hooks;

	public RcSingleStackItemStorage(TransferApiBridge.SingleStackHooks hooks) {
		this.hooks = hooks;
	}

	@Override
	public RcItemVariant getResource() {
		return RcItemVariant.of(hooks.getStack());
	}

	@Override
	public long getAmount() {
		return hooks.getStack().getCount();
	}

	@Override
	public boolean isResourceBlank() {
		return hooks.getStack().isEmpty();
	}

	@Override
	public long insert(RcItemVariant variant, long maxAmount, RcTransactionContext transaction) {
		RcStoragePreconditions.notBlankNotNegative(variant, maxAmount);
		if (!hooks.canInsert(variant)) {
			return 0;
		}
		ItemStack current = hooks.getStack();
		long insertedLong;

		if (current.isEmpty()) {
			insertedLong = Math.min(maxAmount, hooks.getCapacity(variant));
		} else if (variant.matchesStack(current)) {
			insertedLong = Math.min(maxAmount, (long) hooks.getCapacity(variant) - current.getCount());
		} else {
			return 0;
		}

		int inserted = insertedLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) insertedLong;
		if (inserted <= 0) {
			return 0;
		}

		updateSnapshots(transaction);
		if (current.isEmpty()) {
			ItemStack insertedStack = variant.toStack(inserted);
			hooks.setStack(insertedStack);
		} else {
			current.grow(inserted);
			hooks.setStack(current);
		}
		transaction.addCloseCallback(committed -> {
			if (committed) {
				hooks.onFinalCommit();
			}
		});
		return inserted;
	}

	@Override
	protected ItemStack createSnapshot() {
		return hooks.getStack().copy();
	}

	@Override
	protected void readSnapshot(ItemStack snapshot) {
		hooks.setStack(snapshot.isEmpty() ? ItemStack.EMPTY : snapshot);
	}

	@Override
	public long extract(RcItemVariant variant, long maxAmount, RcTransactionContext transaction) {
		RcStoragePreconditions.notBlankNotNegative(variant, maxAmount);
		if (!hooks.canExtract(variant)) {
			return 0;
		}
		ItemStack current = hooks.getStack();
		if (current.isEmpty() || !variant.matchesStack(current)) {
			return 0;
		}

		int extracted = (int) Math.min(maxAmount, current.getCount());
		if (extracted <= 0) {
			return 0;
		}

		updateSnapshots(transaction);
		current.shrink(extracted);
		hooks.setStack(current.isEmpty() ? ItemStack.EMPTY : current);
		transaction.addCloseCallback(committed -> {
			if (committed) {
				hooks.onFinalCommit();
			}
		});
		return extracted;
	}
}
