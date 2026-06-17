/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2022 TeamReborn
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

package reborncore.common.recipes;

import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

/**
 * Datagen helper for {@link PaddedShapedRecipe}. Wraps vanilla {@link ShapedRecipeBuilder} because its constructor is private in 26.1.
 */
public final class PaddedShapedRecipeJsonBuilder {
	private final ShapedRecipeBuilder delegate;

	private PaddedShapedRecipeJsonBuilder(ShapedRecipeBuilder delegate) {
		this.delegate = delegate;
	}

	public static PaddedShapedRecipeJsonBuilder shaped(HolderGetter<Item> items, RecipeCategory category, ItemLike output) {
		return shaped(items, category, output, 1);
	}

	public static PaddedShapedRecipeJsonBuilder shaped(HolderGetter<Item> items, RecipeCategory category, ItemLike output, int outputCount) {
		return new PaddedShapedRecipeJsonBuilder(ShapedRecipeBuilder.shaped(items, category, output, outputCount));
	}

	public PaddedShapedRecipeJsonBuilder define(Character symbol, net.minecraft.world.item.crafting.Ingredient ingredient) {
		delegate.define(symbol, ingredient);
		return this;
	}

	public PaddedShapedRecipeJsonBuilder define(Character symbol, ItemLike item) {
		delegate.define(symbol, item);
		return this;
	}

	public PaddedShapedRecipeJsonBuilder pattern(String row) {
		delegate.pattern(row);
		return this;
	}

	public PaddedShapedRecipeJsonBuilder unlockedBy(String name, net.minecraft.advancements.Criterion<?> criterion) {
		delegate.unlockedBy(name, criterion);
		return this;
	}

	public PaddedShapedRecipeJsonBuilder group(String group) {
		delegate.group(group);
		return this;
	}

	public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
		List<String> rows = readField("rows");
		ShapedRecipePattern pattern = ShapedRecipePattern.of(readField("key"), rows);
		PaddedShapedRecipe recipe = new PaddedShapedRecipe(
			RecipeBuilder.createCraftingCommonInfo(readField("showNotification")),
			RecipeBuilder.createCraftingBookInfo(readField("category"), readField("group")),
			pattern,
			readField("result")
		);
		RecipeUnlockAdvancementBuilder advancementBuilder = readField("advancementBuilder");
		RecipeCategory category = readField("category");
		output.accept(id, recipe, advancementBuilder.build(output, id, category));
	}

	@SuppressWarnings("unchecked")
	private <T> T readField(String name) {
		try {
			Field f = ShapedRecipeBuilder.class.getDeclaredField(name);
			f.setAccessible(true);
			return (T) f.get(delegate);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to access field " + name + " on ShapedRecipeBuilder", e);
		}
	}
}
