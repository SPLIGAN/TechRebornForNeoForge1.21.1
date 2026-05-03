/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.client.render;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import techreborn.blocks.misc.BlockMachineCasing;
import techreborn.utils.DirectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class MachineCasingModel {
	public static final String MODEL_PATH = "block/machines/structure/";

	private MachineCasingModel() {
	}

	public static Unbaked unbakedFor(BlockMachineCasing block, net.minecraft.world.level.block.state.BlockState state) {
		ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);
		ResourceLocation model = ResourceLocation.fromNamespaceAndPath(blockKey.getNamespace(), MODEL_PATH + blockKey.getPath());
		Either<Material, String> alone = Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, model));
		Either<Material, String> start = Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, model.withSuffix("_start")));
		Either<Material, String> middle = Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, model.withSuffix("_middle")));
		Either<Material, String> end = Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, model.withSuffix("_end")));

		Map<String, Either<Material, String>> textures = new HashMap<>();
		textures.put("down", alone);
		textures.put("up", alone);
		for (Direction direction : new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST }) {
			switch (DirectionUtils.getHorizontalPart(direction, state.getValue(DirectionUtils.HORIZONTAL_NEIGHBORS))) {
				case ALONE -> textures.put(direction.getName(), alone);
				case START -> textures.put(direction.getName(), start);
				case MIDDLE -> textures.put(direction.getName(), middle);
				case END -> textures.put(direction.getName(), end);
			}
		}
		return new Unbaked(model, textures);
	}

	public record Unbaked(ResourceLocation id, Map<String, Either<Material, String>> textures) implements UnbakedModel {
		@Override
		public Collection<ResourceLocation> getDependencies() {
			return List.of(id);
		}

		@Override
		public void resolveParents(Function<ResourceLocation, UnbakedModel> modelLoader) {
		}

		@Override
		public @Nullable BakedModel bake(
			ModelBaker baker,
			Function<Material, TextureAtlasSprite> textureGetter,
			ModelState rotationContainer
		) {
			BlockModel model = (BlockModel) baker.getModel(id);
			model.textureMap.putAll(textures);
			return model.bake(baker, textureGetter, rotationContainer);
		}
	}
}
