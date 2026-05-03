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

import java.util.ArrayList;
import java.util.List;

public final class RcCombinedItemStorage implements RcStorage<RcItemVariant> {
	private final List<RcStorage<RcItemVariant>> parts;

	public RcCombinedItemStorage(List<RcStorage<RcItemVariant>> parts) {
		this.parts = List.copyOf(parts);
	}

	@Override
	public RcItemVariant getResource() {
		for (RcStorage<RcItemVariant> part : parts) {
			if (!part.isResourceBlank()) {
				return part.getResource();
			}
		}
		return RcItemVariant.of(ItemStack.EMPTY);
	}

	@Override
	public long getAmount() {
		long sum = 0;
		for (RcStorage<RcItemVariant> part : parts) {
			sum += part.getAmount();
		}
		return sum;
	}

	@Override
	public boolean isResourceBlank() {
		for (RcStorage<RcItemVariant> part : parts) {
			if (!part.isResourceBlank()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public long insert(RcItemVariant variant, long maxAmount, RcTransactionContext tx) {
		long inserted = 0;
		for (RcStorage<RcItemVariant> part : parts) {
			inserted += part.insert(variant, maxAmount - inserted, tx);
			if (inserted >= maxAmount) {
				break;
			}
		}
		return inserted;
	}

	@Override
	public long extract(RcItemVariant variant, long maxAmount, RcTransactionContext tx) {
		long extracted = 0;
		for (RcStorage<RcItemVariant> part : parts) {
			extracted += part.extract(variant, maxAmount - extracted, tx);
			if (extracted >= maxAmount) {
				break;
			}
		}
		return extracted;
	}

	@Override
	public Iterable<RcStorageView<RcItemVariant>> views() {
		List<RcStorageView<RcItemVariant>> list = new ArrayList<>();
		for (RcStorage<RcItemVariant> part : parts) {
			for (RcStorageView<RcItemVariant> view : part.views()) {
				list.add(view);
			}
		}
		return list;
	}
}
