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

package techreborn.client.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import techreborn.init.ModRecipes;
import techreborn.init.TRContent;
import techreborn.init.TRContent.Machine;

import java.util.HashMap;
import java.util.Map;

/**
 * Catalyst icons keyed by vanilla recipe type — initialized eagerly so REI-only or JEI-only loads both work.
 */
public final class TechRebornRecipeIcons {
	public static final Map<RecipeType<?>, ItemLike> RECIPE_ICONS = new HashMap<>();

	static {
		RECIPE_ICONS.put(ModRecipes.ALLOY_SMELTER, Machine.ALLOY_SMELTER);
		RECIPE_ICONS.put(ModRecipes.ASSEMBLING_MACHINE, Machine.ASSEMBLY_MACHINE);
		RECIPE_ICONS.put(ModRecipes.BLAST_FURNACE, Machine.INDUSTRIAL_BLAST_FURNACE);
		RECIPE_ICONS.put(ModRecipes.CENTRIFUGE, Machine.INDUSTRIAL_CENTRIFUGE);
		RECIPE_ICONS.put(ModRecipes.CHEMICAL_REACTOR, Machine.CHEMICAL_REACTOR);
		RECIPE_ICONS.put(ModRecipes.COMPRESSOR, Machine.COMPRESSOR);
		RECIPE_ICONS.put(ModRecipes.DISTILLATION_TOWER, Machine.DISTILLATION_TOWER);
		RECIPE_ICONS.put(ModRecipes.EXTRACTOR, Machine.EXTRACTOR);
		RECIPE_ICONS.put(ModRecipes.FLUID_REPLICATOR, Machine.FLUID_REPLICATOR);
		RECIPE_ICONS.put(ModRecipes.FUSION_REACTOR, Machine.FUSION_CONTROL_COMPUTER);
		RECIPE_ICONS.put(ModRecipes.GRINDER, Machine.GRINDER);
		RECIPE_ICONS.put(ModRecipes.IMPLOSION_COMPRESSOR, Machine.IMPLOSION_COMPRESSOR);
		RECIPE_ICONS.put(ModRecipes.INDUSTRIAL_ELECTROLYZER, Machine.INDUSTRIAL_ELECTROLYZER);
		RECIPE_ICONS.put(ModRecipes.INDUSTRIAL_GRINDER, Machine.INDUSTRIAL_GRINDER);
		RECIPE_ICONS.put(ModRecipes.INDUSTRIAL_SAWMILL, Machine.INDUSTRIAL_SAWMILL);
		RECIPE_ICONS.put(ModRecipes.ROLLING_MACHINE, Machine.ROLLING_MACHINE);
		RECIPE_ICONS.put(ModRecipes.SCRAPBOX, () -> TRContent.SCRAP_BOX);
		RECIPE_ICONS.put(ModRecipes.SOLID_CANNING_MACHINE, Machine.SOLID_CANNING_MACHINE);
		RECIPE_ICONS.put(ModRecipes.VACUUM_FREEZER, Machine.VACUUM_FREEZER);
		RECIPE_ICONS.put(ModRecipes.WIRE_MILL, Machine.WIRE_MILL);
		RECIPE_ICONS.put(ModRecipes.THERMAL_GENERATOR, Machine.THERMAL_GENERATOR);
		RECIPE_ICONS.put(ModRecipes.GAS_GENERATOR, Machine.GAS_TURBINE);
		RECIPE_ICONS.put(ModRecipes.DIESEL_GENERATOR, Machine.DIESEL_GENERATOR);
		RECIPE_ICONS.put(ModRecipes.SEMI_FLUID_GENERATOR, Machine.SEMI_FLUID_GENERATOR);
		RECIPE_ICONS.put(ModRecipes.PLASMA_GENERATOR, Machine.PLASMA_GENERATOR);
	}

	private TechRebornRecipeIcons() {
	}

	public static ItemStack stackForRecipeType(RecipeType<?> type) {
		ItemLike fallback = Items.DIAMOND_SHOVEL;
		return new ItemStack(RECIPE_ICONS.getOrDefault(type, fallback).asItem());
	}
}
