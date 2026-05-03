/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 TeamReborn
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

package reborncore.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.material.Fluid;
import reborncore.common.fluid.FluidSettings;
import reborncore.common.fluid.RebornFluid;
import reborncore.common.fluid.RebornFluidManager;
import reborncore.common.util.TemporaryLazy;

import java.util.HashMap;
import java.util.Map;

public class RebornFluidRenderManager extends SimplePreparableReloadListener<Void> {

	private static final Map<Fluid, TemporaryLazy<TextureAtlasSprite[]>> spriteMap = new HashMap<>();

	public static void bootstrap() {
		RebornFluidManager.getFluidStream().forEach(RebornFluidRenderManager::setupFluidRenderer);
	}

	private static void setupFluidRenderer(RebornFluid fluid) {
		TemporaryLazy<TextureAtlasSprite[]> sprites = new TemporaryLazy<>(() -> {
			FluidSettings fluidSettings = fluid.getFluidSettings();
			return new TextureAtlasSprite[]{RenderUtil.getSprite(fluidSettings.getStillTexture()), RenderUtil.getSprite(fluidSettings.getFlowingTexture())};
		});

		spriteMap.put(fluid, sprites);
	}

	@Override
	protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return null;
	}

	@Override
	protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
		spriteMap.forEach((key, value) -> value.reset());
	}
}
