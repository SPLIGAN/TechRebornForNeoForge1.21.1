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

package techreborn.client.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;
import techreborn.recipe.recipes.FluidGeneratorRecipe;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FluidGeneratorJeiCategory implements IRecipeCategory<RecipeHolder<FluidGeneratorRecipe>> {
	private static final int WIDTH = 130;
	private static final int HEIGHT = 56;

	private final mezz.jei.api.recipe.RecipeType jeiRecipeType;
	private final RecipeType<FluidGeneratorRecipe> minecraftRecipeType;
	private final IDrawable icon;

	public FluidGeneratorJeiCategory(IGuiHelper guiHelper, RecipeType<FluidGeneratorRecipe> minecraftRecipeType, ItemStack iconStack) {
		this.minecraftRecipeType = minecraftRecipeType;
		this.jeiRecipeType = mezz.jei.api.recipe.RecipeType.createFromVanilla(minecraftRecipeType);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
	}

	@Override
	public mezz.jei.api.recipe.RecipeType getRecipeType() {
		return jeiRecipeType;
	}

	@Override
	public Component getTitle() {
		return Component.translatable(minecraftRecipeType.toString());
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public @Nullable IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<FluidGeneratorRecipe> holder, IFocusGroup focuses) {
		FluidGeneratorRecipe recipe = holder.value();
		int totalEnergy = recipe.power() * 1000;
		builder.addInputSlot(52, 12)
			.addFluidStack(recipe.getFluid(), 1000)
			.addRichTooltipCallback((recipeSlotView, tooltip) -> {
				tooltip.add(Component.translatable("techreborn.jei.recipe.energy"));
				tooltip.add(Component.translatable("techreborn.jei.recipe.generator.total", totalEnergy).withStyle(ChatFormatting.GRAY));
			});
	}
}
