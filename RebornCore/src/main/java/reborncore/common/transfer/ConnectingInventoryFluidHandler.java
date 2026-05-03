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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.Optional;

/**
 * Fluid handler bridging machine fluid slots backed by a {@link Container}.
 */
public final class ConnectingInventoryFluidHandler implements IFluidHandler {
	private final Container inventory;
	private final int inputSlot;

	public ConnectingInventoryFluidHandler(Container inventory, int inputSlot, int outputSlot) {
		this.inventory = inventory;
		this.inputSlot = inputSlot;
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		if (tank != 0) {
			return FluidStack.EMPTY;
		}
		ItemStack stack = inventory.getItem(inputSlot);
		return FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
	}

	@Override
	public int getTankCapacity(int tank) {
		return tank == 0 ? Integer.MAX_VALUE : 0;
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return tank == 0;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty()) {
			return 0;
		}
		ItemStack stack = inventory.getItem(inputSlot);
		if (stack.isEmpty()) {
			return 0;
		}
		Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack.copyWithCount(1));
		if (cap.isEmpty()) {
			return 0;
		}
		IFluidHandlerItem handlerItem = cap.get();
		int filled = handlerItem.fill(resource, action);
		if (filled > 0 && action.execute()) {
			inventory.setItem(inputSlot, handlerItem.getContainer());
		}
		return filled;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty()) {
			return FluidStack.EMPTY;
		}
		ItemStack stack = inventory.getItem(inputSlot);
		if (stack.isEmpty()) {
			return FluidStack.EMPTY;
		}
		Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack.copyWithCount(1));
		if (cap.isEmpty()) {
			return FluidStack.EMPTY;
		}
		IFluidHandlerItem handlerItem = cap.get();
		FluidStack drained = handlerItem.drain(resource, action);
		if (!drained.isEmpty() && action.execute()) {
			inventory.setItem(inputSlot, handlerItem.getContainer());
		}
		return drained;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		ItemStack stack = inventory.getItem(inputSlot);
		if (stack.isEmpty()) {
			return FluidStack.EMPTY;
		}
		Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack.copyWithCount(1));
		if (cap.isEmpty()) {
			return FluidStack.EMPTY;
		}
		IFluidHandlerItem handlerItem = cap.get();
		FluidStack drained = handlerItem.drain(maxDrain, action);
		if (!drained.isEmpty() && action.execute()) {
			inventory.setItem(inputSlot, handlerItem.getContainer());
		}
		return drained;
	}
}
