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

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import reborncore.common.util.Tank;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Transactional NeoForge fluid handler (millibuckets) over a RebornCore {@link Tank} (droplets).
 * Transfers are rounded down to whole millibuckets so that no fraction is moved without being reported.
 * Fluids carrying data components are rejected because {@link Tank} only stores the plain fluid.
 */
public final class RcTankResourceHandler implements ResourceHandler<FluidResource> {
	private static final long DROPLETS_PER_MB = RcTransferConstants.DROPLETS_PER_BUCKET / 1000;

	private final Supplier<@Nullable Tank> tank;

	public RcTankResourceHandler(Supplier<@Nullable Tank> tank) {
		this.tank = tank;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public FluidResource getResource(int index) {
		Objects.checkIndex(index, size());
		Tank t = tank.get();
		return t == null || t.isEmpty() ? FluidResource.EMPTY : FluidResource.of(t.getFluid());
	}

	@Override
	public long getAmountAsLong(int index) {
		Objects.checkIndex(index, size());
		Tank t = tank.get();
		return t == null ? 0 : t.getAmount() / DROPLETS_PER_MB;
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		Objects.checkIndex(index, size());
		Tank t = tank.get();
		if (t == null || (!resource.isEmpty() && !accepts(t, resource))) {
			return 0;
		}
		return t.getCapacity() / DROPLETS_PER_MB;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		Objects.checkIndex(index, size());
		Tank t = tank.get();
		return t != null && accepts(t, resource);
	}

	private static boolean accepts(Tank tank, FluidResource resource) {
		return !resource.isEmpty() && resource.isComponentsPatchEmpty()
			&& (tank.isResourceBlank() || tank.getResource().fluid() == resource.getFluid());
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		Objects.checkIndex(index, size());
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		Tank t = tank.get();
		if (t == null || !accepts(t, resource)) {
			return 0;
		}
		long mb = Math.min(amount, (t.getCapacity() - t.getAmount()) / DROPLETS_PER_MB);
		if (mb <= 0) {
			return 0;
		}
		long inserted = t.insert(RcFluidVariant.of(resource.getFluid()), mb * DROPLETS_PER_MB, RcNeoTransactionBridge.openBoundTo(transaction));
		return (int) (inserted / DROPLETS_PER_MB);
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		Objects.checkIndex(index, size());
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		Tank t = tank.get();
		if (t == null || t.isEmpty() || !accepts(t, resource)) {
			return 0;
		}
		long mb = Math.min(amount, t.getAmount() / DROPLETS_PER_MB);
		if (mb <= 0) {
			return 0;
		}
		long extracted = t.extract(RcFluidVariant.of(resource.getFluid()), mb * DROPLETS_PER_MB, RcNeoTransactionBridge.openBoundTo(transaction));
		return (int) (extracted / DROPLETS_PER_MB);
	}
}
