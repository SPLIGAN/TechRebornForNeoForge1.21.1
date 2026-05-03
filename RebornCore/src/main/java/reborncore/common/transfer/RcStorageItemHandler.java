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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * NeoForge {@link IItemHandler} view over {@link RcStorage} slots ({@link RcStorage#views()}).
 */
public final class RcStorageItemHandler implements IItemHandler {
	private final List<RcStorageView<RcItemVariant>> slotViews;

	public RcStorageItemHandler(RcStorage<RcItemVariant> storage) {
		List<RcStorageView<RcItemVariant>> list = new ArrayList<>();
		for (RcStorageView<RcItemVariant> view : storage.views()) {
			list.add(view);
		}
		this.slotViews = List.copyOf(list);
	}

	@Override
	public int getSlots() {
		return slotViews.size();
	}

	@Override
	public @NotNull ItemStack getStackInSlot(int slot) {
		RcStorageView<RcItemVariant> view = slotViews.get(slot);
		if (view.isResourceBlank()) {
			return ItemStack.EMPTY;
		}
		long amt = view.getAmount();
		int count = amt > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amt;
		return view.getResource().toStack(count);
	}

	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		RcStorageView<RcItemVariant> view = slotViews.get(slot);
		if (view instanceof RcItemHandlerSlotStorage handlerSlot) {
			return handlerSlot.insertStack(stack, simulate);
		}
		if (view instanceof RcStorage<RcItemVariant> st) {
			try (RcTransaction tx = RcTransaction.openOuter()) {
				long inserted = st.insert(RcItemVariant.of(stack), stack.getCount(), tx);
				if (!simulate && inserted > 0) {
					tx.commit();
				}
				if (inserted <= 0) {
					return stack;
				}
				if (inserted >= stack.getCount()) {
					return ItemStack.EMPTY;
				}
				ItemStack remainder = stack.copy();
				remainder.shrink((int) inserted);
				return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
			}
		}
		return stack;
	}

	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount <= 0) {
			return ItemStack.EMPTY;
		}
		RcStorageView<RcItemVariant> view = slotViews.get(slot);
		if (view.isResourceBlank()) {
			return ItemStack.EMPTY;
		}
		RcItemVariant resource = view.getResource();
		if (view instanceof RcItemHandlerSlotStorage handlerSlot) {
			return handlerSlot.extractStack(amount, simulate);
		}
		if (view instanceof RcStorage<RcItemVariant> st) {
			try (RcTransaction tx = RcTransaction.openOuter()) {
				long ext = st.extract(resource, amount, tx);
				if (!simulate && ext > 0) {
					tx.commit();
				}
				return ext <= 0 ? ItemStack.EMPTY : resource.toStack((int) ext);
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		RcStorageView<RcItemVariant> view = slotViews.get(slot);
		if (view instanceof RcItemHandlerSlotStorage handlerSlot) {
			return handlerSlot.slotLimit();
		}
		RcItemVariant res = view.getResource();
		return res.isBlank() ? 64 : res.prototype().getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return true;
	}
}
