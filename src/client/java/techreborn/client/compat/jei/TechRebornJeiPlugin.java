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

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import reborncore.common.crafting.RebornRecipe;
import reborncore.common.crafting.RecipeManager;
import techreborn.TechReborn;
import techreborn.client.compat.TechRebornRecipeIcons;
import techreborn.init.ModRecipes;
import techreborn.init.TRContent.Machine;
import techreborn.recipe.recipes.FluidGeneratorRecipe;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class TechRebornJeiPlugin implements IModPlugin {
	private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(TechReborn.MOD_ID, "jei");

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	private static boolean isFluidGeneratorType(RecipeType<?> type) {
		return type == ModRecipes.THERMAL_GENERATOR
			|| type == ModRecipes.GAS_GENERATOR
			|| type == ModRecipes.DIESEL_GENERATOR
			|| type == ModRecipes.SEMI_FLUID_GENERATOR
			|| type == ModRecipes.PLASMA_GENERATOR;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		var guiHelper = registration.getJeiHelpers().getGuiHelper();
		for (RecipeType<?> type : RecipeManager.getRecipeTypes(TechReborn.MOD_ID)) {
			if (type == ModRecipes.RECYCLER) {
				continue;
			}
			if (isFluidGeneratorType(type)) {
				@SuppressWarnings({"unchecked", "rawtypes"})
				RecipeType<FluidGeneratorRecipe> fluidType = (RecipeType) type;
				registration.addRecipeCategories(new FluidGeneratorJeiCategory(guiHelper, fluidType, TechRebornRecipeIcons.stackForRecipeType(type)));
				continue;
			}
			if (type == ModRecipes.ROLLING_MACHINE) {
				registration.addRecipeCategories(new RollingMachineJeiCategory(guiHelper, TechRebornRecipeIcons.stackForRecipeType(type)));
				continue;
			}
			@SuppressWarnings({"unchecked", "rawtypes"})
			RecipeType<? extends RebornRecipe> cast = (RecipeType) type;
			registration.addRecipeCategories(new TechRebornMachineRecipeCategory(guiHelper, cast, TechRebornRecipeIcons.stackForRecipeType(type)));
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		var mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}
		net.minecraft.world.item.crafting.RecipeManager vanillaRecipes = mc.level.getRecipeManager();
		for (RecipeType<?> type : RecipeManager.getRecipeTypes(TechReborn.MOD_ID)) {
			if (type == ModRecipes.RECYCLER) {
				continue;
			}
			mezz.jei.api.recipe.RecipeType<?> jeiType = mezz.jei.api.recipe.RecipeType.createFromVanilla(type);
			@SuppressWarnings({"unchecked", "rawtypes"})
			List<?> holders = new ArrayList<>(vanillaRecipes.getAllRecipesFor((RecipeType) type));
			registration.addRecipes(jeiType, (List) holders);
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		for (RecipeType<?> type : RecipeManager.getRecipeTypes(TechReborn.MOD_ID)) {
			if (type == ModRecipes.RECYCLER) {
				continue;
			}
			mezz.jei.api.recipe.RecipeType<?> jeiType = mezz.jei.api.recipe.RecipeType.createFromVanilla(type);
			registration.addRecipeCatalyst(TechRebornRecipeIcons.stackForRecipeType(type), jeiType);
		}
		registration.addRecipeCatalyst(new ItemStack(Machine.IRON_ALLOY_FURNACE.asItem()), mezz.jei.api.recipe.RecipeType.createFromVanilla(ModRecipes.ALLOY_SMELTER));
	}
}
