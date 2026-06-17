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

package techreborn.init;

import java.util.LinkedHashMap;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.RegisterEvent;
import reborncore.common.crafting.RebornRecipe;
import reborncore.common.crafting.RecipeManager;
import techreborn.recipe.recipes.AssemblingMachineRecipe;
import techreborn.recipe.recipes.BlastFurnaceRecipe;
import techreborn.recipe.recipes.CentrifugeRecipe;
import techreborn.recipe.recipes.FluidGeneratorRecipe;
import techreborn.recipe.recipes.FluidReplicatorRecipe;
import techreborn.recipe.recipes.FusionReactorRecipe;
import techreborn.recipe.recipes.IndustrialGrinderRecipe;
import techreborn.recipe.recipes.IndustrialSawmillRecipe;
import techreborn.recipe.recipes.RollingMachineRecipe;

public class ModRecipes {

	private static final LinkedHashMap<Identifier, RecipeType<?>> RECIPE_TYPES_BY_ID = new LinkedHashMap<>();
	private static final LinkedHashMap<Identifier, RecipeSerializer<?>> RECIPE_SERIALIZERS_BY_ID = new LinkedHashMap<>();
	private static volatile boolean prepared;

	public static RecipeType<RebornRecipe> ALLOY_SMELTER;
	public static RecipeType<AssemblingMachineRecipe> ASSEMBLING_MACHINE;
	public static RecipeType<BlastFurnaceRecipe> BLAST_FURNACE;
	public static RecipeType<CentrifugeRecipe> CENTRIFUGE;
	public static RecipeType<RebornRecipe> CHEMICAL_REACTOR;
	public static RecipeType<RebornRecipe> COMPRESSOR;
	public static RecipeType<RebornRecipe> DISTILLATION_TOWER;
	public static RecipeType<RebornRecipe> EXTRACTOR;
	public static RecipeType<RebornRecipe> GRINDER;
	public static RecipeType<RebornRecipe> IMPLOSION_COMPRESSOR;
	public static RecipeType<RebornRecipe> INDUSTRIAL_ELECTROLYZER;
	public static RecipeType<IndustrialGrinderRecipe> INDUSTRIAL_GRINDER;
	public static RecipeType<IndustrialSawmillRecipe> INDUSTRIAL_SAWMILL;
	public static RecipeType<RebornRecipe> RECYCLER;
	public static RecipeType<RebornRecipe> SCRAPBOX;
	public static RecipeType<RebornRecipe> VACUUM_FREEZER;
	public static RecipeType<FluidReplicatorRecipe> FLUID_REPLICATOR;
	public static RecipeType<FusionReactorRecipe> FUSION_REACTOR;
	public static RecipeType<RollingMachineRecipe> ROLLING_MACHINE;
	public static RecipeType<RebornRecipe> SOLID_CANNING_MACHINE;
	public static RecipeType<RebornRecipe> WIRE_MILL;
	public static RecipeType<FluidGeneratorRecipe> THERMAL_GENERATOR;
	public static RecipeType<FluidGeneratorRecipe> GAS_GENERATOR;
	public static RecipeType<FluidGeneratorRecipe> DIESEL_GENERATOR;
	public static RecipeType<FluidGeneratorRecipe> SEMI_FLUID_GENERATOR;
	public static RecipeType<FluidGeneratorRecipe> PLASMA_GENERATOR;

	private ModRecipes() {
	}

	private static <R extends RebornRecipe> void add(Identifier id, RecipeManager.RecipeTypeRegistration<R> reg, Consumer<RecipeType<R>> assignField) {
		assignField.accept(reg.type());
		RECIPE_TYPES_BY_ID.put(id, reg.type());
		RECIPE_SERIALIZERS_BY_ID.put(id, reg.serializer());
	}

