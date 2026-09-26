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

import net.minecraft.world.Container;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Item access for a machine's fluid container slots: containers are taken from the input slot and the resulting
 * (filled or emptied) containers are only ever placed in the output slot. Transactional through
 * {@link VanillaContainerWrapper}.
 */
public final class MachineSlotsItemAccess implements ItemAccess {
	private final ResourceHandler<ItemResource> slots;
	private final int inputSlot;
	private final int outputSlot;

	public MachineSlotsItemAccess(Container inventory, int inputSlot, int outputSlot) {
		this.slots = VanillaContainerWrapper.of(inventory);
		this.inputSlot = inputSlot;
		this.outputSlot = outputSlot;
	}

	@Override
	public ItemResource getResource() {
		return slots.getResource(inputSlot);
	}

	@Override
	public int getAmount() {
		return slots.getAmountAsInt(inputSlot);
	}

	@Override
	public int insert(ItemResource resource, int amount, TransactionContext transaction) {
		return slots.insert(outputSlot, resource, amount, transaction);
	}

	@Override
	public int extract(ItemResource resource, int amount, TransactionContext transaction) {
		return slots.extract(inputSlot, resource, amount, transaction);
	}
}
