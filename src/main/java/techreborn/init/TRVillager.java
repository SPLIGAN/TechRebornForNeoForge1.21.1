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

import reborncore.common.util.TradeUtils;
import techreborn.TechReborn;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.RegisterEvent;

public class TRVillager {

	public static final ResourceLocation METALLURGIST_ID = ResourceLocation.fromNamespaceAndPath(TechReborn.MOD_ID, "metallurgist");
	public static final ResourceLocation ELECTRICIAN_ID = ResourceLocation.fromNamespaceAndPath(TechReborn.MOD_ID, "electrician");

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
				SoundEvents.VILLAGER_WORK_TOOLSMITH
			);
			return METALLURGIST_PROFESSION;
		});
		event.register(Registries.VILLAGER_PROFESSION, ELECTRICIAN_ID, () -> {
			ELECTRICIAN_PROFESSION = VillagerBridge.buildProfession(
				ELECTRICIAN_ID,
				ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ELECTRICIAN_ID),
				ModSounds.CABLE_SHOCK
			);
			return ELECTRICIAN_PROFESSION;
		});
	}

	private TRVillager() {/* No instantiation. */}

	public static void registerVillagerTrades() {
		// metallurgist
		TradeUtils.registerTradesForLevel(METALLURGIST_PROFESSION, TradeUtils.Level.NOVICE, false,
			TradeUtils.createBuy(TRContent.RawMetals.TIN, 1, 6, 12, 2),
			TradeUtils.createBuy(TRContent.RawMetals.LEAD, 1, 4, 12, 2),
			TradeUtils.createSell(TRContent.Parts.RUBBER, 2, 3, 16, 2)
		);
		TradeUtils.registerTradesForLevel(METALLURGIST_PROFESSION, TradeUtils.Level.APPRENTICE, false,
			TradeUtils.createSell(TRContent.Ingots.BRONZE, 2, 1, 12, 10),
			TradeUtils.createSell(TRContent.Ingots.BRASS, 5, 1, 12, 10),
			TradeUtils.createSell(TRContent.Parts.ELECTRONIC_CIRCUIT, 3, 2, 12, 10)
		);
		TradeUtils.registerTradesForLevel(METALLURGIST_PROFESSION, TradeUtils.Level.JOURNEYMAN, false,
			TradeUtils.createSell(TRContent.Ingots.ELECTRUM, 7, 3, 12, 20),
			TradeUtils.createBuy(TRContent.Parts.CARBON_FIBER, 1, 3, 12, 20)
		);
		TradeUtils.registerTradesForLevel(METALLURGIST_PROFESSION, TradeUtils.Level.EXPERT, false,
			TradeUtils.createSell(TRContent.Ingots.ADVANCED_ALLOY, 7, 4, 12, 20),
			TradeUtils.createBuy(TRContent.Ingots.NICKEL, 1, 1, 12, 30)
		);
		TradeUtils.registerTradesForLevel(METALLURGIST_PROFESSION, TradeUtils.Level.MASTER, false,
			TradeUtils.createSell(TRContent.Parts.ADVANCED_CIRCUIT, 7, 3, 12, 30)
		);
		// electrician
		TradeUtils.registerTradesForLevel(ELECTRICIAN_PROFESSION, TradeUtils.Level.NOVICE, false,
			TradeUtils.createBuy(TRContent.Parts.RUBBER, 1, 6, 12, 2),
			TradeUtils.createBuy(Items.COPPER_INGOT, 1, 3, 12, 2),
			TradeUtils.createSell(TRContent.Cables.INSULATED_COPPER, 1, 3, 12, 2)
		);
		TradeUtils.registerTradesForLevel(ELECTRICIAN_PROFESSION, TradeUtils.Level.APPRENTICE, false,
			TradeUtils.createBuy(Items.GOLD_INGOT, 1, 4, 12, 10),
			TradeUtils.createSell(TRContent.Cables.INSULATED_GOLD, 5, 3, 12, 10),
			TradeUtils.createSell(TRContent.Parts.ELECTRONIC_CIRCUIT, 3, 2, 12, 10)
		);
		TradeUtils.registerTradesForLevel(ELECTRICIAN_PROFESSION, TradeUtils.Level.JOURNEYMAN, false,
			TradeUtils.createBuy(TRContent.RED_CELL_BATTERY, 1, 1, 12, 20),
			TradeUtils.createSell(TRContent.Machine.LOW_VOLTAGE_SU, 8, 1, 12, 20),
			TradeUtils.createSell(TRContent.Machine.SOLID_FUEL_GENERATOR, 8, 1, 12, 20)
		);
		TradeUtils.registerTradesForLevel(ELECTRICIAN_PROFESSION, TradeUtils.Level.EXPERT, false,
			TradeUtils.createSell(TRContent.Parts.ADVANCED_CIRCUIT, 7, 3, 12, 20),
			TradeUtils.createBuy(TRContent.Gems.RUBY, 1, 6, 12, 20),
			TradeUtils.createSell(TRContent.Cables.GLASSFIBER, 4, 1, 8, 30)
		);
		TradeUtils.registerTradesForLevel(ELECTRICIAN_PROFESSION, TradeUtils.Level.MASTER, false,
			TradeUtils.createSell(TRContent.Machine.LAMP_LED, 8, 1, 12, 20),
			TradeUtils.createSell(TRContent.LITHIUM_ION_BATTERY, 30, 1, 8, 30)
		);
	}

	public static void registerWanderingTraderTrades() {
		List<VillagerTrades.ItemListing> extraCommonTrades = new LinkedList<>();
		List<VillagerTrades.ItemListing> extraRareTrades = new LinkedList<>();
		// specify extra trades below here
		extraCommonTrades.add(TradeUtils.createSell(TRContent.RUBBER_SAPLING, 5, 1, 8, 1));
		// registration of the trades, no changes necessary for new trades
		VillagerBridge.registerWanderingTraderOffers(1, allTradesList -> allTradesList.addAll(
			extraCommonTrades
		));
		VillagerBridge.registerWanderingTraderOffers(2, allTradesList -> allTradesList.addAll(
			extraRareTrades
		));
	}

	public static void registerVillagerHouses() {
		// StructureTemplatePool#templates is private in 1.21; village house injection was done via Fabric registry
		// callbacks. Re-enable via datapack template pool merges or a structure-pool helper when ported.
		TechReborn.LOGGER.debug("TRVillager.registerVillagerHouses: structure pool append skipped on NeoForge (private pool templates).");
	}
}
