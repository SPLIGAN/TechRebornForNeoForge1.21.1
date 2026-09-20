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

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import reborncore.common.compat.ItemGroupApiBridge;

import java.util.function.Consumer;

public final class NeoForgeItemGroupBridge {
	private static final CreativeModeTab.TabVisibility DEFAULT_VISIBILITY = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

	private NeoForgeItemGroupBridge() {
	}

	public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
		for (Consumer<ItemGroupApiBridge.Entries> consumer : ItemGroupApiBridge.consumersFor(event.getTabKey())) {
			consumer.accept(new NeoForgeEntriesAdapter(event));
		}
	}

	private record NeoForgeEntriesAdapter(BuildCreativeModeTabContentsEvent event) implements ItemGroupApiBridge.Entries {

		@Override
		public void add(ItemLike item) {
			event.accept(new ItemStack(item.asItem()), DEFAULT_VISIBILITY);
		}

		@Override
		public void add(ItemStack stack) {
			event.accept(stack, DEFAULT_VISIBILITY);
		}

		@Override
		public void addAfter(ItemLike after, ItemLike... items) {
			ItemStack anchor = new ItemStack(after.asItem());
			for (ItemLike item : items) {
				ItemStack stack = new ItemStack(item.asItem());
				event.insertAfter(anchor, stack, DEFAULT_VISIBILITY);
				anchor = stack;
			}
		}

		@Override
		public void addBefore(ItemLike before, ItemLike... items) {
			ItemStack anchor = new ItemStack(before.asItem());
			for (ItemLike item : items) {
				ItemStack stack = new ItemStack(item.asItem());
				event.insertBefore(anchor, stack, DEFAULT_VISIBILITY);
				anchor = stack;
			}
		}

		@Override
		public void addBefore(ItemLike before, ItemStack... stacks) {
			ItemStack anchor = new ItemStack(before.asItem());
			for (ItemStack stack : stacks) {
				event.insertBefore(anchor, stack, DEFAULT_VISIBILITY);
				anchor = stack;
			}
		}

		@Override
		public boolean shouldShowOpRestrictedItems() {
			return event.hasPermissions();
		}

		@Override
		public HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry() {
			return event.getParameters().holders().lookupOrThrow(Registries.ENCHANTMENT);
		}
	}
}
