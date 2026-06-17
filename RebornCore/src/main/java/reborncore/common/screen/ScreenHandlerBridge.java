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

package reborncore.common.screen;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import reborncore.common.compat.neoforge.NeoForgeExtendedScreenHandlerBridge;

public final class ScreenHandlerBridge {
	private ScreenHandlerBridge() {
	}

	public static <D> MenuType<BuiltScreenHandler> registerExtended(
			Identifier id,
			StreamCodec<? super RegistryFriendlyByteBuf, D> packetCodec,
			ScreenHandlerDataFactory<D> factory) {
		return NeoForgeExtendedScreenHandlerBridge.registerExtended(id, packetCodec, factory);
	}

	public static <D> void openExtended(
			Player player,
			D data,
			Component displayName,
			MenuFactory menuFactory,
			StreamCodec<? super RegistryFriendlyByteBuf, D> packetCodec) {
		NeoForgeExtendedScreenHandlerBridge.openExtended(player, data, displayName, menuFactory, packetCodec);
	}

	@FunctionalInterface
	public interface ScreenHandlerDataFactory<D> {
		BuiltScreenHandler create(int syncId, Inventory playerInventory, D data);
	}

	@FunctionalInterface
	public interface MenuFactory {
		AbstractContainerMenu create(int syncId, Inventory inventory, Player player);
	}
}
