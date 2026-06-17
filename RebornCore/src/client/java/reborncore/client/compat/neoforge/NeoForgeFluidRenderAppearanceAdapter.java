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
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import reborncore.client.compat.FluidRenderAppearanceHandler;

/**
 * {@link FluidRenderAppearanceHandler} backed by NeoForge fluid model tint sources.
 */
public record NeoForgeFluidRenderAppearanceAdapter(Fluid fluid) implements FluidRenderAppearanceHandler {

	@Override
	public int getFluidColor(BlockAndTintGetter view, BlockPos pos, FluidState state) {
		FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
		FluidTintSource tintSource = model.fluidTintSource();
		if (tintSource == null) {
			return 0xFFFFFFFF;
		}
		return tintSource.colorInWorld(state, state.createLegacyBlock(), view, pos);
	}

	@Override
	public TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter view, BlockPos pos, FluidState state) {
		FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
		return new TextureAtlasSprite[] {model.stillMaterial().sprite(), model.flowingMaterial().sprite()};
	}
}
