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

package techreborn.packets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import techreborn.blockentity.GuiType;
import techreborn.blockentity.machine.tier1.ElevatorBlockEntity;
import techreborn.component.TRDataComponentTypes;
import techreborn.config.TechRebornConfig;
import techreborn.init.TRBlockEntities;
import techreborn.init.TRContent;
import techreborn.packets.serverbound.AESUConfigPayload;
import techreborn.packets.serverbound.AutoCraftingLockPayload;
import techreborn.packets.serverbound.ChunkloaderPayload;
import techreborn.packets.serverbound.DetectorRadiusPayload;
import techreborn.packets.serverbound.ExperiencePayload;
import techreborn.packets.serverbound.FusionControlSizePayload;
import techreborn.packets.serverbound.JumpPayload;
import techreborn.packets.serverbound.LaunchSpeedPayload;
import techreborn.packets.serverbound.PumpDepthPayload;
import techreborn.packets.serverbound.PumpRangePayload;
import techreborn.packets.serverbound.QuantumSuitSprintPayload;
import techreborn.packets.serverbound.RefundPayload;
import techreborn.packets.serverbound.RollingMachineLockPayload;
import techreborn.packets.serverbound.StorageUnitLockPayload;
import techreborn.packets.serverbound.SuitNightVisionPayload;

public class ServerboundPackets {
	public static void register(PayloadRegistrar reg) {
		reg.playToServer(AESUConfigPayload.ID, AESUConfigPayload.CODEC, (payload, context) -> {
			var aesu = GuiType.AESU.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.ADJUSTABLE_SU);
			aesu.handleGuiInputFromClient(payload.buttonID(), payload.shift(), payload.ctrl());
		});

		reg.playToServer(AutoCraftingLockPayload.ID, AutoCraftingLockPayload.CODEC, (payload, context) -> {
			var autoCraftingTable = GuiType.AUTO_CRAFTING_TABLE.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.AUTO_CRAFTING_TABLE);
			autoCraftingTable.locked = payload.locked();
		});

		reg.playToServer(RollingMachineLockPayload.ID, RollingMachineLockPayload.CODEC, (payload, context) -> {
			var rollingMachine = GuiType.ROLLING_MACHINE.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.ROLLING_MACHINE);
			rollingMachine.locked = payload.locked();
		});

		reg.playToServer(StorageUnitLockPayload.ID, StorageUnitLockPayload.CODEC, (payload, context) -> {
			var storageUnit = GuiType.STORAGE_UNIT.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.STORAGE_UNIT);
			storageUnit.setLocked(payload.locked());
		});

		reg.playToServer(FusionControlSizePayload.ID, FusionControlSizePayload.CODEC, (payload, context) -> {
			var fusionControlComputer = GuiType.FUSION_CONTROLLER.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.FUSION_CONTROL_COMPUTER);
			fusionControlComputer.changeSize(payload.sizeDelta());
		});

		reg.playToServer(RefundPayload.ID, RefundPayload.CODEC, (payload, context) -> {
			if (!TechRebornConfig.allowManualRefund) {
				return;
			}
			ServerPlayer player = (ServerPlayer) context.player();
			Inventory inventory = player.getInventory();
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				ItemStack stack = inventory.getItem(i);
				if (stack.is(TRContent.MANUAL)) {
					inventory.setItem(i, ItemStack.EMPTY);
					inventory.add(new ItemStack(Items.BOOK));
					inventory.add(TRContent.Ingots.REFINED_IRON.getStack());
					return;
				}
			}
		});

		reg.playToServer(ChunkloaderPayload.ID, ChunkloaderPayload.CODEC, (payload, context) -> {
			var chunkLoader = GuiType.CHUNK_LOADER.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.CHUNK_LOADER);
			chunkLoader.handleGuiInputFromClient(payload.buttonID(), payload.sync() ? (ServerPlayer) context.player() : null);
		});

		reg.playToServer(ExperiencePayload.ID, ExperiencePayload.CODEC, (payload, context) -> {
			var ironFurnace = GuiType.IRON_FURNACE.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.IRON_FURNACE);
			ironFurnace.handleGuiInputFromClient((ServerPlayer) context.player());
		});

		reg.playToServer(DetectorRadiusPayload.ID, DetectorRadiusPayload.CODEC, (payload, context) -> {
			var playerDetector = GuiType.PLAYER_DETECTOR.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.PLAYER_DETECTOR);
			playerDetector.handleGuiInputFromClient(payload.buttonAmount());
		});

		reg.playToServer(LaunchSpeedPayload.ID, LaunchSpeedPayload.CODEC, (payload, context) -> {
			var launchpad = GuiType.LAUNCHPAD.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.LAUNCHPAD);
			launchpad.handleGuiInputFromClient(payload.buttonAmount());
		});

		reg.playToServer(PumpDepthPayload.ID, PumpDepthPayload.CODEC, (payload, context) -> {
			var pump = GuiType.PUMP.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.PUMP);
			pump.handleDepthGuiInputFromClient(payload.buttonAmount());
		});

		reg.playToServer(PumpRangePayload.ID, PumpRangePayload.CODEC, (payload, context) -> {
			var pump = GuiType.PUMP.getBlockEntity((ServerPlayer) context.player(), payload, TRBlockEntities.PUMP);
			pump.handleRangeGuiInputFromClient(payload.buttonAmount());
		});

		reg.playToServer(JumpPayload.ID, JumpPayload.CODEC, (payload, context) -> {
			ServerPlayer player = (ServerPlayer) context.player();
			MachineBaseBlockEntity legacyMachineBase = (MachineBaseBlockEntity) player.level().getBlockEntity(payload.pos().below());
			if (legacyMachineBase instanceof ElevatorBlockEntity elevator) {
				elevator.teleportUp(player);
			}
		});

		reg.playToServer(SuitNightVisionPayload.ID, SuitNightVisionPayload.CODEC, (payload, context) -> {
			ServerPlayer player = (ServerPlayer) context.player();
			for (ItemStack itemStack : player.getArmorSlots()) {
				if (itemStack.is(TRContent.NANO_HELMET) || itemStack.is(TRContent.QUANTUM_HELMET)) {
					itemStack.set(TRDataComponentTypes.IS_ACTIVE, !itemStack.getOrDefault(TRDataComponentTypes.IS_ACTIVE, false));
					break;
				}
			}
		});

		reg.playToServer(QuantumSuitSprintPayload.ID, QuantumSuitSprintPayload.CODEC, (payload, context) -> {
			ServerPlayer player = (ServerPlayer) context.player();
			for (ItemStack itemStack : player.getArmorSlots()) {
				if (itemStack.is(TRContent.QUANTUM_LEGGINGS)) {
					itemStack.set(TRDataComponentTypes.IS_ACTIVE, !itemStack.getOrDefault(TRDataComponentTypes.IS_ACTIVE, false));
					break;
				}
			}
		});
	}
}
