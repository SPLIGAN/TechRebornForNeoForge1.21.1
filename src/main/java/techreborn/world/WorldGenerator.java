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

package techreborn.world;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WorldGenerator {
	public static final ResourceLocation OIL_LAKE_ID = ResourceLocation.fromNamespaceAndPath("techreborn", "oil_lake");
	public static final ResourceKey<ConfiguredFeature<?, ?> > OIL_LAKE_FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE, WorldGenerator.OIL_LAKE_ID);
	public static final ResourceKey<PlacedFeature> OIL_LAKE_PLACED_FEATURE = ResourceKey.create(Registries.PLACED_FEATURE, WorldGenerator.OIL_LAKE_ID);

	public static final ResourceLocation RUBBER_TREE_ID = ResourceLocation.fromNamespaceAndPath("techreborn", "rubber_tree");
	public static final ResourceKey<ConfiguredFeature<?, ?> > RUBBER_TREE_FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE, WorldGenerator.RUBBER_TREE_ID);
	public static final ResourceKey<PlacedFeature> RUBBER_TREE_PLACED_FEATURE = ResourceKey.create(Registries.PLACED_FEATURE, WorldGenerator.RUBBER_TREE_ID);

	public static final ResourceLocation RUBBER_TREE_PATCH_ID = ResourceLocation.fromNamespaceAndPath("techreborn", "rubber_tree_patch");
	public static final ResourceKey<ConfiguredFeature<?, ?> > RUBBER_TREE_PATCH_FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE, WorldGenerator.RUBBER_TREE_PATCH_ID);
	public static final ResourceKey<PlacedFeature> RUBBER_TREE_PATCH_PLACED_FEATURE = ResourceKey.create(Registries.PLACED_FEATURE, WorldGenerator.RUBBER_TREE_PATCH_ID);

	public static final TreeDecoratorType<RubberTreeSpikeDecorator> RUBBER_TREE_SPIKE = Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE, ResourceLocation.fromNamespaceAndPath("techreborn", "rubber_tree_spike"), new TreeDecoratorType<RubberTreeSpikeDecorator>(RubberTreeSpikeDecorator.CODEC));

	public static final TreeGrower RUBBER_TREE_SAPLING_GENERATOR = new TreeGrower(WorldGenerator.RUBBER_TREE_ID.toString(), java.util.Optional.empty(), java.util.Optional.of(WorldGenerator.RUBBER_TREE_FEATURE), java.util.Optional.empty());

}
