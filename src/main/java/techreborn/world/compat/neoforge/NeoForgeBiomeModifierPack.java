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

package techreborn.world.compat.neoforge;

import com.mojang.logging.LogUtils;
import net.minecraft.DetectedVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;
import reborncore.common.config.Configuration;
import techreborn.config.TechRebornConfig;
import techreborn.world.TargetDimension;
import techreborn.world.TROreFeatureConfig;
import techreborn.world.TROreFeatureQueries;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Generates a tiny datapack with NeoForge {@code neoforge:add_features} biome modifiers so ore patches,
 * rubber trees, and oil lakes participate in vanilla biome modifier loading without Fabric biome APIs.
 */
public final class NeoForgeBiomeModifierPack {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String PACK_ID = "techreborn/dynamic_worldgen";
	private static final Path RELATIVE_PACK_ROOT = Path.of("techreborn").resolve("generated_worldgen_pack");
	/** Matches {@link techreborn.world.WorldGenerator#RUBBER_TREE_PATCH_PLACED_FEATURE}. */
	private static final Identifier RUBBER_TREE_PATCH_PLACED = Identifier.fromNamespaceAndPath("techreborn", "rubber_tree_patch");
	/** Matches {@link techreborn.world.WorldGenerator#OIL_LAKE_PLACED_FEATURE}. */
	private static final Identifier OIL_LAKE_PLACED = Identifier.fromNamespaceAndPath("techreborn", "oil_lake");

	private NeoForgeBiomeModifierPack() {
	}

	public static void register(AddPackFindersEvent event) {
		if (event.getPackType() != PackType.SERVER_DATA) {
			return;
		}

		new Configuration(TechRebornConfig.class, "techreborn");

		Path root = FMLPaths.CONFIGDIR.get().resolve(RELATIVE_PACK_ROOT);
		boolean wrote = writePackContents(root);
		if (!wrote) {
			purgeStalePack(root);
			return;
		}

		Pack.ResourcesSupplier supplier = new PathPackResources.PathResourcesSupplier(root);

		Pack pack = Pack.readMetaAndCreate(
				new PackLocationInfo(PACK_ID, Component.literal("TechReborn dynamic worldgen"), PackSource.DEFAULT, Optional.empty()),
				supplier,
				PackType.SERVER_DATA,
				new PackSelectionConfig(true, Pack.Position.TOP, false));

		if (pack != null) {
			event.addRepositorySource(output -> output.accept(pack));
		} else {
			LOGGER.warn("TechReborn failed to create dynamic worldgen datapack from {}", root);
		}
	}

	private static void purgeStalePack(Path root) {
		if (!Files.isDirectory(root)) {
			return;
		}
		try (Stream<Path> walk = Files.walk(root)) {
			walk.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException e) {
					LOGGER.warn("Could not delete stale generated pack path {}", path, e);
				}
			});
		} catch (IOException e) {
			LOGGER.warn("Could not purge stale generated pack at {}", root, e);
		}
	}

	private static boolean writePackContents(Path root) {
		boolean ore = TechRebornConfig.enableOreGeneration;
		boolean rubber = TechRebornConfig.enableRubberTreeGeneration;
		boolean oil = TechRebornConfig.enableOilLakeGeneration;
		if (!ore && !rubber && !oil) {
			return false;
		}

		try {
			purgeStalePack(root);
			Path dataBiomeMods = root.resolve("data").resolve("techreborn").resolve("neoforge").resolve("biome_modifier");
			Files.createDirectories(dataBiomeMods);

			writePackMcmeta(root);

			if (rubber) {
				writeAddFeaturesJson(dataBiomeMods.resolve("rubber_tree_patch_forest.json"),
						"#minecraft:is_forest",
						RUBBER_TREE_PATCH_PLACED,
						"vegetal_decoration");
				writeAddFeaturesJson(dataBiomeMods.resolve("rubber_tree_patch_taiga.json"),
						"#minecraft:is_taiga",
						RUBBER_TREE_PATCH_PLACED,
						"vegetal_decoration");
				writeAddFeaturesJson(dataBiomeMods.resolve("rubber_tree_patch_swamp.json"),
						"minecraft:swamp",
						RUBBER_TREE_PATCH_PLACED,
						"vegetal_decoration");
			}
			if (oil) {
				writeAddFeaturesJson(dataBiomeMods.resolve("oil_lake.json"),
						"#minecraft:is_overworld",
						OIL_LAKE_PLACED,
						"lakes");
			}
			if (ore) {
				for (TROreFeatureConfig feature : TROreFeatureQueries.spawnEnabledByConfig()) {
					String biomeTarget = biomeSpecifier(feature);
					Path path = dataBiomeMods.resolve(feature.placedFeature().identifier().getPath() + ".json");
					writeAddFeaturesJson(path, biomeTarget, feature.placedFeature().identifier(), "underground_ores");
				}
			}
			return true;
		} catch (IOException e) {
			LOGGER.error("Could not write TechReborn dynamic worldgen datapack under {}", root, e);
			return false;
		}
	}

	private static String biomeSpecifier(TROreFeatureConfig feature) {
		TargetDimension dimension = feature.ore().distribution.dimension;
		return switch (dimension) {
			case OVERWORLD -> "#minecraft:is_overworld";
			case NETHER -> "#minecraft:is_nether";
			case END -> "#minecraft:is_end";
		};
	}

	private static void writePackMcmeta(Path root) throws IOException {
		int format = DetectedVersion.tryDetectVersion().packVersion(PackType.SERVER_DATA).major();
		// MC 26.1 requires min_format / max_format (pack_format alone triggers fallback warnings).
		String mcmeta = """
				{
				  "pack": {
				    "description": "TechReborn dynamic world generation (NeoForge biome modifiers)",
				    "pack_format": %d,
				    "min_format": %d,
				    "max_format": %d
				  }
				}
				""".formatted(format, format, format);
		Files.writeString(root.resolve("pack.mcmeta"), mcmeta, StandardCharsets.UTF_8);
	}

	private static void writeAddFeaturesJson(Path path, String biomes, Identifier placedFeature, String step)
			throws IOException {
		String json = """
				{
				  "type": "neoforge:add_features",
				  "biomes": "%s",
				  "features": "%s",
				  "step": "%s"
				}
				""".formatted(biomes, placedFeature, step);
		Files.writeString(path, json, StandardCharsets.UTF_8);
	}
}
