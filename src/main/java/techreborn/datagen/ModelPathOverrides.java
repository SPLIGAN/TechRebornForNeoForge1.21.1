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

package techreborn.datagen;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * Runtime overrides for model/texture path lookup during client datagen.
 * Populated by Groovy {@code TexturePaths} when the datagen source set is loaded;
 * empty in production so client mixins are no-ops.
 */
public final class ModelPathOverrides {
	private static final Map<Item, Identifier> ITEM_PATHS = new ConcurrentHashMap<>();
	private static final Map<Block, Identifier> BLOCK_PATHS = new ConcurrentHashMap<>();
	private static final Map<Block, Identifier> ALIAS_PATHS = new ConcurrentHashMap<>();

	private ModelPathOverrides() {
	}

	public static void putItem(Item item, Identifier id) {
		ITEM_PATHS.put(item, id);
	}

	public static void putBlock(Block block, Identifier id) {
		BLOCK_PATHS.put(block, id);
	}

	public static void putAlias(Block block, Identifier id) {
		ALIAS_PATHS.put(block, id);
	}

	public static void ifPresent(Item item, Consumer<Identifier> callback) {
		Identifier id = ITEM_PATHS.get(item);
		if (id != null) {
			callback.accept(id);
		}
	}

	public static void ifPresent(Item item, String suffix, Consumer<Identifier> callback) {
		Identifier id = ITEM_PATHS.get(item);
		if (id != null) {
			callback.accept(id.withSuffix(suffix));
		}
	}

	public static void ifPresent(Block block, Consumer<Identifier> callback) {
		Identifier id = BLOCK_PATHS.get(block);
		if (id != null) {
			callback.accept(id);
		}
	}

	public static void ifPresentOrAlias(Block block, Consumer<Identifier> callback) {
		Identifier id = Optional.ofNullable(ALIAS_PATHS.get(block)).orElseGet(() -> BLOCK_PATHS.get(block));
		if (id != null) {
			callback.accept(id);
		}
	}

	public static void ifPresent(Block block, String suffix, Consumer<Identifier> callback) {
		Identifier id = BLOCK_PATHS.get(block);
		if (id != null) {
			callback.accept(id.withSuffix(suffix));
		}
	}

	public static @Nullable Identifier getItem(Item item) {
		return ITEM_PATHS.get(item);
	}

	public static @Nullable Identifier getBlock(Block block) {
		return BLOCK_PATHS.get(block);
	}
}
