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
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;
import reborncore.common.crafting.RebornFluidRecipe;
import reborncore.common.crafting.RebornRecipe;
import reborncore.common.crafting.SizedIngredient;
import reborncore.common.transfer.RcFluidAmounts;
import techreborn.recipe.recipes.BlastFurnaceRecipe;

import java.text.DecimalFormat;

public class TechRebornMachineRecipeCategory implements IRecipeCategory<RecipeHolder<RebornRecipe>> {
	private static final DecimalFormat TIME_FMT = new DecimalFormat("###.##");
	private static final int WIDTH = 162;
	private static final int HEIGHT = 108;

	private final IRecipeHolderType<RebornRecipe> jeiRecipeType;
	private final RecipeType<? extends RebornRecipe> minecraftRecipeType;
	private final IDrawable icon;

	@SuppressWarnings("unchecked")
	public TechRebornMachineRecipeCategory(IGuiHelper guiHelper, RecipeType<? extends RebornRecipe> minecraftRecipeType, ItemStack iconStack) {
		this.minecraftRecipeType = minecraftRecipeType;
		this.jeiRecipeType = IRecipeHolderType.create((RecipeType<RebornRecipe>) minecraftRecipeType);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
	}

	private static void appendProcessingTooltip(ITooltipBuilder tooltip, RebornRecipe recipe) {
		tooltip.add(Component.translatable("techreborn.jei.recipe.running.cost", Component.literal("E"), recipe.power()).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("techreborn.jei.recipe.generator.total", (long) recipe.power() * recipe.time()).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("techreborn.jei.recipe.processing.time.3", TIME_FMT.format(recipe.time() / 20.0)).withStyle(ChatFormatting.GRAY));
		if (recipe instanceof BlastFurnaceRecipe blastFurnaceRecipe) {
			tooltip.add(Component.literal(String.valueOf(blastFurnaceRecipe.getHeat()))
				.append(" ")
				.append(Component.translatable("techreborn.jei.recipe.heat")));
		}
	}

	@Override
	public IRecipeHolderType<RebornRecipe> getRecipeType() {
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
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<RebornRecipe> holder, IFocusGroup focuses) {
		RebornRecipe recipe = holder.value();
		int x = 8;
		int y = 10;
		int idx = 0;
		for (SizedIngredient sized : recipe.ingredients()) {
			int sx = x + (idx % 9) * 18;
			int sy = y + (idx / 9) * 18;
			builder.addInputSlot(sx, sy)
				.addIngredients(VanillaTypes.ITEM_STACK, sized.getPreviewStacks())
				.addRichTooltipCallback((recipeSlotView, tooltip) -> appendProcessingTooltip(tooltip, recipe));
			idx++;
		}
		if (recipe instanceof RebornFluidRecipe fluidRecipe && !fluidRecipe.fluid().isEmpty()) {
			var fi = fluidRecipe.fluid();
			long mb = RcFluidAmounts.toMilliBucketsClamped(fi.getAmount().getRawValue());
			int sx = x + (idx % 9) * 18;
			int sy = y + (idx / 9) * 18;
			builder.addInputSlot(sx, sy)
				.add(fi.fluid(), mb)
				.addRichTooltipCallback((recipeSlotView, tooltip) -> appendProcessingTooltip(tooltip, recipe));
			idx++;
		}
		int rowUsed = idx == 0 ? 0 : (idx + 8) / 9;
		int oy = y + rowUsed * 18 + (rowUsed > 0 ? 12 : 0);
		int ox = x;
		for (ItemStackTemplate output : recipe.outputs()) {
			builder.addOutputSlot(ox, oy)
				.add(output.create())
				.addRichTooltipCallback((recipeSlotView, tooltip) -> appendProcessingTooltip(tooltip, recipe));
			ox += 18;
		}
	}
}
