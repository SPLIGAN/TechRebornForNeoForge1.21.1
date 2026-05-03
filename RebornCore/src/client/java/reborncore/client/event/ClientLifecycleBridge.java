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

package reborncore.client.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.concurrent.CopyOnWriteArrayList;

public final class ClientLifecycleBridge {
	private static final CopyOnWriteArrayList<HudRenderCallback> hudRender = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<TooltipAppender> tooltipAppenders = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<java.util.function.Consumer<RenderHighlightEvent.Block>> blockOutline = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<BlockEntityUnloadCallback> blockEntityUnload = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<ClientStartedCallback> clientStarted = new CopyOnWriteArrayList<>();
	private static final CopyOnWriteArrayList<EndClientTickCallback> endClientTick = new CopyOnWriteArrayList<>();

	private static volatile boolean wired;
	private static volatile boolean clientStartedFired;

	private ClientLifecycleBridge() {
	}

	private static void ensureWired() {
		if (wired) {
			return;
		}
		synchronized (ClientLifecycleBridge.class) {
			if (wired) {
				return;
			}
			wired = true;
			NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (RenderGuiEvent.Post e) -> {
				for (HudRenderCallback c : hudRender) {
					c.onHudRender(e.getGuiGraphics(), e.getPartialTick());
				}
			});
			NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (ItemTooltipEvent e) -> {
				for (TooltipAppender a : tooltipAppenders) {
					a.append(e.getItemStack(), e.getContext(), e.getFlags(), e.getToolTip());
				}
			});
			NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (RenderHighlightEvent.Block e) -> {
				for (java.util.function.Consumer<RenderHighlightEvent.Block> c : blockOutline) {
					c.accept(e);
				}
			});
			NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (ChunkEvent.Unload e) -> {
				if (!(e.getLevel() instanceof net.minecraft.world.level.Level level) || !level.isClientSide() || !(e.getChunk() instanceof LevelChunk lc)) {
					return;
				}
				for (BlockEntity be : lc.getBlockEntities().values()) {
					for (BlockEntityUnloadCallback c : blockEntityUnload) {
						c.onUnload(be, level);
					}
				}
			});
			NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (ClientTickEvent.Post e) -> {
				Minecraft client = Minecraft.getInstance();
				if (!clientStartedFired) {
					clientStartedFired = true;
					for (ClientStartedCallback c : clientStarted) {
						c.onClientStarted(client);
					}
				}
				for (EndClientTickCallback c : endClientTick) {
					c.onEndClientTick(client);
				}
			});
		}
	}

	public static void onHudRender(HudRenderCallback callback) {
		ensureWired();
		hudRender.add(callback);
	}

	public static void registerTooltipAppender(TooltipAppender appender) {
		ensureWired();
		tooltipAppenders.add(appender);
	}

	public static void onBlockOutline(java.util.function.Consumer<RenderHighlightEvent.Block> callback) {
		ensureWired();
		blockOutline.add(callback);
	}

	public static void onBlockEntityUnload(BlockEntityUnloadCallback callback) {
		ensureWired();
		blockEntityUnload.add(callback);
	}

	public static void onClientStarted(ClientStartedCallback callback) {
		ensureWired();
		clientStarted.add(callback);
	}

	public static void onEndClientTick(EndClientTickCallback callback) {
		ensureWired();
		endClientTick.add(callback);
	}

	public static void registerBuiltinResourcePack(ResourceLocation id, net.neoforged.fml.ModContainer modContainer) {
		// Optional packs: use AddPackFindersEvent from mod bus when needed.
	}

	@FunctionalInterface
	public interface HudRenderCallback {
		void onHudRender(GuiGraphics guiGraphics, DeltaTracker partialTick);
	}

	@FunctionalInterface
	public interface BlockEntityUnloadCallback {
		void onUnload(BlockEntity blockEntity, net.minecraft.world.level.Level world);
	}

	@FunctionalInterface
	public interface ClientStartedCallback {
		void onClientStarted(Minecraft client);
	}

	@FunctionalInterface
	public interface EndClientTickCallback {
		void onEndClientTick(Minecraft client);
	}
}
