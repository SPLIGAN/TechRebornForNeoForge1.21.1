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

package techreborn.recipe;

import reborncore.common.crafting.RebornRecipe;
import reborncore.common.crafting.RecipeUtils;
import reborncore.common.recipes.RecipeCrafter;
import reborncore.common.util.RebornInventory;
import techreborn.blockentity.machine.tier1.RecyclerBlockEntity;
import techreborn.config.TechRebornConfig;
import techreborn.init.ModRecipes;

import java.util.List;
import java.util.Objects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RecyclerRecipeCrafter extends RecipeCrafter {

	public RecyclerRecipeCrafter(BlockEntity blockEntity, RebornInventory<?> inventory, int[] inputSlots, int[] outputSlots) {
		super(ModRecipes.RECYCLER, blockEntity, 1, 1, inventory, inputSlots, outputSlots);
	}

	@Override
	public void updateCurrentRecipe() {
		List<RebornRecipe> recipeList = RecipeUtils.getRecipes(blockEntity.getLevel(), ModRecipes.RECYCLER);
		if (recipeList.isEmpty()) {
			return;
		}

		if (currentRecipe != null && !isValidRecyclerRecipe(currentRecipe)) {
			resetRecyclerCrafter();
		}

		if (currentRecipe == null) {
			RebornRecipe recipe = recipeList.getFirst();
			if (!isValidRecyclerRecipe(recipe)) {
				return;
			}
			setCurrentRecipe(recipe);
			currentNeededTicks = Math.max((int) (recipe.time() * (1.0 - getSpeedMultiplier())), 1);
			setIsActive();
		}
	}

	@Override
	public boolean hasAllInputs(RebornRecipe recipe) {
		if (recipe == null) {
			return false;
		}
		boolean hasItem = false;
		for (int inputSlot : inputSlots) {
			ItemStack stack = inventory.getItem(inputSlot);
			if (stack.isEmpty()) {
				continue;
			}
			if (!RecyclerBlockEntity.canRecycle(stack)) {
				return false;
			}
			hasItem = true;
			break;
		}
		return hasItem;
	}

	@Override
	public void useAllInputs() {
		if (currentRecipe == null) {
			return;
		}
		for (int inputSlot : inputSlots) {
			if (inventory.getItem(inputSlot).isEmpty()) {
				continue;
			}
			inventory.shrinkSlot(inputSlot, 1);
			break;
		}
	}

	@Override
	public void fitStack(ItemStack stack, int slot) {
		final Level world = Objects.requireNonNull(blockEntity.getLevel());
		final RandomSource random = world.getRandom();

		final int randomChance = random.nextInt(TechRebornConfig.recyclerChance);
		if (randomChance == 0) {
			super.fitStack(stack, slot);
		}
	}

	private boolean isValidRecyclerRecipe(RebornRecipe recipe) {
		if (!hasAllInputs(recipe)) {
			return false;
		}
		if (!recipe.canCraft(blockEntity)) {
			return false;
		}
		final List<ItemStack> outputs = recipe.outputs().stream().map(ItemStackTemplate::create).toList();
		for (int i = 0; i < outputs.size(); i++) {
			if (!canFitOutput(outputs.get(i), outputSlots[i])) {
				return false;
			}
		}
		return true;
	}

	private void resetRecyclerCrafter() {
		currentTickTime = 0;
		currentNeededTicks = 0;
		setCurrentRecipe(null);
		setIsActive();
	}
}
