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

public record RcFluidHandlerBackedStorage(IFluidHandler handler) implements RcStorage<RcFluidVariant> {

	@Override
	public RcFluidVariant getResource() {
		for (int i = 0; i < handler.getTanks(); i++) {
			FluidStack fs = handler.getFluidInTank(i);
			if (!fs.isEmpty()) {
				return RcFluidVariant.of(fs.getFluid());
			}
		}
		return RcFluidVariant.blank();
	}

	@Override
	public long getAmount() {
		long sum = 0;
		for (int i = 0; i < handler.getTanks(); i++) {
			sum += RcFluidAmounts.dropletsFromFluidStack(handler.getFluidInTank(i));
		}
		return sum;
	}

	@Override
	public boolean isResourceBlank() {
		return getAmount() == 0;
	}

	@Override
	public long insert(RcFluidVariant resource, long maxAmount, RcTransactionContext tx) {
		if (resource.isBlank() || maxAmount <= 0) {
			return 0;
		}
		FluidStack stack = new FluidStack(resource.fluid(), RcFluidAmounts.toMilliBucketsClamped(maxAmount));
		int filled = handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
		return RcFluidAmounts.dropletsFromMilliBuckets(filled);
	}

	@Override
	public long extract(RcFluidVariant resource, long maxAmount, RcTransactionContext tx) {
		if (resource.isBlank() || maxAmount <= 0) {
			return 0;
		}
		FluidStack stack = new FluidStack(resource.fluid(), RcFluidAmounts.toMilliBucketsClamped(maxAmount));
		FluidStack drained = handler.drain(stack, IFluidHandler.FluidAction.EXECUTE);
		return RcFluidAmounts.dropletsFromFluidStack(drained);
	}
}
