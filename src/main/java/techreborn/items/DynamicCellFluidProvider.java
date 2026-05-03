/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
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

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import reborncore.common.compat.TransferApiBridge;
import reborncore.common.transfer.RcItemVariant;
import techreborn.component.TRDataComponentTypes;

/**
 * Dynamic cell fluid mapping for transfer helpers.
 */
final class DynamicCellFluidProvider implements TransferApiBridge.DynamicCellFluidStorageProvider {
	private final DynamicCellItem cell;

	DynamicCellFluidProvider(DynamicCellItem cell) {
		this.cell = cell;
	}

	@Override
	public Fluid fluidFromItemVariant(RcItemVariant variant) {
		Holder<Fluid> holder = variant.prototype().get(TRDataComponentTypes.FLUID);
		return holder != null ? holder.value() : Fluids.EMPTY;
	}

	@Override
	public ItemStack stackWithFluid(Fluid fluid) {
		return cell.getCellWithFluid(fluid);
	}
}
