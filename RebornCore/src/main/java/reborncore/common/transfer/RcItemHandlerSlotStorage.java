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
import net.neoforged.neoforge.items.IItemHandler;

public final class RcItemHandlerSlotStorage implements RcStorage<RcItemVariant> {
	private final IItemHandler handler;
	private final int slot;

	public RcItemHandlerSlotStorage(IItemHandler handler, int slot) {
		this.handler = handler;
		this.slot = slot;
	}

	@Override
	public RcItemVariant getResource() {
		return RcItemVariant.of(handler.getStackInSlot(slot));
	}

	@Override
	public long getAmount() {
		return handler.getStackInSlot(slot).getCount();
	}

	@Override
	public boolean isResourceBlank() {
		return handler.getStackInSlot(slot).isEmpty();
	}

	@Override
	public long insert(RcItemVariant variant, long maxAmount, RcTransactionContext tx) {
		if (variant.isBlank() || maxAmount <= 0) {
			return 0;
		}
		ItemStack stack = variant.toStack(Math.min(maxAmount, Integer.MAX_VALUE));
		ItemStack remainder = handler.insertItem(slot, stack, false);
		return stack.getCount() - remainder.getCount();
	}

	@Override
	public long extract(RcItemVariant variant, long maxAmount, RcTransactionContext tx) {
		if (variant.isBlank() || maxAmount <= 0) {
			return 0;
		}
		ItemStack current = handler.getStackInSlot(slot);
		if (current.isEmpty() || !variant.matchesStack(current)) {
			return 0;
		}
		ItemStack extracted = handler.extractItem(slot, (int) Math.min(maxAmount, Integer.MAX_VALUE), false);
		return extracted.getCount();
	}

	public ItemStack insertStack(ItemStack stack, boolean simulate) {
		return handler.insertItem(slot, stack, simulate);
	}

	public ItemStack extractStack(int amount, boolean simulate) {
		return handler.extractItem(slot, amount, simulate);
	}

	public int slotLimit() {
		return handler.getSlotLimit(slot);
	}
}
