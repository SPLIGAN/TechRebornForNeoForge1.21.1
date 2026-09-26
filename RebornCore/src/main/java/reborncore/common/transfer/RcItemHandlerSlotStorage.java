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

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * One index of a NeoForge item {@link ResourceHandler} as an {@link RcStorage}. Operations join the RebornCore
 * transaction (see {@link RcNeoTransactionBridge#openNeoBoundTo}), so simulations and aborted moves are rolled back.
 */
public final class RcItemHandlerSlotStorage implements RcStorage<RcItemVariant> {
	private final ResourceHandler<ItemResource> handler;
	private final int slot;

	public RcItemHandlerSlotStorage(ResourceHandler<ItemResource> handler, int slot) {
		this.handler = handler;
		this.slot = slot;
	}

	public ResourceHandler<ItemResource> handler() {
		return handler;
	}

	public int slot() {
		return slot;
	}

	@Override
	public RcItemVariant getResource() {
		return RcItemVariant.of(handler.getResource(slot).toStack());
	}

	@Override
	public long getAmount() {
		return handler.getAmountAsLong(slot);
	}

	@Override
	public boolean isResourceBlank() {
		return handler.getResource(slot).isEmpty();
	}

	@Override
	public long insert(RcItemVariant variant, long maxAmount, @Nullable RcTransactionContext tx) {
		if (variant.isBlank() || maxAmount <= 0) {
			return 0;
		}
		ItemResource resource = ItemResource.of(variant.toStack(1));
		int amount = Ints.saturatedCast(maxAmount);
		if (tx != null) {
			return handler.insert(slot, resource, amount, RcNeoTransactionBridge.openNeoBoundTo(tx));
		}
		try (Transaction transaction = RcNeoTransactionBridge.openNeoBoundTo(null)) {
			int inserted = handler.insert(slot, resource, amount, transaction);
			transaction.commit();
			return inserted;
		}
	}

	@Override
	public long extract(RcItemVariant variant, long maxAmount, @Nullable RcTransactionContext tx) {
		if (variant.isBlank() || maxAmount <= 0) {
			return 0;
		}
		ItemResource resource = ItemResource.of(variant.toStack(1));
		int amount = Ints.saturatedCast(maxAmount);
		if (tx != null) {
			return handler.extract(slot, resource, amount, RcNeoTransactionBridge.openNeoBoundTo(tx));
		}
		try (Transaction transaction = RcNeoTransactionBridge.openNeoBoundTo(null)) {
			int extracted = handler.extract(slot, resource, amount, transaction);
			transaction.commit();
			return extracted;
		}
	}
}
