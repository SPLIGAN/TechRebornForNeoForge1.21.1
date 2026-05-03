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

package reborncore.common.energy.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import reborncore.common.transfer.RcTransactionContext;
import reborncore.common.energy.capability.TeamRebornEnergyCapabilities;
import reborncore.common.energy.impl.EmptyEnergyStorage;
import reborncore.common.energy.impl.EnergyImpl;

import java.util.Objects;

@SuppressWarnings({"unused"})
public interface EnergyStorage {
	EnergyStorage EMPTY = Objects.requireNonNull(EmptyEnergyStorage.EMPTY);

	DataComponentType<Long> ENERGY_COMPONENT = Objects.requireNonNull(EnergyImpl.ENERGY_COMPONENT);

	static EnergyStorage findSided(Level level, BlockPos pos, @Nullable Direction face) {
		return TeamRebornEnergyCapabilities.findSided(level, pos, face);
	}

	default boolean supportsInsertion() {
		return true;
	}

	long insert(long maxAmount, RcTransactionContext transaction);

	default boolean supportsExtraction() {
		return true;
	}

	long extract(long maxAmount, RcTransactionContext transaction);

	long getAmount();

	long getCapacity();
}
