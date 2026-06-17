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

package reborncore.common.fluid.container;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.minecraft.world.level.material.Fluids;

public record FluidContainerIngredient(Holder<Fluid> fluid, long amountMb) implements ICustomIngredient {
	public static final long DEFAULT_AMOUNT_MB = 1000L;
	public static final Identifier ID = Identifier.fromNamespaceAndPath("reborncore", "fluid_container");

	public static IngredientType<FluidContainerIngredient> TYPE;

	public static final MapCodec<FluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("fluid").forGetter(FluidContainerIngredient::fluid),
		Codec.LONG.optionalFieldOf("amount", DEFAULT_AMOUNT_MB).forGetter(FluidContainerIngredient::amountMb)
	).apply(instance, FluidContainerIngredient::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerIngredient> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.holderRegistry(Registries.FLUID), FluidContainerIngredient::fluid,
		ByteBufCodecs.VAR_LONG, FluidContainerIngredient::amountMb,
		FluidContainerIngredient::new
	);

	@Override
	public boolean test(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof ItemFluidInfo fluidInfo)) {
			return false;
		}
		Fluid stackFluid = fluidInfo.getFluid(stack);
		return stackFluid != Fluids.EMPTY && stackFluid.isSame(fluid.value());
	}

	@Override
	public Stream<Holder<Item>> items() {
		return BuiltInRegistries.ITEM.stream()
			.filter(item -> item instanceof ItemFluidInfo)
			.filter(item -> test(((ItemFluidInfo) item).getFull(fluid.value())))
			.map(Item::builtInRegistryHolder);
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IngredientType<?> getType() {
		return TYPE;
	}

	public ItemStack computeRemainder(ItemStack input) {
		if (input.getItem() instanceof ItemFluidInfo fluidInfo) {
			return fluidInfo.getEmpty();
		}
		return input.copy();
	}

	public static void modifyRemainders(CraftingRecipe recipe, CraftingInput input, NonNullList<ItemStack> remainders) {
		PlacementInfo info = recipe.placementInfo();
		if (info == null || info.ingredients() == null) {
			return;
		}

		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack inputStack = input.getItem(slot);
			if (inputStack.isEmpty()) {
				continue;
			}

			for (Ingredient ingredient : info.ingredients()) {
				if (!ingredient.isCustom()) {
					continue;
				}
				ICustomIngredient custom = ingredient.getCustomIngredient();
				if (custom instanceof FluidContainerIngredient fluidIngredient && fluidIngredient.test(inputStack)) {
					remainders.set(slot, fluidIngredient.computeRemainder(inputStack));
					break;
				}
			}
		}
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof FluidContainerIngredient other)) {
			return false;
		}
		return amountMb == other.amountMb && fluid.value().isSame(other.fluid.value());
	}

	@Override
	public int hashCode() {
		return Objects.hash(BuiltInRegistries.FLUID.getKey(fluid.value()), amountMb);
	}
}
