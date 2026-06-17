/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.client.compat;

import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import techreborn.TechReborn;
import techreborn.init.ModFluids;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * NeoForge replacement for Fabric {@code FluidRenderingRegistryImpl#getUnbakedModels()}.
 */
public final class FluidModelLookupBridge {
	private static final int OVERWORLD_WATER_COLOR = 0x3F76E4;

	private FluidModelLookupBridge() {
	}

	public static Map<Fluid, FluidModel.Unbaked> getUnbakedModels() {
		Map<Fluid, FluidModel.Unbaked> models = new IdentityHashMap<>();
		models.put(Fluids.WATER, vanillaWater());
		models.put(Fluids.LAVA, vanillaLava());

		for (ModFluids modFluid : ModFluids.values()) {
			Fluid still = modFluid.getFluid();
			if (still == Fluids.EMPTY) {
				continue;
			}
			Identifier id = BuiltInRegistries.FLUID.getKey(still);
			if (id.getNamespace().equals(TechReborn.MOD_ID)) {
				models.put(still, modFluid(id.getPath()));
			}
		}
		return models;
	}

	public static FluidModel.Unbaked getUnbaked(Fluid fluid) {
		if (fluid == Fluids.WATER) {
			return vanillaWater();
		}
		if (fluid == Fluids.LAVA) {
			return vanillaLava();
		}
		Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
		if (id.getNamespace().equals(TechReborn.MOD_ID)) {
			return modFluid(id.getPath());
		}
		return getUnbakedModels().get(fluid);
	}

	private static FluidModel.Unbaked vanillaWater() {
		return new FluidModel.Unbaked(
			new Material(Identifier.withDefaultNamespace("block/water_still")),
			new Material(Identifier.withDefaultNamespace("block/water_flow")),
			new Material(Identifier.withDefaultNamespace("block/water_overlay")),
			BlockTintSources.constant(OVERWORLD_WATER_COLOR)
		);
	}

	private static FluidModel.Unbaked vanillaLava() {
		return new FluidModel.Unbaked(
			new Material(Identifier.withDefaultNamespace("block/lava_still")),
			new Material(Identifier.withDefaultNamespace("block/lava_flow")),
			null,
			null
		);
	}

	private static FluidModel.Unbaked modFluid(String path) {
		Identifier still = Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, "block/fluid/" + path + "_still");
		Identifier flow = Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, "block/fluid/" + path + "_flow");
		return new FluidModel.Unbaked(new Material(still), new Material(flow), null, null);
	}
}
