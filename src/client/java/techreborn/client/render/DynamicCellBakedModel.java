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
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import techreborn.TechReborn;

public class DynamicCellBakedModel extends DynamicFluidItemModelBase {

	public static final ResourceLocation CELL_BASE = ResourceLocation.fromNamespaceAndPath(TechReborn.MOD_ID, "item/cell_base");
	public static final ResourceLocation CELL_BACKGROUND = ResourceLocation.fromNamespaceAndPath(TechReborn.MOD_ID, "item/cell_background");
	public static final ResourceLocation CELL_FLUID = ResourceLocation.fromNamespaceAndPath(TechReborn.MOD_ID, "item/cell_fluid");
	public static final ResourceLocation CELL_GLASS = ResourceLocation.fromNamespaceAndPath(TechReborn.MOD_ID, "item/cell_glass");

	@Override
	protected List<BakedQuad> buildExtraLayers(ItemStack stack, RandomSource rand, @Nullable RenderType renderType) {
		ModelManager bakedModelManager = Minecraft.getInstance().getModelManager();
		BakedModel glass = bakedModelManager.getModel(ModelResourceLocation.standalone(CELL_GLASS));
		return new ArrayList<>(DynamicFluidItemModelBase.collectQuadsForModel(glass, rand, renderType));
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return Minecraft.getInstance()
				.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
				.apply(ResourceLocation.parse("techreborn:item/cell_base"));
	}

	@Override
	public ResourceLocation getBaseModel() {
		return CELL_BASE;
	}

	@Override
	public ResourceLocation getBackgroundModel() {
		return CELL_BACKGROUND;
	}

	@Override
	public ResourceLocation getFluidModel() {
		return CELL_FLUID;
	}
}
