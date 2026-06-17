/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.init;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import reborncore.common.fluid.FluidUtils;
import techreborn.items.DynamicCellItem;

public class TRCauldronBehavior {
	public static void init() {
		CauldronInteraction FILL_CELL_WITH_LAVA = (state, world, pos, player, hand, stack) -> {
			if (!FluidUtils.isContainerEmpty(stack)) {
				return InteractionResult.TRY_WITH_EMPTY_HAND;
			}

			if (!world.isClientSide()) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, DynamicCellItem.getCellWithFluid(Fluids.LAVA)));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
				world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
				world.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
				world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
			}
			return InteractionResult.SUCCESS;
		};

		CauldronInteraction FILL_CELL_WITH_WATER = (state, world, pos, player, hand, stack) -> {
			if (!FluidUtils.isContainerEmpty(stack)) {
				return InteractionResult.TRY_WITH_EMPTY_HAND;
			}

			if (!world.isClientSide()) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, DynamicCellItem.getCellWithFluid(Fluids.WATER)));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
				LayeredCauldronBlock.lowerFillLevel(state, world, pos);
				world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
			}
			return InteractionResult.SUCCESS;
		};

		CauldronInteraction FILL_FROM_CELL = (state, world, pos, player, hand, stack) -> {
			if (!(stack.getItem() instanceof DynamicCellItem cellItem)) {
				return InteractionResult.PASS;
			}
			Fluid cellFluid = cellItem.getFluid(stack);
			if (cellFluid == Fluids.WATER) {
				return fillCauldronFromCell(world, pos, player, hand, stack,
					Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3),
					SoundEvents.BUCKET_EMPTY);
			} else if (cellFluid == Fluids.LAVA) {
				return fillCauldronFromCell(world, pos, player, hand, stack,
					Blocks.LAVA_CAULDRON.defaultBlockState(),
					SoundEvents.BUCKET_EMPTY_LAVA);
			}

			return InteractionResult.PASS;
		};

		CauldronInteractions.LAVA.put(TRContent.CELL, FILL_CELL_WITH_LAVA);
		CauldronInteractions.WATER.put(TRContent.CELL, FILL_CELL_WITH_WATER);
		CauldronInteractions.EMPTY.put(TRContent.CELL, FILL_FROM_CELL);
	}

	static InteractionResult fillCauldronFromCell(Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, BlockState state, SoundEvent soundEvent) {
		if (!world.isClientSide()) {
			player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(TRContent.CELL)));
			player.awardStat(Stats.FILL_CAULDRON);
			world.setBlockAndUpdate(pos, state);
			world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
			world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
		}

		return InteractionResult.SUCCESS;
	}
}
