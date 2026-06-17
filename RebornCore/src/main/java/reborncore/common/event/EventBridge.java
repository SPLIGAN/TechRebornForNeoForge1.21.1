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

package reborncore.common.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class EventBridge {
	private EventBridge() {
	}

	@FunctionalInterface
	public interface CommandRegistration {
		void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment);
	}

	public static void registerCommands(CommandRegistration callback) {
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (RegisterCommandsEvent event) ->
			callback.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));
	}

	public static void registerUseBlock(UseBlockHandler handler) {
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, (PlayerInteractEvent.RightClickBlock event) -> {
			if (event.getLevel().isClientSide()) {
				return;
			}
			BlockHitResult hit = event.getHitVec();
			InteractionResult result = handler.onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), hit);
			if (result != InteractionResult.PASS) {
				event.setCanceled(true);
				event.setCancellationResult(result);
			}
		});
	}

	public static void registerLootModify(LootModify callback) {
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (LootTableLoadEvent event) -> {
			callback.modify(event.getKey(), event.getTable(), null);
		});
	}

	public static void onTemplatePoolAdded(TemplatePoolAddedHandler handler) {
		// Fabric used DynamicRegistrySetupCallback + registerEntryAdded. NeoForge has no identical hook here;
		// TechReborn village pool injection is currently disabled (see techreborn.init.TRVillager).
	}

	@FunctionalInterface
	public interface LootModify {
		void modify(ResourceKey<LootTable> key, LootTable table, @org.jetbrains.annotations.Nullable LootDataType source);
	}

	@FunctionalInterface
	public interface UseBlockHandler {
		InteractionResult onUseBlock(Player playerEntity, Level world, InteractionHand hand, BlockHitResult blockHitResult);
	}

	@FunctionalInterface
	public interface TemplatePoolAddedHandler {
		void onTemplatePoolAdded(Identifier id, StructureTemplatePool pool);
	}
}
