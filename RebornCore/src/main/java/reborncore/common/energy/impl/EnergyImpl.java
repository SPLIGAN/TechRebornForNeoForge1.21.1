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

package reborncore.common.energy.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EnergyImpl {
	public static final DataComponentType<Long> ENERGY_COMPONENT = DataComponentType.<Long>builder()
		.persistent(nonNegativeLongCodec())
		.networkSynchronized(ByteBufCodecs.VAR_LONG)
		.build();

	public static void register(RegisterEvent event) {
		event.register(
			Registries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath("team_reborn_energy", "energy"),
			() -> ENERGY_COMPONENT
		);
	}

	private static Codec<Long> nonNegativeLongCodec() {
		return Codec.LONG.validate(value -> {
			if (value >= 0) {
				return DataResult.success(value);
			}
			return DataResult.error(() -> "Energy value must be non-negative: " + value);
		});
	}
}
