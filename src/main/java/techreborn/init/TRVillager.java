/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2022 TechReborn
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

package techreborn.init;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import techreborn.TechReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.neoforge.registries.RegisterEvent;
import techreborn.init.ModSounds;

public class TRVillager {

	public static final Identifier METALLURGIST_ID = Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, "metallurgist");
	public static final Identifier ELECTRICIAN_ID = Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, "electrician");

	public static PoiType METALLURGIST_POI;
	public static PoiType ELECTRICIAN_POI;

	public static VillagerProfession METALLURGIST_PROFESSION;
	public static VillagerProfession ELECTRICIAN_PROFESSION;

	public static void registerPoiTypes(RegisterEvent event) {
		event.register(Registries.POINT_OF_INTEREST_TYPE, METALLURGIST_ID, () -> {
			METALLURGIST_POI = VillagerBridge.createPoi(METALLURGIST_ID, 1, 1, TRContent.Machine.IRON_ALLOY_FURNACE.block);
			return METALLURGIST_POI;
		});
		event.register(Registries.POINT_OF_INTEREST_TYPE, ELECTRICIAN_ID, () -> {
			ELECTRICIAN_POI = VillagerBridge.createPoi(ELECTRICIAN_ID, 1, 1, TRContent.Machine.SOLID_FUEL_GENERATOR.block);
			return ELECTRICIAN_POI;
		});
	}

	public static void registerProfessions(RegisterEvent event) {
		event.register(Registries.VILLAGER_PROFESSION, METALLURGIST_ID, () -> {
			METALLURGIST_PROFESSION = VillagerBridge.buildProfession(
				METALLURGIST_ID,
				ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, METALLURGIST_ID),
				SoundEvents.VILLAGER_WORK_TOOLSMITH,
				tradeSetsFor(METALLURGIST_ID)
			);
			return METALLURGIST_PROFESSION;
		});
		event.register(Registries.VILLAGER_PROFESSION, ELECTRICIAN_ID, () -> {
			ELECTRICIAN_PROFESSION = VillagerBridge.buildProfession(
				ELECTRICIAN_ID,
				ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ELECTRICIAN_ID),
				ModSounds.CABLE_SHOCK,
				tradeSetsFor(ELECTRICIAN_ID)
			);
			return ELECTRICIAN_PROFESSION;
		});
	}

	private static Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsFor(Identifier profession) {
		Int2ObjectMap<ResourceKey<TradeSet>> tradeSets = new Int2ObjectArrayMap<>();
		for (int level = 1; level <= 5; level++) {
			tradeSets.put(
				level,
				ResourceKey.create(
					Registries.TRADE_SET,
					Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, profession.getPath() + "/level_" + level)
				)
			);
		}
		return tradeSets;
	}

	private TRVillager() {
	}

	/** Trades are datapack-driven (VIL-01): {@code data/techreborn/villager_trade/} + {@code trade_set/}. */
	public static void registerVillagerTrades() {
	}

	/** Wandering trader rubber sapling is appended via {@code data/minecraft/tags/villager_trade/} (VIL-02). */
	public static void registerWanderingTraderTrades() {
	}

	public static void registerVillagerHouses() {
		TechReborn.LOGGER.debug("TRVillager.registerVillagerHouses: structure pool append skipped on NeoForge (private pool templates).");
	}
}
