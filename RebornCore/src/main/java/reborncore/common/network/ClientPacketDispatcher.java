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

package reborncore.common.network;

import net.neoforged.neoforge.network.handling.IPayloadHandler;
import reborncore.common.network.clientbound.ChunkSyncPayload;
import reborncore.common.network.clientbound.CustomDescriptionPayload;
import reborncore.common.network.clientbound.FluidConfigSyncPayload;
import reborncore.common.network.clientbound.QueueItemStacksPayload;
import reborncore.common.network.clientbound.ScreenHandlerUpdatePayload;
import reborncore.common.network.clientbound.SlotSyncPayload;

/**
 * Play-phase client-bound handlers registered on the dedicated server as no-ops until
 * {@link reborncore.RebornCoreClient} assigns real implementations on the physical client.
 */
public final class ClientPacketDispatcher {
	private ClientPacketDispatcher() {
	}

	public static IPayloadHandler<ChunkSyncPayload> chunkSync = (payload, context) -> {
	};
	public static IPayloadHandler<CustomDescriptionPayload> customDescription = (payload, context) -> {
	};
	public static IPayloadHandler<FluidConfigSyncPayload> fluidConfigSync = (payload, context) -> {
	};
	public static IPayloadHandler<SlotSyncPayload> slotSync = (payload, context) -> {
	};
	public static IPayloadHandler<ScreenHandlerUpdatePayload> screenHandlerUpdate = (payload, context) -> {
	};
	public static IPayloadHandler<QueueItemStacksPayload> queueItemStacks = (payload, context) -> {
	};
}
