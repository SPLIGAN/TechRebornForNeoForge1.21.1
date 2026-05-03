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
import reborncore.common.util.Tank;

public final class TankFluidHandler implements IFluidHandler {
	private final Tank tank;

	public TankFluidHandler(Tank tank) {
		this.tank = tank;
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tankIndex) {
		if (tankIndex != 0) {
			return FluidStack.EMPTY;
		}
		if (tank.isEmpty()) {
			return FluidStack.EMPTY;
		}
		int mb = RcFluidAmounts.toMilliBucketsClamped(tank.getFluidAmount().getRawValue());
		return new FluidStack(tank.getFluid(), mb);
	}

	@Override
	public int getTankCapacity(int tankIndex) {
		return tankIndex != 0 ? 0 : RcFluidAmounts.toMilliBucketsClamped(tank.getFluidValueCapacity().getRawValue());
	}

	@Override
	public boolean isFluidValid(int tankIndex, FluidStack stack) {
		return tankIndex == 0 && !stack.isEmpty() && (tank.isEmpty() || tank.getFluid() == stack.getFluid());
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty()) {
			return 0;
		}
		RcFluidVariant variant = RcFluidVariant.of(resource.getFluid());
		long droplets = RcFluidAmounts.dropletsFromMilliBuckets(resource.getAmount());
		try (RcTransaction tx = RcTransaction.openOuter()) {
			long inserted = tank.insert(variant, droplets, tx);
			if (action.execute()) {
				tx.commit();
			}
			return RcFluidAmounts.toMilliBucketsClamped(inserted);
		}
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty()) {
			return FluidStack.EMPTY;
		}
		RcFluidVariant variant = RcFluidVariant.of(resource.getFluid());
		long want = RcFluidAmounts.dropletsFromMilliBuckets(resource.getAmount());
		try (RcTransaction tx = RcTransaction.openOuter()) {
			long extracted = tank.extract(variant, want, tx);
			if (action.execute()) {
				tx.commit();
			}
			if (extracted <= 0) {
				return FluidStack.EMPTY;
			}
			int mb = RcFluidAmounts.toMilliBucketsClamped(extracted);
			return new FluidStack(resource.getFluid(), mb);
		}
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (maxDrain <= 0 || tank.isEmpty()) {
			return FluidStack.EMPTY;
		}
		RcFluidVariant variant = RcFluidVariant.of(tank.getFluid());
		long want = RcFluidAmounts.dropletsFromMilliBuckets(maxDrain);
		try (RcTransaction tx = RcTransaction.openOuter()) {
			long extracted = tank.extract(variant, want, tx);
			if (action.execute()) {
				tx.commit();
			}
			if (extracted <= 0) {
				return FluidStack.EMPTY;
			}
			int mb = RcFluidAmounts.toMilliBucketsClamped(extracted);
			return new FluidStack(tank.getFluid(), mb);
		}
	}
}
