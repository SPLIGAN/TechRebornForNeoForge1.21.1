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

package reborncore.common.energy.api.base;

import net.minecraft.world.item.ItemStack;
import reborncore.common.transfer.RcItemVariant;
import reborncore.common.energy.api.EnergyItemContext;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.impl.SimpleItemEnergyStorageImpl;

public interface SimpleEnergyItem {

	static EnergyStorage createStorage(EnergyItemContext ctx, long capacity, long maxInsert, long maxExtract) {
		return SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, capacity, maxInsert, maxExtract);
	}

	long getEnergyCapacity(ItemStack stack);

	long getEnergyMaxInput(ItemStack stack);

	long getEnergyMaxOutput(ItemStack stack);

	default long getStoredEnergy(ItemStack stack) {
		return getStoredEnergyUnchecked(stack);
	}

	default void setStoredEnergy(ItemStack stack, long newAmount) {
		setStoredEnergyUnchecked(stack, newAmount);
	}

	default boolean tryUseEnergy(ItemStack stack, long amount) {
		if (stack.getCount() != 1) {
			throw new IllegalArgumentException("Invalid count: " + stack.getCount());
		}

		long newAmount = getStoredEnergy(stack) - amount;

		if (newAmount < 0) {
			return false;
		} else {
			setStoredEnergy(stack, newAmount);
			return true;
		}
	}

	static long getStoredEnergyUnchecked(ItemStack stack) {
		return stack.getOrDefault(EnergyStorage.ENERGY_COMPONENT, 0L);
	}

	static long getStoredEnergyUnchecked(RcItemVariant variant) {
		return variant.prototype().getOrDefault(EnergyStorage.ENERGY_COMPONENT, 0L);
	}

	static void setStoredEnergyUnchecked(ItemStack stack, long newAmount) {
		if (newAmount <= 0) {
			stack.remove(EnergyStorage.ENERGY_COMPONENT);
		} else {
			stack.set(EnergyStorage.ENERGY_COMPONENT, newAmount);
		}
	}
}
