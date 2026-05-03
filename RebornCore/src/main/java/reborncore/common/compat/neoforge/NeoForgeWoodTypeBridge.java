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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * Vanilla {@link BlockSetType#register(BlockSetType)} / {@link WoodType#register(WoodType)} (copied from a base type).
 */
public final class NeoForgeWoodTypeBridge {
	private NeoForgeWoodTypeBridge() {
	}

	public static BlockSetType createBlockSetType(ResourceLocation id, BlockSetType baseType) {
		BlockSetType created = new BlockSetType(
				id.getPath(),
				baseType.canOpenByHand(),
				baseType.canOpenByWindCharge(),
				baseType.canButtonBeActivatedByArrows(),
				baseType.pressurePlateSensitivity(),
				baseType.soundType(),
				baseType.doorClose(),
				baseType.doorOpen(),
				baseType.trapdoorClose(),
				baseType.trapdoorOpen(),
				baseType.pressurePlateClickOff(),
				baseType.pressurePlateClickOn(),
				baseType.buttonClickOff(),
				baseType.buttonClickOn());
		return BlockSetType.register(created);
	}

	public static WoodType registerWoodType(ResourceLocation id, WoodType baseType, BlockSetType blockSetType) {
		WoodType created = new WoodType(
				id.getPath(),
				blockSetType,
				baseType.soundType(),
				baseType.hangingSignSoundType(),
				baseType.fenceGateClose(),
				baseType.fenceGateOpen());
		return WoodType.register(created);
	}
}
