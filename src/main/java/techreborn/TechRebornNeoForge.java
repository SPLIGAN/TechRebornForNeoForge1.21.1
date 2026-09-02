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

package techreborn;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import techreborn.client.TechRebornNeoForgeClient;
import techreborn.client.keybindings.KeyBindings;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import techreborn.component.TRDataComponentTypes;
import techreborn.events.ModRegistry;
import techreborn.init.TRContent;
import techreborn.init.TechRebornCapabilities;
import techreborn.events.OreDepthSyncHandler;
import techreborn.events.TRRecipeHandler;
import techreborn.packets.Packets;
import techreborn.packets.ServerboundPackets;
import techreborn.client.ClientGuiType;
import techreborn.compat.pal.PlayerAbilityLibCompat;
import techreborn.datagen.TRBlockTagsProvider;
import techreborn.datagen.TRItemTagsProvider;
import techreborn.datagen.TRPointOfInterestTagsProvider;
import techreborn.world.TRDynamicRegistries;
import techreborn.world.compat.neoforge.NeoForgeBiomeModifierPack;

@Mod(TechReborn.MOD_ID)
public final class TechRebornNeoForge {

	public static final RegistrySetBuilder DATAPACK_ENTRIES = new RegistrySetBuilder()
		.add(Registries.DAMAGE_TYPE, TRDynamicRegistries::damageTypes)
		.add(Registries.CONFIGURED_FEATURE, TRDynamicRegistries::configuredFeatures)
		.add(Registries.PLACED_FEATURE, TRDynamicRegistries::placedFeatures);

	public TechRebornNeoForge(IEventBus modBus) {
		modBus.addListener(RegisterEvent.class, TRContent::registerEntityTypes);
		modBus.addListener(RegisterEvent.class, ModRegistry::registerNeoForge);
		modBus.addListener(RegisterEvent.class, TRDataComponentTypes::register);
		modBus.addListener(AddPackFindersEvent.class, NeoForgeBiomeModifierPack::register);
		modBus.addListener(GatherDataEvent.Server.class, TechRebornNeoForge::gatherServerData);
		modBus.addListener(GatherDataEvent.Client.class, TechRebornNeoForge::gatherClientData);
		modBus.addListener(RegisterGameTestsEvent.class, TechRebornNeoForge::registerGameTests);
		modBus.addListener(TechRebornCapabilities::register);
		modBus.addListener(this::registerPayloads);
		modBus.addListener(OreDepthSyncHandler::registerConfigurationTasks);
		modBus.addListener(this::commonSetup);
		if (FMLEnvironment.getDist().isClient()) {
			TechRebornNeoForgeClient.subscribeModBus(modBus);
			TechRebornNeoForgeClient.subscribeGameBus();
			modBus.addListener(RegisterKeyMappingsEvent.class, KeyBindings::registerKeys);
			modBus.addListener((RegisterMenuScreensEvent e) -> ClientGuiType.registerMenuScreens(e));
			modBus.addListener(this::clientSetup);
		}
	}

	private static void gatherServerData(GatherDataEvent.Server event) {
		event.createDatapackRegistryObjects(DATAPACK_ENTRIES);
		event.createProvider(TRItemTagsProvider::new);
		event.createProvider(TRBlockTagsProvider::new);
		event.createProvider(TRPointOfInterestTagsProvider::new);
		// Recipe Groovy providers live on the datagen source set (present for runServerData).
		try {
			Class.forName("techreborn.datagen.TechRebornRecipeDatagen")
				.getMethod("gatherServerData", GatherDataEvent.Server.class)
				.invoke(null, event);
		} catch (ClassNotFoundException ignored) {
			// Production / non-datagen runs do not ship the datagen source set.
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to register TechReborn recipe datagen", e);
		}
	}

	private static void gatherClientData(GatherDataEvent.Client event) {
		try {
			Class.forName("techreborn.datagen.TechRebornRecipeDatagen")
				.getMethod("gatherClientData", GatherDataEvent.Client.class)
				.invoke(null, event);
		} catch (ClassNotFoundException ignored) {
			// Production / non-datagen runs do not ship the datagen source set.
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to register TechReborn client datagen", e);
		}
	}

	private static void registerGameTests(RegisterGameTestsEvent event) {
		try {
			Class.forName("techreborn.test.TRGameTestRegistration")
				.getMethod("register", RegisterGameTestsEvent.class)
				.invoke(null, event);
		} catch (ClassNotFoundException ignored) {
			// Production / non-gametest runs do not ship the gametest source set.
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to register TechReborn game tests", e);
		}
	}

	private void registerPayloads(RegisterPayloadHandlersEvent event) {
		var reg = event.registrar("1");
		Packets.register(reg);
		ServerboundPackets.register(reg);
		OreDepthSyncHandler.registerConfigurationPayload(reg);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			new PlayerAbilityLibCompat().onInitialize();
			new TechReborn().onInitialize();
			TRRecipeHandler.registerNeoForge();
		});
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		event.enqueueWork(() -> new TechRebornClient().onInitializeClient());
	}
}
