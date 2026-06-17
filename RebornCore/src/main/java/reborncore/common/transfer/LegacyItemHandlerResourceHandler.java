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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes a legacy {@link IItemHandler} as a {@link ResourceHandler} for NeoForge 26.1 capability registration.
 */
@SuppressWarnings("removal")
public final class LegacyItemHandlerResourceHandler implements ResourceHandler<ItemResource> {
	private final IItemHandler handler;

	public LegacyItemHandlerResourceHandler(IItemHandler handler) {
		this.handler = handler;
	}

	@Override
	public int size() {
		return handler.getSlots();
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(handler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getStackInSlot(index).getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		if (!resource.isEmpty() && !handler.isItemValid(index, resource.toStack(1))) {
			return 0;
		}
		return handler.getSlotLimit(index);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return !resource.isEmpty() && handler.isItemValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		ItemStack remaining = handler.insertItem(index, resource.toStack(amount), false);
		return amount - remaining.getCount();
	}

	@Override
	public int insert(ItemResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		int inserted = 0;
		for (int slot = 0; slot < handler.getSlots() && inserted < amount; slot++) {
			inserted += insert(slot, resource, amount - inserted, transaction);
		}
		return inserted;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		ItemStack extracted = handler.extractItem(index, amount, false);
		if (extracted.isEmpty() || !resource.matches(extracted)) {
			return 0;
		}
		return extracted.getCount();
	}

	@Override
	public int extract(ItemResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		int extracted = 0;
		for (int slot = 0; slot < handler.getSlots() && extracted < amount; slot++) {
			extracted += extract(slot, resource, amount - extracted, transaction);
		}
		return extracted;
	}
}
