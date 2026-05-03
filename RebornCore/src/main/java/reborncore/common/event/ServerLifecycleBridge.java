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

package reborncore.common.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import reborncore.common.multiblock.MultiblockRegistry;

import java.util.concurrent.CopyOnWriteArrayList;

public final class ServerLifecycleBridge {
	private static final CopyOnWriteArrayList<WorldTickHandler> START_WORLD_TICK = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<ServerTickHandler> START_SERVER_TICK = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<WorldLoadHandler> WORLD_LOAD = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<BlockEntityUnloadHandler> BLOCK_ENTITY_UNLOAD = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<EquipmentChangeHandler> EQUIPMENT_CHANGE = new CopyOnWriteArrayList<>();

	static {
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (LevelTickEvent.Pre e) -> {
			if (e.getLevel().isClientSide() || !(e.getLevel() instanceof ServerLevel sl)) {
				return;
			}
			for (WorldTickHandler h : START_WORLD_TICK) {
				h.onStartWorldTick(sl);
			}
		});
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (ServerTickEvent.Pre e) -> {
			for (ServerTickHandler h : START_SERVER_TICK) {
				h.onStartServerTick(e.getServer());
			}
		});
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (LevelEvent.Load e) -> {
			if (!(e.getLevel() instanceof ServerLevel sl)) {
				return;
			}
			MinecraftServer server = sl.getServer();
			for (WorldLoadHandler h : WORLD_LOAD) {
				h.onWorldLoad(server, sl);
			}
		});
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (ChunkEvent.Load e) -> {
			if (!(e.getLevel() instanceof ServerLevel sl)) {
				return;
			}
			MultiblockRegistry.onChunkLoaded(sl, e.getChunk());
		});
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (LevelEvent.Unload e) -> {
			if (!(e.getLevel() instanceof Level level) || level.isClientSide()) {
				return;
			}
			MultiblockRegistry.onWorldUnloaded(level);
		});
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (ChunkEvent.Unload e) -> {
			if (!(e.getLevel() instanceof Level level) || level.isClientSide() || !(e.getChunk() instanceof LevelChunk lc)) {
				return;
			}
			for (BlockEntity be : lc.getBlockEntities().values()) {
				for (BlockEntityUnloadHandler h : BLOCK_ENTITY_UNLOAD) {
					h.onBlockEntityUnload(be, level);
				}
			}
		});
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (LivingEquipmentChangeEvent e) -> {
			for (EquipmentChangeHandler h : EQUIPMENT_CHANGE) {
				h.onEquipmentChange(e.getEntity(), e.getSlot(), e.getFrom(), e.getTo());
			}
		});
	}

	private ServerLifecycleBridge() {
	}

	public static void onStartWorldTick(WorldTickHandler handler) {
		START_WORLD_TICK.add(handler);
	}

	public static void onStartServerTick(ServerTickHandler handler) {
		START_SERVER_TICK.add(handler);
	}

	public static void onWorldLoad(WorldLoadHandler handler) {
		WORLD_LOAD.add(handler);
	}

	public static void onBlockEntityUnload(BlockEntityUnloadHandler handler) {
		BLOCK_ENTITY_UNLOAD.add(handler);
	}

	public static void onEquipmentChange(EquipmentChangeHandler handler) {
		EQUIPMENT_CHANGE.add(handler);
	}

	@FunctionalInterface
	public interface WorldTickHandler {
		void onStartWorldTick(ServerLevel world);
	}

	@FunctionalInterface
	public interface WorldLoadHandler {
		void onWorldLoad(MinecraftServer server, ServerLevel world);
	}

	@FunctionalInterface
	public interface ServerTickHandler {
		void onStartServerTick(MinecraftServer server);
	}

	@FunctionalInterface
	public interface BlockEntityUnloadHandler {
		void onBlockEntityUnload(BlockEntity blockEntity, Level world);
	}

	@FunctionalInterface
	public interface EquipmentChangeHandler {
		void onEquipmentChange(LivingEntity livingEntity, EquipmentSlot equipmentSlot, ItemStack previousStack, ItemStack currentStack);
	}
}
