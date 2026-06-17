/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2026 TechReborn
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

package techreborn.blockentity.generator.nuclear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jspecify.annotations.Nullable;

import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.api.EnergyStorageUtil;
import reborncore.common.transfer.RcTransactionContext;
import techreborn.init.TRBlockEntities;

public class ReactorChamberBlockEntity extends BlockEntity {

	@Nullable
	private BlockPos linkedReactorPos = null;

	/**
	 * Energy storage proxy that delegates to the linked reactor.
	 * This allows cables to extract energy from the reactor through the chamber.
	 */
	private final EnergyStorage energyProxy = new EnergyStorage() {
		@Override
		public long insert(long maxAmount, RcTransactionContext transaction) {
			return 0;
		}

		@Override
		public long extract(long maxAmount, RcTransactionContext transaction) {
			NuclearReactorBlockEntity reactor = getLinkedReactor();
			if (reactor == null) {
				return 0;
			}
			EnergyStorage reactorStorage = reactor.getSideEnergyStorage(null);
			if (reactorStorage != null) {
				return reactorStorage.extract(maxAmount, transaction);
			}
			return 0;
		}

		@Override
		public long getAmount() {
			NuclearReactorBlockEntity reactor = getLinkedReactor();
			if (reactor == null) {
				return 0;
			}
			return reactor.getStored();
		}

		@Override
		public long getCapacity() {
			NuclearReactorBlockEntity reactor = getLinkedReactor();
			if (reactor == null) {
				return 0;
			}
			return reactor.getMaxStoredPower();
		}

		@Override
		public boolean supportsInsertion() {
			return false;
		}

		@Override
		public boolean supportsExtraction() {
			return getLinkedReactor() != null;
		}
	};

	public ReactorChamberBlockEntity(BlockPos pos, BlockState state) {
		super(TRBlockEntities.REACTOR_CHAMBER, pos, state);
	}

	/**
	 * Push energy from the reactor to adjacent blocks each tick.
	 */
	public static void tick(Level level, BlockPos pos, BlockState state, ReactorChamberBlockEntity chamber) {
		if (level.isClientSide()) {
			return;
		}

		if (chamber.getLinkedReactor() == null) {
			return;
		}

		for (Direction side : Direction.values()) {
			EnergyStorage source = chamber.getSideEnergyStorage(side);
			if (source == null) {
				continue;
			}

			EnergyStorageUtil.move(
					source,
					EnergyStorage.findSided(level, pos.relative(side), side.getOpposite()),
					Long.MAX_VALUE,
					null
			);
		}
	}

	@Nullable
	public BlockPos getLinkedReactorPos() {
		return linkedReactorPos;
	}

	public void setLinkedReactorPos(@Nullable BlockPos pos) {
		this.linkedReactorPos = pos;
		setChanged();
		if (level != null) {
			level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
		}
	}

	@Nullable
	public NuclearReactorBlockEntity getLinkedReactor() {
		if (linkedReactorPos == null || level == null) {
			return null;
		}
		BlockEntity be = level.getBlockEntity(linkedReactorPos);
		if (be instanceof NuclearReactorBlockEntity reactor) {
			return reactor;
		}
		return null;
	}

	@Nullable
	public EnergyStorage getSideEnergyStorage(@Nullable Direction side) {
		if (linkedReactorPos != null && getLinkedReactor() != null && side != null) {
			BlockPos sidePos = worldPosition.relative(side);
			if (sidePos.equals(linkedReactorPos)) {
				return null;
			}
			return energyProxy;
		}
		return null;
	}

	@Override
	public void saveAdditional(ValueOutput view) {
		super.saveAdditional(view);
		if (linkedReactorPos != null) {
			view.putInt("LinkedReactorX", linkedReactorPos.getX());
			view.putInt("LinkedReactorY", linkedReactorPos.getY());
			view.putInt("LinkedReactorZ", linkedReactorPos.getZ());
		}
	}

	@Override
	public void loadAdditional(ValueInput view) {
		super.loadAdditional(view);
		int x = view.getIntOr("LinkedReactorX", Integer.MIN_VALUE);
		if (x != Integer.MIN_VALUE) {
			int y = view.getIntOr("LinkedReactorY", 0);
			int z = view.getIntOr("LinkedReactorZ", 0);
			linkedReactorPos = new BlockPos(x, y, z);
		} else {
			linkedReactorPos = null;
		}
	}
}
