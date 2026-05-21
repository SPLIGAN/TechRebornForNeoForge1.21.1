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

package reborncore.common.compat.neoforge;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class NeoForgeVillagerBridge {
	private static final Map<Integer, List<Consumer<List<VillagerTrades.ItemListing>>>> WANDERING_TRADE_CALLBACKS =
			new ConcurrentHashMap<>();

	private NeoForgeVillagerBridge() {
	}

	public static PoiType createPoiType(ResourceLocation id, int ticketCount, int searchDistance, Block block) {
		ImmutableSet<BlockState> states = ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
		return new PoiType(states, ticketCount, searchDistance);
	}

	public static PoiType registerPoi(ResourceLocation id, int ticketCount, int searchDistance, Block block) {
		return Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, id, createPoiType(id, ticketCount, searchDistance, block));
	}

	public static VillagerProfession buildProfession(ResourceLocation id, ResourceKey<PoiType> workstation, SoundEvent workSound) {
		Predicate<Holder<PoiType>> forPoi = holder -> holder.is(workstation);
		// Matches lang keys: entity.minecraft.villager.<namespace>.<path>
		String professionName = id.getNamespace() + "." + id.getPath();
		return new VillagerProfession(
				professionName,
				forPoi,
				forPoi,
				ImmutableSet.<Item>of(),
				ImmutableSet.<Block>of(),
				workSound);
	}

	public static void registerWanderingTraderOffers(int level, Consumer<List<VillagerTrades.ItemListing>> consumer) {
		WANDERING_TRADE_CALLBACKS.computeIfAbsent(level, l -> new CopyOnWriteArrayList<>()).add(consumer);
	}

	public static void onWandererTrades(WandererTradesEvent event) {
		applyWanderingLevel(1, event.getGenericTrades());
		applyWanderingLevel(2, event.getRareTrades());
	}

	private static void applyWanderingLevel(int level, List<VillagerTrades.ItemListing> trades) {
		List<Consumer<List<VillagerTrades.ItemListing>>> callbacks = WANDERING_TRADE_CALLBACKS.get(level);
		if (callbacks == null) {
			return;
		}
		for (Consumer<List<VillagerTrades.ItemListing>> callback : callbacks) {
			callback.accept(trades);
		}
	}
}
