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



package techreborn.init;



import reborncore.common.compat.VillagerApiBridge;

import reborncore.common.event.EventBridge;



import net.minecraft.resources.ResourceKey;

import net.minecraft.resources.Identifier;

import net.minecraft.sounds.SoundEvent;

import net.minecraft.world.entity.ai.village.poi.PoiType;

import net.minecraft.world.entity.npc.villager.VillagerProfession;

import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;



public final class VillagerBridge {

	private VillagerBridge() {

	}



	public static PoiType registerPoi(Identifier id, int ticketCount, int searchDistance, Block block) {

		return VillagerApiBridge.registerPoi(id, ticketCount, searchDistance, block);

	}



	public static PoiType createPoi(Identifier id, int ticketCount, int searchDistance, Block block) {

		return VillagerApiBridge.createPoi(id, ticketCount, searchDistance, block);

	}



	public static VillagerProfession buildProfession(Identifier id, ResourceKey<PoiType> workstation, SoundEvent workSound) {
		return VillagerApiBridge.buildProfession(id, workstation, workSound);
	}

	public static VillagerProfession buildProfession(
		Identifier id,
		ResourceKey<PoiType> workstation,
		SoundEvent workSound,
		it.unimi.dsi.fastutil.ints.Int2ObjectMap<ResourceKey<net.minecraft.world.item.trading.TradeSet>> tradeSetsByLevel
	) {
		return VillagerApiBridge.buildProfession(id, workstation, workSound, tradeSetsByLevel);
	}



	public static void onTemplatePoolAdded(TemplatePoolAddedHandler handler) {

		EventBridge.onTemplatePoolAdded(handler::onTemplatePoolAdded);

	}



	@FunctionalInterface

	public interface TemplatePoolAddedHandler {

		void onTemplatePoolAdded(Identifier id, StructureTemplatePool pool);

	}

}

