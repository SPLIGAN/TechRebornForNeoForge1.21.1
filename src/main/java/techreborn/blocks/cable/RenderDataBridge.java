/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.blocks.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import techreborn.blockentity.cable.CableBlockEntity;

public final class RenderDataBridge {
	private RenderDataBridge() {
	}

	@Nullable
	public static BlockState getRenderAttachment(BlockAndLightGetter renderView, BlockPos pos) {
		BlockEntity be = renderView.getBlockEntity(pos);
		if (be instanceof CableBlockEntity cable) {
			return cable.getRenderAttachmentData();
		}
		return null;
	}
}
