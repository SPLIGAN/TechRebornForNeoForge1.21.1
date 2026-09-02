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

package techreborn.datagen

import net.minecraft.core.HolderLookup
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.data.event.GatherDataEvent
import techreborn.datagen.advancement.TRAdvancementProvider
import techreborn.datagen.compat.Ae2
import techreborn.datagen.loottables.TRLootTableProvider
import techreborn.datagen.models.ModelProvider
import techreborn.datagen.recipes.crafting.CraftingRecipesProvider
import techreborn.datagen.recipes.machine.alloy_smelter.AlloySmelterRecipesProvider
import techreborn.datagen.recipes.machine.assembling_machine.AssemblingMachineRecipesProvider
import techreborn.datagen.recipes.machine.blast_furnace.BlastFurnaceRecipesProvider
import techreborn.datagen.recipes.machine.centrifuge.CentrifugeRecipesProvider
import techreborn.datagen.recipes.machine.chemical_reactor.ChemicalReactorRecipesProvider
import techreborn.datagen.recipes.machine.compressor.CompressorRecipesProvider
import techreborn.datagen.recipes.machine.distillation_tower.DistillationTowerRecipesProvider
import techreborn.datagen.recipes.machine.extractor.ExtractorRecipesProvider
import techreborn.datagen.recipes.machine.fluid_generator.FluidGeneratorRecipeProvider
import techreborn.datagen.recipes.machine.fluid_replicator.FluidReplicatorRecipesProvider
import techreborn.datagen.recipes.machine.fusion_reactor.FusionReactorRecipesProvider
import techreborn.datagen.recipes.machine.grinder.GrinderRecipesProvider
import techreborn.datagen.recipes.machine.implosion_compressor.ImplosionCompressorRecipesProvider
import techreborn.datagen.recipes.machine.industrial_electrolyzer.IndustrialElectrolyzerRecipesProvider
import techreborn.datagen.recipes.machine.industrial_grinder.IndustrialGrinderRecipesProvider
import techreborn.datagen.recipes.machine.industrial_sawmill.IndustrialSawmillRecipesProvider
import techreborn.datagen.recipes.machine.recycler.RecyclerRecipesProvider
import techreborn.datagen.recipes.machine.rolling_machine.RollingMachineRecipesProvider
import techreborn.datagen.recipes.machine.scrapbox.ScrapboxRecipesProvider
import techreborn.datagen.recipes.machine.solid_canning_machine.SolidCanningMachineRecipesProvider
import techreborn.datagen.recipes.machine.vacuum_freezer.VacuumFreezerRecipesProvider
import techreborn.datagen.recipes.machine.wire_mill.WireMillRecipesProvider
import techreborn.datagen.recipes.smelting.SmeltingRecipesProvider

import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction

/**
 * Registers NeoForge recipe datagen providers. Invoked from {@code TechRebornNeoForge} via reflection
 * so main does not compile against the datagen source set.
 */
class TechRebornRecipeDatagen {
	private TechRebornRecipeDatagen() {
	}

	static void gatherServerData(GatherDataEvent.Server event) {
		Ae2.setup()

		add(event, TRLootTableProvider::new)
		add(event, TRAdvancementProvider::new)

		add(event, SmeltingRecipesProvider::new)
		add(event, CraftingRecipesProvider::new)

		add(event, GrinderRecipesProvider::new)
		add(event, CompressorRecipesProvider::new)
		add(event, ExtractorRecipesProvider::new)
		add(event, FluidReplicatorRecipesProvider::new)
		add(event, ChemicalReactorRecipesProvider::new)
		add(event, AssemblingMachineRecipesProvider::new)
		add(event, BlastFurnaceRecipesProvider::new)
		add(event, CentrifugeRecipesProvider::new)
		add(event, IndustrialGrinderRecipesProvider::new)
		add(event, IndustrialSawmillRecipesProvider::new)
		add(event, ImplosionCompressorRecipesProvider::new)
		add(event, IndustrialElectrolyzerRecipesProvider::new)
		add(event, AlloySmelterRecipesProvider::new)
		add(event, RecyclerRecipesProvider::new)
		add(event, ScrapboxRecipesProvider::new)
		add(event, SolidCanningMachineRecipesProvider::new)
		add(event, VacuumFreezerRecipesProvider::new)
		add(event, WireMillRecipesProvider::new)
		add(event, RollingMachineRecipesProvider::new)
		add(event, DistillationTowerRecipesProvider::new)
		add(event, FusionReactorRecipesProvider::new)
		add(event, FluidGeneratorRecipeProvider::new)
	}

	static void gatherClientData(GatherDataEvent.Client event) {
		event.createProvider({ PackOutput output ->
			new ModelProvider(output)
		} as GatherDataEvent.DataProviderFromOutput)
	}

	private static <T extends DataProvider> void add(
		GatherDataEvent.Server event,
		BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> factory
	) {
		event.createProvider({ PackOutput output, CompletableFuture<HolderLookup.Provider> lookup ->
			factory.apply(output, lookup)
		} as GatherDataEvent.DataProviderFromOutputLookup)
	}
}
