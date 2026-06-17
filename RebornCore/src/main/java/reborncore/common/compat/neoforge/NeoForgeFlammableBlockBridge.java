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

import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 26.1: {@code FireBlock#setFlammable} is private. Stores flammability for blocks that override
 * {@link net.neoforged.neoforge.common.extensions.IBlockExtension} fire hooks (VIL/WLD follow-up).
 */
public final class NeoForgeFlammableBlockBridge {
	private static final Map<Block, int[]> FLAMMABILITY = new ConcurrentHashMap<>();

	private NeoForgeFlammableBlockBridge() {
	}

	public static void register(Block block, int burnChance, int spreadChance) {
		FLAMMABILITY.put(block, new int[]{burnChance, spreadChance});
	}

	public static int getBurnChance(Block block) {
		int[] values = FLAMMABILITY.get(block);
		return values == null ? 0 : values[0];
	}

	public static int getSpreadChance(Block block) {
		int[] values = FLAMMABILITY.get(block);
		return values == null ? 0 : values[1];
	}

	public static boolean isRegistered(Block block) {
		return FLAMMABILITY.containsKey(block);
	}
}