	private static void prepare() {
		synchronized (ModRecipes.class) {
			if (prepared) {
				return;
			}
			Identifier id;
			id = Identifier.parse("techreborn:alloy_smelter");
			add(id, RecipeManager.createRecipeRegistration(id), t -> ALLOY_SMELTER = t);
			id = Identifier.parse("techreborn:assembling_machine");
			add(id, RecipeManager.createRecipeRegistration(id, AssemblingMachineRecipe.CODEC, AssemblingMachineRecipe.PACKET_CODEC), t -> ASSEMBLING_MACHINE = t);
			id = Identifier.parse("techreborn:blast_furnace");
			add(id, RecipeManager.createRecipeRegistration(id, BlastFurnaceRecipe.CODEC, BlastFurnaceRecipe.PACKET_CODEC), t -> BLAST_FURNACE = t);
			id = Identifier.parse("techreborn:centrifuge");
			add(id, RecipeManager.createRecipeRegistration(id, CentrifugeRecipe.CODEC, CentrifugeRecipe.PACKET_CODEC), t -> CENTRIFUGE = t);
			id = Identifier.parse("techreborn:chemical_reactor");
			add(id, RecipeManager.createRecipeRegistration(id), t -> CHEMICAL_REACTOR = t);
			id = Identifier.parse("techreborn:compressor");
			add(id, RecipeManager.createRecipeRegistration(id), t -> COMPRESSOR = t);
			id = Identifier.parse("techreborn:distillation_tower");
			add(id, RecipeManager.createRecipeRegistration(id), t -> DISTILLATION_TOWER = t);
			id = Identifier.parse("techreborn:extractor");
			add(id, RecipeManager.createRecipeRegistration(id), t -> EXTRACTOR = t);
			id = Identifier.parse("techreborn:grinder");
			add(id, RecipeManager.createRecipeRegistration(id), t -> GRINDER = t);
			id = Identifier.parse("techreborn:implosion_compressor");
			add(id, RecipeManager.createRecipeRegistration(id), t -> IMPLOSION_COMPRESSOR = t);
			id = Identifier.parse("techreborn:industrial_electrolyzer");
			add(id, RecipeManager.createRecipeRegistration(id), t -> INDUSTRIAL_ELECTROLYZER = t);
			id = Identifier.parse("techreborn:industrial_grinder");
			add(id, RecipeManager.createRecipeRegistration(id, IndustrialGrinderRecipe.CODEC, IndustrialGrinderRecipe.PACKET_CODEC), t -> INDUSTRIAL_GRINDER = t);
			id = Identifier.parse("techreborn:industrial_sawmill");
			add(id, RecipeManager.createRecipeRegistration(id, IndustrialSawmillRecipe.CODEC, IndustrialSawmillRecipe.PACKET_CODEC), t -> INDUSTRIAL_SAWMILL = t);
			id = Identifier.parse("techreborn:recycler");
			add(id, RecipeManager.createRecipeRegistration(id), t -> RECYCLER = t);
			id = Identifier.parse("techreborn:scrapbox");
			add(id, RecipeManager.createRecipeRegistration(id), t -> SCRAPBOX = t);
			id = Identifier.parse("techreborn:vacuum_freezer");
			add(id, RecipeManager.createRecipeRegistration(id), t -> VACUUM_FREEZER = t);
			id = Identifier.parse("techreborn:fluid_replicator");
			add(id, RecipeManager.createRecipeRegistration(id, FluidReplicatorRecipe.CODEC, FluidReplicatorRecipe.PACKET_CODEC), t -> FLUID_REPLICATOR = t);
			id = Identifier.parse("techreborn:fusion_reactor");
			add(id, RecipeManager.createRecipeRegistration(id, FusionReactorRecipe.CODEC, FusionReactorRecipe.PACKET_CODEC), t -> FUSION_REACTOR = t);
			id = Identifier.parse("techreborn:rolling_machine");
			add(id, RecipeManager.createRecipeRegistration(id, RollingMachineRecipe.CODEC, RollingMachineRecipe.PACKET_CODEC), t -> ROLLING_MACHINE = t);
			id = Identifier.parse("techreborn:solid_canning_machine");
			add(id, RecipeManager.createRecipeRegistration(id), t -> SOLID_CANNING_MACHINE = t);
			id = Identifier.parse("techreborn:wire_mill");
			add(id, RecipeManager.createRecipeRegistration(id), t -> WIRE_MILL = t);
			id = Identifier.parse("techreborn:thermal_generator");
			add(id, RecipeManager.createRecipeRegistration(id, FluidGeneratorRecipe.CODEC, FluidGeneratorRecipe.PACKET_CODEC), t -> THERMAL_GENERATOR = t);
			id = Identifier.parse("techreborn:gas_generator");
			add(id, RecipeManager.createRecipeRegistration(id, FluidGeneratorRecipe.CODEC, FluidGeneratorRecipe.PACKET_CODEC), t -> GAS_GENERATOR = t);
			id = Identifier.parse("techreborn:diesel_generator");
			add(id, RecipeManager.createRecipeRegistration(id, FluidGeneratorRecipe.CODEC, FluidGeneratorRecipe.PACKET_CODEC), t -> DIESEL_GENERATOR = t);
			id = Identifier.parse("techreborn:semi_fluid_generator");
			add(id, RecipeManager.createRecipeRegistration(id, FluidGeneratorRecipe.CODEC, FluidGeneratorRecipe.PACKET_CODEC), t -> SEMI_FLUID_GENERATOR = t);
			id = Identifier.parse("techreborn:plasma_generator");
			add(id, RecipeManager.createRecipeRegistration(id, FluidGeneratorRecipe.CODEC, FluidGeneratorRecipe.PACKET_CODEC), t -> PLASMA_GENERATOR = t);
			prepared = true;
		}
	}

	public static void register(RegisterEvent event) {
		ResourceKey<? extends Registry<?>> key = event.getRegistryKey();
		if (!key.equals(Registries.RECIPE_SERIALIZER) && !key.equals(Registries.RECIPE_TYPE)) {
			return;
		}
		prepare();
		if (key.equals(Registries.RECIPE_SERIALIZER)) {
			RECIPE_SERIALIZERS_BY_ID.forEach((id, serializer) -> event.register(Registries.RECIPE_SERIALIZER, id, () -> serializer));
		} else {
			RECIPE_TYPES_BY_ID.forEach((id, recipeType) -> event.register(Registries.RECIPE_TYPE, id, () -> recipeType));
		}
	}
}
