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

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class RcItemVariant {
	private final ItemStack prototype;

	private RcItemVariant(ItemStack prototype) {
		this.prototype = prototype.isEmpty() ? ItemStack.EMPTY : prototype.copyWithCount(1);
	}

	public static RcItemVariant of(ItemStack stack) {
		return new RcItemVariant(stack);
	}

	public static RcItemVariant of(Item item) {
		return new RcItemVariant(new ItemStack(item));
	}

	public ItemStack prototype() {
		return prototype;
	}

	public boolean isBlank() {
		return prototype.isEmpty();
	}

	public ItemStack toStack(long count) {
		if (prototype.isEmpty() || count <= 0) {
			return ItemStack.EMPTY;
		}
		ItemStack copy = prototype.copy();
		copy.setCount((int) Math.min(count, Integer.MAX_VALUE));
		return copy;
	}

	public Item item() {
		return prototype.getItem();
	}

	public boolean matchesStack(ItemStack stack) {
		return ItemStack.isSameItemSameComponents(prototype, stack);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof RcItemVariant other && ItemStack.isSameItemSameComponents(prototype, other.prototype);
	}

	@Override
	public int hashCode() {
		return Objects.hash(prototype.getItem(), prototype.getComponentsPatch());
	}
}
