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
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import reborncore.common.misc.MultiBlockBreakingTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BlockOutlineRenderer {
	private BlockOutlineRenderer() {
	}

	public static void onBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || player != event.getCamera().entity()) {
			return;
		}

		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty() || !(stack.getItem() instanceof MultiBlockBreakingTool tool)) {
			return;
		}

		BlockPos targetPos = event.getBlockPos();
		Level world = player.level();
		Set<BlockPos> blockPosList = tool.getBlocksToBreak(stack, world, targetPos, player);
		List<VoxelShape> shapes = new ArrayList<>();

		for (BlockPos pos : blockPosList) {
			if (pos.equals(targetPos)) {
				continue;
			}
			BlockState blockState = world.getBlockState(pos);
			shapes.add(blockState.getShape(world, pos, CollisionContext.of(player))
				.move(pos.getX() - targetPos.getX(), pos.getY() - targetPos.getY(), pos.getZ() - targetPos.getZ()));
		}

		if (shapes.isEmpty()) {
			return;
		}

		BlockState state = world.getBlockState(targetPos);
		VoxelShape shape = state.getShape(world, targetPos, CollisionContext.of(player));
		for (VoxelShape voxelShape : shapes) {
			shape = Shapes.or(shape, voxelShape);
		}

		Vec3 cam = event.getCamera().position();
		event.addCustomRenderer((renderState, buffer, poseStack, translucentPass, levelRenderState) -> false);
	}
}
