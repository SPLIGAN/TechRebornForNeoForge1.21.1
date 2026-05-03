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

package reborncore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import reborncore.common.compat.neoforge.NeoForgeFuelRegistryBridge;
import reborncore.common.energy.impl.EnergyImpl;
import reborncore.common.misc.ModSounds;
import reborncore.common.recipes.PaddedShapedRecipe;
import reborncore.common.compat.neoforge.NeoForgeItemGroupBridge;
import reborncore.common.compat.neoforge.NeoForgeVillagerBridge;
import reborncore.common.network.Packets;
import reborncore.common.network.ServerBoundPackets;
import reborncore.client.RebornFluidRenderManager;
import reborncore.client.gui.ThemeManager;

@Mod(RebornCore.MOD_ID)
public final class RebornCoreNeoForge {

	public RebornCoreNeoForge(IEventBus modBus) {
		modBus.addListener(RegisterEvent.class, EnergyImpl::register);
		modBus.addListener(RegisterEvent.class, ModSounds::register);
		modBus.addListener(RegisterEvent.class, PaddedShapedRecipe::register);
		NeoForge.EVENT_BUS.addListener(NeoForgeFuelRegistryBridge::onFurnaceFuelBurnTime);
		NeoForge.EVENT_BUS.addListener(NeoForgeVillagerBridge::onWandererTrades);
		modBus.addListener(BuildCreativeModeTabContentsEvent.class, NeoForgeItemGroupBridge::onBuildCreativeTab);
		modBus.addListener(this::registerPayloads);
		modBus.addListener(this::commonSetup);
		if (FMLEnvironment.dist.isClient()) {
			modBus.addListener(this::clientSetup);
			modBus.addListener(this::registerClientReloadListeners);
		}
	}

	private void registerPayloads(RegisterPayloadHandlersEvent event) {
		var reg = event.registrar("1");
		Packets.register(reg);
		ServerBoundPackets.register(reg);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> new RebornCore().onInitialize());
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		event.enqueueWork(() -> new RebornCoreClient().onInitializeClient());
	}

	private void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
		RebornFluidRenderManager.bootstrap();
		event.registerReloadListener(new ThemeManager());
		event.registerReloadListener(new RebornFluidRenderManager());
	}
}
