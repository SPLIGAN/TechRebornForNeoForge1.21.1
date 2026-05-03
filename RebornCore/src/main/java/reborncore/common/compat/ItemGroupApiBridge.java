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

package reborncore.common.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Creative-tab helpers: vanilla builders plus NeoForge {@link net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent}.
 */
public final class ItemGroupApiBridge {
	private static final Map<ResourceKey<CreativeModeTab>, CopyOnWriteArrayList<Consumer<Entries>>> MODIFY_CALLBACKS = new ConcurrentHashMap<>();

	private ItemGroupApiBridge() {
	}

	public static CreativeModeTab.Builder createItemGroupBuilder() {
		return CreativeModeTab.builder();
	}

	public static void registerModifyEntriesEvent(ResourceKey<CreativeModeTab> group, Consumer<Entries> callback) {
		MODIFY_CALLBACKS.computeIfAbsent(group, k -> new CopyOnWriteArrayList<>()).add(callback);
	}

	public static Iterable<Consumer<Entries>> consumersFor(ResourceKey<CreativeModeTab> tabKey) {
		CopyOnWriteArrayList<Consumer<Entries>> list = MODIFY_CALLBACKS.get(tabKey);
		return list != null ? list : Collections.emptyList();
	}

	public interface Entries {
		void add(ItemLike item);

		void add(ItemStack stack);

		void addAfter(ItemLike after, ItemLike... items);

		void addBefore(ItemLike before, ItemLike... items);

		void addBefore(ItemLike before, ItemStack... stacks);

		boolean shouldShowOpRestrictedItems();

		HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry();
	}
}
