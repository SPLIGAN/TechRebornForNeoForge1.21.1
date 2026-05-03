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

package techreborn.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import reborncore.client.compat.FluidRenderAppearanceHandler;
import reborncore.client.compat.FluidRenderRegistryBridge;
import reborncore.common.fluid.container.ItemFluidInfo;

/**
 * Stack-aware fluid overlay item models using NeoForge {@link BakedModel#getRenderPasses(ItemStack, boolean)}.
 */
public abstract class DynamicFluidItemModelBase implements IDynamicBakedModel {

	private static ModelResourceLocation itemModel(ResourceLocation id) {
		return ModelResourceLocation.standalone(id);
	}

	public abstract ResourceLocation getBaseModel();

	public abstract ResourceLocation getBackgroundModel();

	public abstract ResourceLocation getFluidModel();

	@Override
	public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
		return List.of(new PassModel(this, itemStack));
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
		return Collections.emptyList();
	}

	protected List<BakedQuad> buildExtraLayers(ItemStack stack, RandomSource rand, @Nullable RenderType renderType) {
		return List.of();
	}

	private List<BakedQuad> buildAllQuads(ItemStack stack, RandomSource rand, @Nullable RenderType renderType) {
		List<BakedQuad> out = new ArrayList<>();
		ModelManager mgr = Minecraft.getInstance().getModelManager();
		addQuadsFromModel(mgr.getModel(itemModel(getBaseModel())), rand, renderType, out);
		addQuadsFromModel(mgr.getModel(itemModel(getBackgroundModel())), rand, renderType, out);

		Fluid fluid = Fluids.EMPTY;
		if (stack.getItem() instanceof ItemFluidInfo fluidInfo) {
			fluid = fluidInfo.getFluid(stack);
		}
		if (fluid != Fluids.EMPTY) {
			FluidRenderAppearanceHandler app = FluidRenderRegistryBridge.getFluidRenderAppearanceHandler(fluid);
			if (app != null) {
				LocalPlayer player = Minecraft.getInstance().player;
				if (player != null && Minecraft.getInstance().level != null) {
					int fluidColor = app.getFluidColor(Minecraft.getInstance().level, player.blockPosition(), fluid.defaultFluidState());
					TextureAtlasSprite[] sprites = app.getFluidSprites(Minecraft.getInstance().level, BlockPos.ZERO, fluid.defaultFluidState());
					TextureAtlasSprite fluidSprite = sprites != null && sprites.length > 0 ? sprites[0] : null;
					BakedModel fluidModel = mgr.getModel(itemModel(getFluidModel()));
					for (BakedQuad q : collectQuadsForModel(fluidModel, rand, renderType)) {
						out.add(retintQuad(q, fluidColor, fluidSprite));
					}
				}
			}
		}

		out.addAll(buildExtraLayers(stack, rand, renderType));
		return out;
	}

	private static void addQuadsFromModel(BakedModel m, RandomSource rand, @Nullable RenderType renderType, List<BakedQuad> out) {
		out.addAll(collectQuadsForModel(m, rand, renderType));
	}

	static List<BakedQuad> collectQuadsForModel(BakedModel m, RandomSource rand, @Nullable RenderType renderType) {
		if (m == null) {
			return List.of();
		}
		List<BakedQuad> r = new ArrayList<>(m.getQuads(null, null, rand, ModelData.EMPTY, renderType));
		if (r.isEmpty()) {
			for (Direction dir : Direction.values()) {
				r.addAll(m.getQuads(null, dir, rand, ModelData.EMPTY, renderType));
			}
		}
		return r;
	}

	private static BakedQuad retintQuad(BakedQuad q, int fluidColorArgb, @Nullable TextureAtlasSprite sprite) {
		int[] v = q.getVertices().clone();
		multiplyVertexColors(v, fluidColorArgb);
		TextureAtlasSprite spr = sprite != null ? sprite : q.getSprite();
		return new BakedQuad(v, q.getTintIndex(), q.getDirection(), spr, q.isShade());
	}

	/**
	 * Multiply baked-vertex diffuse with a fluid tint. {@link net.minecraft.client.renderer.block.model.BakedQuad} vertices follow
	 * {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#BLOCK}: each vertex uses {@link IQuadTransformer#STRIDE} ints and packed RGBA at
	 * {@link IQuadTransformer#COLOR} with <strong>R in the least-significant byte</strong> (see {@link com.mojang.blaze3d.vertex.VertexConsumer#putBulkData} byte offsets).
	 */
	private static void multiplyVertexColors(int[] vertexData, int tintArgb) {
		int rm = tintArgb >> 16 & 0xFF;
		int gm = tintArgb >> 8 & 0xFF;
		int bm = tintArgb & 0xFF;
		int stride = IQuadTransformer.STRIDE;
		int colorOffset = IQuadTransformer.COLOR;
		for (int vi = 0; vi < 4; vi++) {
			int base = vi * stride + colorOffset;
			if (base >= vertexData.length) {
				return;
			}
			int c = vertexData[base];
			int vr = c & 0xFF;
			int vg = c >> 8 & 0xFF;
			int vb = c >> 16 & 0xFF;
			int va = c >>> 24;
			vertexData[base] = va << 24 | vb * bm / 255 << 16 | vg * gm / 255 << 8 | vr * rm / 255;
		}
	}

	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public ItemTransforms getTransforms() {
		return ModelHelper.DEFAULT_ITEM_TRANSFORMS;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();
	}

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		return getParticleIcon();
	}

	private static final class PassModel extends BakedModelWrapper<DynamicFluidItemModelBase> {
		private final DynamicFluidItemModelBase owner;
		private final ItemStack stack;

		PassModel(DynamicFluidItemModelBase owner, ItemStack stack) {
			super(owner);
			this.owner = owner;
			this.stack = stack;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
			List<BakedQuad> all = owner.buildAllQuads(stack, rand, renderType);
			if (side == null) {
				return all;
			}
			List<BakedQuad> filtered = new ArrayList<>();
			for (BakedQuad q : all) {
				Direction d = q.getDirection();
				if (d == null || d == side) {
					filtered.add(q);
				}
			}
			return filtered;
		}

		@Override
		public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
			return List.of(this);
		}
	}
}
