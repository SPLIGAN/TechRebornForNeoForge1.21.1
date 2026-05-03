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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import reborncore.common.misc.MultiBlockBreakingTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BlockOutlineRenderer {
	private BlockOutlineRenderer() {
	}

	public static void onBlockHighlight(RenderHighlightEvent.Block event) {
		List<VoxelShape> shapes = new ArrayList<>();

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		Level world = player.level();
		BlockPos targetPos = event.getTarget().getBlockPos();
		Vec3 cam = event.getCamera().getPosition();
		double cameraX = cam.x;
		double cameraY = cam.y;
		double cameraZ = cam.z;

		if (player == Minecraft.getInstance().player) {
			ItemStack stack = player.getMainHandItem();
			if (stack.isEmpty()) {
				return;
			}

			if (stack.getItem() instanceof MultiBlockBreakingTool tool) {
				Set<BlockPos> blockPosList = tool.getBlocksToBreak(stack, player.level(), targetPos, player);

				for (BlockPos pos : blockPosList) {
					if (pos.equals(targetPos)) {
						continue;
					}

					BlockState blockState = world.getBlockState(pos);
					shapes.add(blockState.getShape(world, pos, CollisionContext.of(player)).move(pos.getX() - targetPos.getX(), pos.getY() - targetPos.getY(), pos.getZ() - targetPos.getZ()));

				}
			}
		}

		if (!shapes.isEmpty()) {
			BlockState state = world.getBlockState(targetPos);
			VoxelShape shape = state.getShape(world, targetPos, CollisionContext.of(player));

			for (VoxelShape voxelShape : shapes) {
				shape = Shapes.or(shape, voxelShape);
			}

			LevelRenderer.renderVoxelShape(
				event.getPoseStack(),
				event.getMultiBufferSource().getBuffer(RenderType.lines()),
				shape,
				(double) targetPos.getX() - cameraX,
				(double) targetPos.getY() - cameraY,
				(double) targetPos.getZ() - cameraZ,
				0.0F,
				0.0F,
				0.0F,
				0.4F,
				true);
		}
	}
}
