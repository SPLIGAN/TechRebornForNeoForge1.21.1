/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TeamReborn
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

package reborncore.common.fluid;

import org.jetbrains.annotations.NotNull;
import reborncore.common.compat.TransferApiBridge;
import reborncore.common.fluid.container.FluidInstance;
import reborncore.common.util.Tank;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidUtils {

	@NotNull
	public static Fluid fluidFromBlock(Block block) {
		if (block instanceof LiquidBlock fluidBlock) {
			return fluidBlock.fluid;
		}

		return Fluids.EMPTY;
	}

	public static List<Fluid> getAllFluids() {
		return BuiltInRegistries.FLUID.stream().collect(Collectors.toList());
	}

	public static boolean drainContainers(Tank tank, Container inventory, int inputSlot, int outputSlot) {
		return drainContainers(tank, inventory, inputSlot, outputSlot, false);
	}

	public static boolean drainContainers(Tank tank, Container inventory, int inputSlot, int outputSlot, boolean voidFluid) {
		var itemStorage = TransferApiBridge.fluidStorageConnectingInventorySlots(inventory, inputSlot, outputSlot);

		if (voidFluid) {
			return TransferApiBridge.drainFluidStorageCompletelyCommitted(itemStorage);
		}
		return TransferApiBridge.moveFluids(itemStorage, tank, fv -> true, Long.MAX_VALUE, null) > 0;
	}

	public static boolean fillContainers(Tank source, Container inventory, int inputSlot, int outputSlot) {
		return TransferApiBridge.moveFluids(
				source,
				TransferApiBridge.fluidStorageConnectingInventorySlots(inventory, inputSlot, outputSlot),
				fv -> true,
				Long.MAX_VALUE,
				null
		) > 0;
	}

	public static boolean fluidEquals(@NotNull Fluid fluid, @NotNull Fluid fluid1) {
		return fluid == fluid1;
	}

	public static boolean isContainer(ItemStack stack) {
		return TransferApiBridge.itemStackProvidesFluidItemStorage(stack);
	}

	public static boolean isContainerEmpty(ItemStack stack) {
		return TransferApiBridge.fluidItemStorageEffectivelyEmpty(stack);
	}

	public static boolean containsMatchingFluid(ItemStack stack, Predicate<Fluid> predicate) {
		return TransferApiBridge.fluidItemStorageMatchesFluid(stack, predicate);
	}

	@Deprecated
	public static boolean interactWithFluidHandler(Player playerIn, Tank tank) {
		// TODO
		return false;
	}

	public static String getFluidName(@NotNull FluidInstance fluidInstance) {
		// TODO: use FluidVariantRendering
		return getFluidName(fluidInstance.fluid());
	}

	public static String getFluidName(@NotNull Fluid fluid) {
		return Component.translatable(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId()).getString();
	}
}
