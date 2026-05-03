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

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import reborncore.client.event.ClientLifecycleBridge;
import reborncore.common.util.LoaderBridge;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Initially taken from https://github.com/JamiesWhiteShirt/developer-mode/tree/experimental-item-render
 * and then ported to 1.15
 * Thanks 2xsaiko for fixing the lighting + odd issues above
 */
public class ItemStackRenderer implements ClientLifecycleBridge.HudRenderCallback {

	private static final int SIZE = 512;

	@Override
	public void onHudRender(GuiGraphics drawContext, DeltaTracker tickCounter) {
		if (!ItemStackRenderManager.RENDER_QUEUE.isEmpty()) {
			ItemStack itemStack = ItemStackRenderManager.RENDER_QUEUE.remove();
			ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
			drawContext.drawString(Minecraft.getInstance().font, "Rendering " + id, 5, 5, -1, false);
			drawContext.drawString(Minecraft.getInstance().font, ItemStackRenderManager.RENDER_QUEUE.size() + " items left", 5, 15, -1, false);
			export(id, itemStack);
		}
	}

	private void export(ResourceLocation identifier, ItemStack item) {
		Minecraft client = Minecraft.getInstance();

		Matrix4f matrix4f = new Matrix4f().setOrtho(0, 16, 16, 0, 1000, 3000);
		RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
		PoseStack stack = new PoseStack();
		stack.pushPose();
		stack.setIdentity();
		stack.translate(0, 0, -2000);
		Lighting.setupFor3DItems();
		RenderSystem.applyModelViewMatrix();

		RenderTarget framebuffer = new TextureTarget(SIZE, SIZE, true, Minecraft.ON_OSX);

		try (NativeImage nativeImage = new NativeImage(SIZE, SIZE, true)) {
			framebuffer.setClearColor(0, 0, 0, 0);
			framebuffer.clear(Minecraft.ON_OSX);

			{
				framebuffer.bindWrite(true);
				GuiGraphics drawContext = new GuiGraphics(client, client.renderBuffers().bufferSource());
				drawContext.renderItem(item, 0, 0);
				drawContext.flush();
				framebuffer.unbindWrite();
			}

			{
				framebuffer.bindRead();
				nativeImage.downloadTexture(0, false);
				nativeImage.flipY();
				framebuffer.unbindRead();
			}

			try {
				Path path = LoaderBridge.getGameDir().resolve("item_renderer").resolve(identifier.getNamespace()).resolve(identifier.getPath() + ".png");
				Files.createDirectories(path.getParent());
				nativeImage.writeToFile(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		framebuffer.destroyBuffers();
		stack.popPose();
		RenderSystem.applyModelViewMatrix();
	}
}
