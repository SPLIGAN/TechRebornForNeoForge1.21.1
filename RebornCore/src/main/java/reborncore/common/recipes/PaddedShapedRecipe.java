/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 TeamReborn
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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.neoforged.neoforge.registries.RegisterEvent;

public class PaddedShapedRecipe extends ShapedRecipe {
	public static final Identifier ID = Identifier.fromNamespaceAndPath("reborncore", "padded");
	public static RecipeSerializer<PaddedShapedRecipe> PADDED;

	public static final MapCodec<PaddedShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
				CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
				ShapedRecipePattern.MAP_CODEC.forGetter(o -> o.pattern),
				ItemStackTemplate.CODEC.fieldOf("result").forGetter(PaddedShapedRecipe::resultTemplate))
			.apply(instance, PaddedShapedRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PaddedShapedRecipe> STREAM_CODEC = StreamCodec.composite(
		Recipe.CommonInfo.STREAM_CODEC,
		o -> o.commonInfo,
		CraftingRecipe.CraftingBookInfo.STREAM_CODEC,
		o -> o.bookInfo,
		ShapedRecipePattern.STREAM_CODEC,
		o -> o.pattern,
		ItemStackTemplate.STREAM_CODEC,
		PaddedShapedRecipe::resultTemplate,
		PaddedShapedRecipe::new);

	public static void register(RegisterEvent event) {
		event.register(Registries.RECIPE_SERIALIZER, ID, () -> {
			RecipeSerializer<PaddedShapedRecipe> serializer = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
			PADDED = serializer;
			return serializer;
		});
	}

	private final ItemStackTemplate resultTemplate;

	public PaddedShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
		super(commonInfo, bookInfo, pattern, result);
		this.resultTemplate = result;
	}

	public PaddedShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
		this(
			new Recipe.CommonInfo(showNotification),
			new CraftingRecipe.CraftingBookInfo(category, group),
			pattern,
			ItemStackTemplate.fromNonEmptyStack(result));
	}

	public static ShapedRecipePattern create(Map<Character, Ingredient> key, List<String> pattern) {
		return ShapedRecipePattern.of(key, pattern);
	}

	@Override
	@SuppressWarnings("unchecked")
	public RecipeSerializer<ShapedRecipe> getSerializer() {
		return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) PADDED;
	}

	public ShapedRecipePattern getRaw() {
		return this.pattern;
	}

	public ItemStackTemplate resultTemplate() {
		return this.resultTemplate;
	}

	public ItemStack getResult() {
		return this.resultTemplate.create();
	}
}
