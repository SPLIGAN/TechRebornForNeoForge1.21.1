/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2026 TeamReborn
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

package reborncore.client.compat.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import reborncore.client.compat.FluidRenderAppearanceHandler;

/**
 * {@link FluidRenderAppearanceHandler} backed by NeoForge {@link IClientFluidTypeExtensions}.
 */
public record NeoForgeFluidRenderAppearanceAdapter(Fluid fluid) implements FluidRenderAppearanceHandler {

	@Override
	public int getFluidColor(BlockAndTintGetter view, BlockPos pos, FluidState state) {
		return IClientFluidTypeExtensions.of(state).getTintColor(state, view, pos);
	}

	@Override
	public TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter view, BlockPos pos, FluidState state) {
		IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(state);
		ResourceLocation still = ext.getStillTexture(state, view, pos);
		ResourceLocation flowing = ext.getFlowingTexture(state, view, pos);
		if (still == null || flowing == null) {
			return new TextureAtlasSprite[] {null, null};
		}
		Material stillMat = ClientHooks.getBlockMaterial(still);
		Material flowMat = ClientHooks.getBlockMaterial(flowing);
		var stillAtlas = Minecraft.getInstance().getTextureAtlas(stillMat.atlasLocation());
		var flowAtlas = Minecraft.getInstance().getTextureAtlas(flowMat.atlasLocation());
		return new TextureAtlasSprite[] {
				stillAtlas.apply(stillMat.texture()),
				flowAtlas.apply(flowMat.texture())
		};
	}
}
