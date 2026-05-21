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

package reborncore.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

/**
 * Decodes Fabric-style {@code fabric:components} ingredient JSON emitted by older datagen / Fabric,
 * after {@link Ingredient#CODEC_NONEMPTY} fails (NeoForge vanilla codec does not understand it).
 */
public final class RecipeIngredientCompat {
	private static final ResourceLocation TECHREBORN_FLUID_COMPONENT = ResourceLocation.fromNamespaceAndPath("techreborn", "fluid");

	public static final Codec<Ingredient> CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<Pair<Ingredient, T>> vanilla = Ingredient.CODEC_NONEMPTY.decode(ops, input);
			if (vanilla.isSuccess()) {
				return vanilla;
			}
			if (ops instanceof JsonOps && input instanceof JsonElement element) {
				return decodeFabricComponentsIngredient(element).map(ingredient -> Pair.of(ingredient, ops.empty()));
			}
			return vanilla;
		}

		@Override
		public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
			return Ingredient.CODEC_NONEMPTY.encode(input, ops, prefix);
		}
	};

	private RecipeIngredientCompat() {
	}

	private static DataResult<Ingredient> decodeFabricComponentsIngredient(JsonElement element) {
		if (!element.isJsonObject()) {
			return DataResult.error(() -> "Expected ingredient object for Fabric compat");
		}
		JsonObject object = element.getAsJsonObject();
		if (!object.has("fabric:type")) {
			return DataResult.error(() -> "Not a Fabric-format ingredient");
		}
		String fabricType = GsonHelper.getAsString(object, "fabric:type");
		if (!"fabric:components".equals(fabricType)) {
			return DataResult.error(() -> "Unsupported fabric:type for ingredient: " + fabricType);
		}

		JsonObject base = GsonHelper.getAsJsonObject(object, "base");
		int count = GsonHelper.getAsInt(object, "count", 1);
		ResourceLocation itemId;
		if (base.has("item")) {
			itemId = ResourceLocation.parse(GsonHelper.getAsString(base, "item"));
		} else if (base.has("id")) {
			itemId = ResourceLocation.parse(GsonHelper.getAsString(base, "id"));
		} else {
			return DataResult.error(() -> "fabric base missing item/id");
		}

		Item item = BuiltInRegistries.ITEM.get(itemId);
		if (item == Items.AIR) {
			return DataResult.error(() -> "Unknown item for Fabric ingredient: " + itemId);
		}

		ItemStack stack = new ItemStack(item, count);
		if (object.has("components")) {
			JsonObject components = GsonHelper.getAsJsonObject(object, "components");
			for (String key : components.keySet()) {
				ResourceLocation componentId = ResourceLocation.parse(key);
				if (!TECHREBORN_FLUID_COMPONENT.equals(componentId)) {
					continue;
				}
				DataComponentType<?> rawType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(componentId);
				if (rawType == null) {
					continue;
				}
				JsonElement valueElement = components.get(key);
				BuiltInRegistries.FLUID.holderByNameCodec()
					.parse(JsonOps.INSTANCE, valueElement)
					.result()
					.ifPresent(holder -> stack.set((DataComponentType<Holder<Fluid>>) rawType, holder));
			}
		}

		return DataResult.success(Ingredient.of(stack));
	}
}
