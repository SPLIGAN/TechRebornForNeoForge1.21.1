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

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes a legacy {@link IFluidHandler} as a {@link ResourceHandler} for NeoForge 26.1 capability registration.
 */
@SuppressWarnings("removal")
public final class LegacyFluidHandlerResourceHandler implements ResourceHandler<FluidResource> {
	private final IFluidHandler handler;

	public LegacyFluidHandlerResourceHandler(IFluidHandler handler) {
		this.handler = handler;
	}

	@Override
	public int size() {
		return handler.getTanks();
	}

	@Override
	public FluidResource getResource(int index) {
		return FluidResource.of(handler.getFluidInTank(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getFluidInTank(index).getAmount();
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		if (!resource.isEmpty() && !handler.isFluidValid(index, resource.toStack(1))) {
			return 0;
		}
		return handler.getTankCapacity(index);
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return !resource.isEmpty() && handler.isFluidValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0 || index < 0 || index >= handler.getTanks()) {
			return 0;
		}
		return handler.fill(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
	}

	@Override
	public int insert(FluidResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		return handler.fill(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0 || index < 0 || index >= handler.getTanks()) {
			return 0;
		}
		FluidStack drained = handler.drain(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
		return drained.getAmount();
	}

	@Override
	public int extract(FluidResource resource, int amount, TransactionContext transaction) {
		if (resource.isEmpty() || amount <= 0) {
			return 0;
		}
		FluidStack drained = handler.drain(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
		return drained.getAmount();
	}
}
