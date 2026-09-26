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
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * A NeoForge fluid {@link ResourceHandler} (millibuckets) as an {@link RcStorage} (droplets). Operations join the
 * RebornCore transaction and only move whole millibuckets, so no fraction is moved without being reported.
 */
public record RcFluidHandlerBackedStorage(ResourceHandler<FluidResource> handler) implements RcStorage<RcFluidVariant> {
	private static final long DROPLETS_PER_MB = RcTransferConstants.DROPLETS_PER_BUCKET / 1000;

	@Override
	public RcFluidVariant getResource() {
		for (int i = 0; i < handler.size(); i++) {
			FluidResource resource = handler.getResource(i);
			if (!resource.isEmpty() && handler.getAmountAsLong(i) > 0) {
				return RcFluidVariant.of(resource.getFluid());
			}
		}
		return RcFluidVariant.blank();
	}

	@Override
	public long getAmount() {
		long sum = 0;
		for (int i = 0; i < handler.size(); i++) {
			sum += handler.getAmountAsLong(i) * DROPLETS_PER_MB;
		}
		return sum;
	}

	@Override
	public boolean isResourceBlank() {
		return getAmount() == 0;
	}

	@Override
	public long insert(RcFluidVariant resource, long maxAmount, @Nullable RcTransactionContext tx) {
		if (resource.isBlank() || maxAmount < DROPLETS_PER_MB) {
			return 0;
		}
		FluidResource fluid = FluidResource.of(resource.fluid());
		int mb = Ints.saturatedCast(maxAmount / DROPLETS_PER_MB);
		if (tx != null) {
			return handler.insert(fluid, mb, RcNeoTransactionBridge.openNeoBoundTo(tx)) * DROPLETS_PER_MB;
		}
		try (Transaction transaction = RcNeoTransactionBridge.openNeoBoundTo(null)) {
			int inserted = handler.insert(fluid, mb, transaction);
			transaction.commit();
			return inserted * DROPLETS_PER_MB;
		}
	}

	@Override
	public long extract(RcFluidVariant resource, long maxAmount, @Nullable RcTransactionContext tx) {
		if (resource.isBlank() || maxAmount < DROPLETS_PER_MB) {
			return 0;
		}
		FluidResource fluid = FluidResource.of(resource.fluid());
		int mb = Ints.saturatedCast(maxAmount / DROPLETS_PER_MB);
		if (tx != null) {
			return handler.extract(fluid, mb, RcNeoTransactionBridge.openNeoBoundTo(tx)) * DROPLETS_PER_MB;
		}
		try (Transaction transaction = RcNeoTransactionBridge.openNeoBoundTo(null)) {
			int extracted = handler.extract(fluid, mb, transaction);
			transaction.commit();
			return extracted * DROPLETS_PER_MB;
		}
	}
}
