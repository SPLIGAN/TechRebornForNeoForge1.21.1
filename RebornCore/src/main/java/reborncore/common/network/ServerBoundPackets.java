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

package reborncore.common.network;

import org.apache.commons.lang3.Validate;
import reborncore.common.blockentity.FluidConfiguration;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.blockentity.SlotConfiguration;
import reborncore.common.chunkloading.ChunkLoaderManager;
import reborncore.common.network.clientbound.FluidConfigSyncPayload;
import reborncore.common.network.clientbound.SlotSyncPayload;
import reborncore.common.network.serverbound.ChunkLoaderRequestPayload;
import reborncore.common.network.serverbound.FluidConfigSavePayload;
import reborncore.common.network.serverbound.FluidIoSavePayload;
import reborncore.common.network.serverbound.IoSavePayload;
import reborncore.common.network.serverbound.SetRedstoneStatePayload;
import reborncore.common.network.serverbound.SlotConfigSavePayload;
import reborncore.common.network.serverbound.SlotSavePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ServerBoundPackets {

	public static void register(PayloadRegistrar reg) {
		reg.playToServer(FluidConfigSavePayload.ID, FluidConfigSavePayload.PACKET_CODEC, (payload, context) -> {
			var machine = payload.getBlockEntity(MachineBaseBlockEntity.class, context.player());
			machine.fluidConfiguration.updateFluidConfig(payload.fluidConfiguration());
			machine.setChanged();

			NetworkManager.sendToTracking(new FluidConfigSyncPayload(payload.pos(), machine.fluidConfiguration), machine);

			Level world = machine.getLevel();
			BlockState blockState = world.getBlockState(machine.getBlockPos());
			world.updateNeighborsAt(machine.getBlockPos(), blockState.getBlock());
		});

		reg.playToServer(SlotConfigSavePayload.ID, SlotConfigSavePayload.PACKET_CODEC, (payload, context) -> {
			var machine = payload.getBlockEntity(MachineBaseBlockEntity.class, context.player());
			for (SlotConfiguration.SlotConfigHolder slotDetail : payload.slotConfig().getSlotDetails()) {
				machine.getSlotConfiguration().updateSlotDetails(slotDetail);
			}
			machine.setChanged();

			NetworkManager.sendToWorld(new SlotSyncPayload(payload.pos(), machine.getSlotConfiguration()), (ServerLevel) machine.getLevel());
		});

		reg.playToServer(FluidIoSavePayload.ID, FluidIoSavePayload.PACKET_CODEC, (payload, context) -> {
			var machine = payload.getBlockEntity(MachineBaseBlockEntity.class, context.player());
			FluidConfiguration config = machine.fluidConfiguration;
			if (config == null) {
				return;
			}
			config.setInput(payload.input());
			config.setOutput(payload.output());

			NetworkManager.sendToTracking(new FluidConfigSyncPayload(payload.pos(), machine.fluidConfiguration), machine);
		});

		reg.playToServer(IoSavePayload.ID, IoSavePayload.PACKET_CODEC, (payload, context) -> {
			var machine = payload.getBlockEntity(MachineBaseBlockEntity.class, context.player());
			Validate.notNull(machine, "machine cannot be null");
			SlotConfiguration.SlotConfigHolder holder = machine.getSlotConfiguration().getSlotDetails(payload.slotID());
			if (holder == null) {
				return;
			}

			holder.setInput(payload.input());
			holder.setOutput(payload.output());
			holder.setFilter(payload.filter());
			holder.setPriority(payload.priority());

			NetworkManager.sendToAll(new SlotSyncPayload(payload.pos(), machine.getSlotConfiguration()), context.player().level().getServer());
		});

		reg.playToServer(SlotSavePayload.ID, SlotSavePayload.PACKET_CODEC, (payload, context) -> {
			var machine = payload.getBlockEntity(MachineBaseBlockEntity.class, context.player());
			machine.getSlotConfiguration().getSlotDetails(payload.slotConfig().getSlotID()).updateSlotConfig(payload.slotConfig());
			machine.setChanged();

			NetworkManager.sendToWorld(new SlotSyncPayload(payload.pos(), machine.getSlotConfiguration()), (ServerLevel) machine.getLevel());
		});

		reg.playToServer(ChunkLoaderRequestPayload.ID, ChunkLoaderRequestPayload.PACKET_CODEC, (payload, context) -> {
			var player = (ServerPlayer) context.player();
			payload.getBlockEntity(MachineBaseBlockEntity.class, player);
			ChunkLoaderManager chunkLoaderManager = ChunkLoaderManager.get(player.level());
			chunkLoaderManager.syncChunkLoaderToClient(player, payload.pos());
		});

		reg.playToServer(SetRedstoneStatePayload.ID, SetRedstoneStatePayload.CODEC, (payload, context) -> {
			var machine = payload.getBlockEntity(MachineBaseBlockEntity.class, context.player());
			machine.setRedstoneConfiguration(machine.getRedstoneConfiguration().withState(payload.element(), payload.state()));
		});
	}
}
