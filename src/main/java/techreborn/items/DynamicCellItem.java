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

package techreborn.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import reborncore.common.compat.TransferApiBridge;
import reborncore.common.fluid.FluidUtils;
import reborncore.common.fluid.container.ItemFluidInfo;
import techreborn.component.TRDataComponentTypes;
import techreborn.init.TRContent;

/**
 * Created by modmuss50 on 17/05/2016.
 */
public class DynamicCellItem extends Item implements ItemFluidInfo {

	public DynamicCellItem() {
		super(new Item.Properties().stacksTo(16).component(TRDataComponentTypes.FLUID, Fluids.EMPTY.builtInRegistryHolder()));
	}

	// Thanks vanilla :)
	@SuppressWarnings("deprecation")
	private void playEmptyingSound(@Nullable Player playerEntity, LevelAccessor world, BlockPos blockPos, Fluid fluid) {
		SoundEvent soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
		world.playSound(playerEntity, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public static ItemStack getCellWithFluid(Fluid fluid, int stackSize) {
		Validate.notNull(fluid, "Can't get cell with NULL fluid");
		ItemStack stack = new ItemStack(TRContent.CELL, stackSize);
		stack.set(TRDataComponentTypes.FLUID, fluid.builtInRegistryHolder());
		stack.setCount(stackSize);
		return stack;
	}

	public static ItemStack getCellWithFluid(Fluid fluid) {
		return getCellWithFluid(fluid, 1);
	}

	public static ItemStack getEmptyCell(int amount) {
		return new ItemStack(TRContent.CELL, amount);
	}

	private void insertOrDropStack(Player playerEntity, ItemStack stack) {
		if (!playerEntity.getInventory().add(stack)) {
			playerEntity.spawnAtLocation(stack);
		}
	}

	public boolean placeFluid(@Nullable Player player, Level world, BlockPos pos, @Nullable BlockHitResult hitResult, ItemStack filledCell) {
		Fluid fluid = getFluid(filledCell);
		if (fluid == Fluids.EMPTY) {
			return false;
		}

		BlockState blockState = world.getBlockState(pos);
		boolean canPlace = blockState.canBeReplaced(fluid);

		if (!blockState.isAir() && !canPlace && (!(blockState.getBlock() instanceof LiquidBlockContainer) || !((LiquidBlockContainer) blockState.getBlock()).canPlaceLiquid(player, world, pos, blockState, fluid))) {
			return hitResult != null && this.placeFluid(player, world, hitResult.getBlockPos().relative(hitResult.getDirection()), null, filledCell);
		} else {
			//noinspection deprecation
			if (world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
				int i = pos.getX();
				int j = pos.getY();
				int k = pos.getZ();
				world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

				for (int l = 0; l < 8; ++l) {
					world.addParticle(ParticleTypes.LARGE_SMOKE, (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0D, 0.0D, 0.0D);
				}
			} else if (blockState.getBlock() instanceof LiquidBlockContainer && fluid == Fluids.WATER) {
				if (((LiquidBlockContainer) blockState.getBlock()).placeLiquid(world, pos, blockState, ((FlowingFluid) fluid).getSource(false))) {
					this.playEmptyingSound(player, world, pos, fluid);
				}
			} else {
				//noinspection deprecation
				if (!world.isClientSide && canPlace && !blockState.liquid()) {
					world.destroyBlock(pos, true);
				}

				this.playEmptyingSound(player, world, pos, fluid);
				world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 11);
			}
			return true;
		}
	}

	@Override
	public Component getName(ItemStack itemStack) {
		Fluid fluid = getFluid(itemStack);
		if (fluid != Fluids.EMPTY) {
			// TODO use translation keys for fluid and the cell https://fabric.asie.pl/wiki/tutorial:lang?s[]=translation might be useful
			return Component.literal(Component.translatable("item.techreborn.cell.fluid").getString().replace("$fluid$", FluidUtils.getFluidName(fluid)));
		}
		return super.getName(itemStack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		Fluid containedFluid = getFluid(stack);

		BlockHitResult hitResult = getPlayerPOVHitResult(world, player, containedFluid == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
			if (hitResult.getType() == HitResult.Type.MISS || !(containedFluid instanceof FlowingFluid || Fluids.EMPTY == containedFluid)) {
			return InteractionResultHolder.pass(stack);
		}
		if (hitResult.getType() != HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(stack);
		}

		BlockPos hitPos = hitResult.getBlockPos();
		if (!world.mayInteract(player, hitPos)) {
			return InteractionResultHolder.fail(stack);
		}

		Direction side = hitResult.getDirection();
		BlockPos placePos = hitPos.relative(side);
		if (!player.mayUseItemAt(placePos, side, stack)) {
			return InteractionResultHolder.fail(stack);
		}

		BlockState hitState = world.getBlockState(hitPos);

		if (containedFluid == Fluids.EMPTY) {
			if (!(hitState.getBlock() instanceof BucketPickup fluidDrainable)) {
				return InteractionResultHolder.fail(stack);
			}
			// This will give us bucket, not a cell
			ItemStack itemStack = fluidDrainable.pickupBlock(player, world, hitPos, hitState);
			if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemFluidInfo) {
				Fluid drainFluid = ((ItemFluidInfo) itemStack.getItem()).getFluid(itemStack);
				fluidDrainable.getPickupSound().ifPresent((sound) -> player.playSound(sound, 1.0F, 1.0F));
				world.gameEvent(player, GameEvent.FLUID_PICKUP, hitPos);
				// Replace bucket item with cell item
				itemStack = getCellWithFluid(drainFluid, 1);
				ItemStack resultStack = ItemUtils.createFilledResult(stack, player, itemStack, false);
				return InteractionResultHolder.sidedSuccess(resultStack, world.isClientSide());
			}
		} else {
			placePos = hitState.getBlock() instanceof LiquidBlockContainer ? hitPos : placePos;
			if (this.placeFluid(player, world, placePos, hitResult, stack)) {

				if (player.getAbilities().instabuild) {
					return InteractionResultHolder.success(stack);
				}

				if (stack.getCount() == 1) {
					stack = getEmpty();
				} else {
					stack.shrink(1);
					insertOrDropStack(player, getEmpty());
				}

				return InteractionResultHolder.success(stack);
			}
		}

		return InteractionResultHolder.fail(stack);
	}

	// ItemFluidInfo
	@Override
	public ItemStack getEmpty() {
		return new ItemStack(this);
	}

	@Override
	public ItemStack getFull(Fluid fluid) {
		return getCellWithFluid(fluid);
	}

	@Override
	public Fluid getFluid(ItemStack itemStack) {
		Holder<Fluid> fluidEntry = itemStack.getOrDefault(TRDataComponentTypes.FLUID, Fluids.EMPTY.builtInRegistryHolder());
		return fluidEntry.value();
	}

	public void registerFluidApi() {
		TransferApiBridge.registerDynamicCellFluidStorage(this, new DynamicCellFluidProvider(this));
	}
}
