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

package techreborn.blockentity.storage.energy.idsu;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import reborncore.common.util.NBTSerializable;
import reborncore.common.energy.api.EnergyStorage;
import reborncore.common.energy.api.base.SimpleEnergyStorage;
import techreborn.config.TechRebornConfig;

import java.util.HashMap;

public class IDSUManager extends SavedData {
	private static final SavedData.Factory<IDSUManager> TYPE = new Factory<>(IDSUManager::new, IDSUManager::createFromTag, null);
	private static final String KEY = "techreborn_idsu";

	private IDSUManager() {
	}

	@NotNull
	public static IDSUPlayer getPlayer(MinecraftServer server, String uuid) {
		return get(server).getPlayer(uuid);
	}

	private static IDSUManager get(MinecraftServer server) {
		ServerLevel serverWorld = server.getLevel(Level.OVERWORLD);
		return serverWorld.getDataStorage().computeIfAbsent(TYPE, KEY);
	}

	private final HashMap<String, IDSUPlayer> playerHashMap = new HashMap<>();

	@NotNull
	public IDSUPlayer getPlayer(String uuid) {
		return playerHashMap.computeIfAbsent(uuid, s -> new IDSUPlayer());
	}

	public static IDSUManager createFromTag(CompoundTag tag, HolderLookup.Provider registryLookup) {
		IDSUManager	idsuManager = new IDSUManager();
		idsuManager.fromTag(tag);
		return idsuManager;
	}

	public void fromTag(CompoundTag tag) {
		for (String uuid : tag.getAllKeys()) {
			playerHashMap.put(uuid, new IDSUPlayer(tag.getCompound(uuid)));
		}
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registryLookup) {
		playerHashMap.forEach((uuid, player) -> tag.put(uuid, player.write()));
		return tag;
	}

	public class IDSUPlayer implements NBTSerializable {
		// This storage is never exposed directly, it's always wrapped behind getMaxInput()/getMaxOutput() checks
		private final SimpleEnergyStorage storage = new SimpleEnergyStorage(TechRebornConfig.idsuMaxEnergy, Long.MAX_VALUE, Long.MAX_VALUE) {
			@Override
			protected void onSnapshotCommitted() {
				setDirty();
			}
		};

		private IDSUPlayer() {
		}

		private IDSUPlayer(CompoundTag compoundTag) {
			read(compoundTag);
		}

		@NotNull
		@Override
		public CompoundTag write() {
			CompoundTag tag = new CompoundTag();
			tag.putLong("energy", storage.amount);
			return tag;
		}

		@Override
		public void read(@NotNull CompoundTag tag) {
			storage.amount = tag.getLong("energy");
		}

		public EnergyStorage getStorage() {
			return storage;
		}

		public long getEnergy() {
			return storage.amount;
		}

		public void setEnergy(long energy) {
			storage.amount = energy;
			setDirty();
		}
	}

}
