/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2026 TechReborn
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

package techreborn.items;

import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import techreborn.init.TRContent;

import java.util.Objects;

/**
 * {@link net.neoforged.neoforge.capabilities.Capabilities.Fluid#ITEM} for Tech Reborn cells: each cell holds exactly
 * one bucket of a fluid that has its own cell item, and is only ever filled or emptied as a whole (like a bucket).
 */
public final class CellFluidHandler extends ItemAccessResourceHandler<FluidResource> {
	public CellFluidHandler(ItemAccess itemAccess) {
		super(itemAccess, 1);
	}

	@Override
	protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
		if (accessResource.getItem() instanceof CellItem cell && !cell.isEmpty()) {
			return FluidResource.of(cell.getCellFluid());
		}
		return FluidResource.EMPTY;
	}

	@Override
	protected int getAmountFrom(ItemResource accessResource, int index) {
		return getResourceFrom(accessResource, index).isEmpty() ? 0 : FluidType.BUCKET_VOLUME;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		Objects.checkIndex(index, size());
		return !resource.isEmpty() && resource.isComponentsPatchEmpty() && resource.getFluid() != Fluids.EMPTY
			&& TRContent.Cells.getCellByFluid(resource.getFluid()) != TRContent.Cells.EMPTY;
	}

	@Override
	protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
		if (newAmount == 0) {
			return ItemResource.of(TRContent.Cells.EMPTY);
		}
		if (newAmount != FluidType.BUCKET_VOLUME || !isValid(index, newResource)) {
			return ItemResource.EMPTY;
		}
		return ItemResource.of(TRContent.Cells.getCellByFluid(newResource.getFluid()));
	}

	@Override
	protected int getCapacity(int index, FluidResource resource) {
		Objects.checkIndex(index, size());
		return FluidType.BUCKET_VOLUME;
	}
}
