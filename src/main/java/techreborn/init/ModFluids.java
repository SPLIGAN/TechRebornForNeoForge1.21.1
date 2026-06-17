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


import reborncore.common.fluid.*;
import techreborn.TechReborn;

import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

public enum ModFluids implements ItemLike {
	BERYLLIUM,
	CALCIUM,
	CALCIUM_CARBONATE,
	CARBON,
	CARBON_FIBER,
	CHLORITE,
	COMPRESSED_AIR,
	DEUTERIUM,
	DIESEL,
	ELECTROLYZED_WATER,
	GLYCERYL,
	HELIUM,
	HELIUM3,
	HELIUMPLASMA,
	HYDROGEN,
	LITHIUM,
	MERCURY,
	METHANE,
	NITRO_CARBON,
	NITRO_DIESEL,
	NITROCOAL_FUEL,
	NITROFUEL,
	NITROGEN,
	NITROGEN_DIOXIDE,
	OIL,
	POTASSIUM,
	SILICON,
	SODIUM,
	SODIUM_SULFIDE,
	SODIUM_PERSULFATE,
	SULFUR,
	SULFURIC_ACID,
	TRITIUM,
	WOLFRAMIUM,
	BIOFUEL;

	private RebornFluid stillFluid;
	private RebornFluid flowingFluid;

	private RebornFluidBlock block;
	private RebornBucketItem bucket;
	private final Identifier identifier;
	private final FluidSettings fluidSettings;
	private final FluidType fluidType;
	private final Supplier<FluidType> fluidTypeSupplier;

	ModFluids() {
		this.identifier = Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, this.toString().toLowerCase(Locale.ROOT));

		fluidSettings = FluidSettings.create();

		Identifier texture_still = Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, "block/fluids/" + this.toString().toLowerCase(Locale.ROOT) + "_still");
		Identifier texture_flowing = Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, "block/fluids/" + this.toString().toLowerCase(Locale.ROOT) + "_flowing");

		fluidSettings.setStillTexture(texture_still);
		fluidSettings.setFlowingTexture(texture_flowing);

		fluidType = RebornFluidTypes.create(fluidSettings);
		fluidTypeSupplier = () -> fluidType;

		stillFluid = new RebornFluid(true, fluidSettings, fluidTypeSupplier, this::getBlock, this::getBucket, () -> flowingFluid, () -> stillFluid) {
		};
		flowingFluid = new RebornFluid(false, fluidSettings, fluidTypeSupplier, this::getBlock, this::getBucket, () -> flowingFluid, () -> stillFluid) {
		};
	}

	public void registerFluidTypeOnly(RegisterEvent event) {
		event.register(NeoForgeRegistries.Keys.FLUID_TYPES, identifier, () -> fluidType);
	}

	private void ensureBlockAndBucket() {
		if (block != null) {
			return;
		}
		block = new RebornFluidBlock(stillFluid, TRBlockSettings.fluid());
		bucket = new RebornBucketItem(stillFluid, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));
	}

	public void registerFluidsOnly() {
		RebornFluidManager.register(stillFluid, identifier);
		RebornFluidManager.register(flowingFluid, Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, identifier.getPath() + "_flowing"));
	}

	public void registerFluidBlockOnly() {
		ensureBlockAndBucket();
		Registry.register(BuiltInRegistries.BLOCK, identifier, block);
	}

	public void registerFluidBucketOnly() {
		ensureBlockAndBucket();
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, identifier.getPath() + "_bucket"), bucket);
	}

	public RebornFluid getFluid() {
		return stillFluid;
	}

	public RebornFluid getFlowingFluid() {
		return flowingFluid;
	}

	public RebornFluidBlock getBlock() {
		ensureBlockAndBucket();
		return block;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public RebornBucketItem getBucket() {
		ensureBlockAndBucket();
		return bucket;
	}

	@Override
	public Item asItem() {
		return getBucket();
	}
}
