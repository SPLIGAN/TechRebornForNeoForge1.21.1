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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;
import techreborn.init.ModRecipes;
import techreborn.recipe.recipes.RollingMachineRecipe;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RollingMachineJeiCategory implements IRecipeCategory<RecipeHolder<RollingMachineRecipe>> {
	private static final DecimalFormat TIME_FMT = new DecimalFormat("###.##");
	private static final int WIDTH = 140;
	private static final int HEIGHT = 88;

	private final RecipeType jeiRecipeType;
	private final IDrawable icon;

	public RollingMachineJeiCategory(IGuiHelper guiHelper, ItemStack iconStack) {
		this.jeiRecipeType = RecipeType.createFromVanilla(ModRecipes.ROLLING_MACHINE);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
	}

	private static void appendTooltip(ITooltipBuilder tooltip, RollingMachineRecipe recipe) {
		tooltip.add(Component.translatable("techreborn.jei.recipe.energy"));
		tooltip.add(Component.translatable("techreborn.jei.recipe.running.cost", Component.literal("E"), recipe.power()).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("techreborn.jei.recipe.generator.total", (long) recipe.power() * recipe.time()).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("techreborn.jei.recipe.processing.time.3", TIME_FMT.format(recipe.time() / 20.0)).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public RecipeType getRecipeType() {
		return jeiRecipeType;
	}

	@Override
	public Component getTitle() {
		return Component.translatable(ModRecipes.ROLLING_MACHINE.toString());
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
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<RollingMachineRecipe> holder, IFocusGroup focuses) {
		RollingMachineRecipe recipe = holder.value();
		ShapedRecipe shaped = recipe.getShapedRecipe();
		List<Ingredient> ingredients = shaped.getIngredients();
		int w = shaped.getWidth();
		int h = shaped.getHeight();
		int sx = 12;
		int sy = 10;
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int index = row * w + col;
				Ingredient ing = ingredients.get(index);
				builder.addInputSlot(sx + col * 18, sy + row * 18)
					.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(ing.getItems()))
					.addRichTooltipCallback((recipeSlotView, tooltip) -> appendTooltip(tooltip, recipe));
			}
		}
		var mc = Minecraft.getInstance();
		HolderLookup.Provider lookup = mc.level != null ? mc.level.registryAccess()
			: mc.getConnection() != null ? mc.getConnection().registryAccess()
				: RegistryAccess.EMPTY;
		ItemStack result = recipe.getResultItem(lookup);
		builder.addOutputSlot(sx + w * 18 + 14, sy + 18)
			.addIngredient(VanillaTypes.ITEM_STACK, result)
			.addRichTooltipCallback((recipeSlotView, tooltip) -> appendTooltip(tooltip, recipe));
	}
}
