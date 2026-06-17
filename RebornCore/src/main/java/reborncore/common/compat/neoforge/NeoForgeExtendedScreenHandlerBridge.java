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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.ScreenHandlerBridge;

/**
 * Extended menus using NeoForge {@link IMenuTypeExtension} (extra client data via {@link StreamCodec}).
 */
public final class NeoForgeExtendedScreenHandlerBridge {
	private NeoForgeExtendedScreenHandlerBridge() {
	}

	public static <D> MenuType<BuiltScreenHandler> registerExtended(
			Identifier id,
			StreamCodec<? super RegistryFriendlyByteBuf, D> packetCodec,
			ScreenHandlerBridge.ScreenHandlerDataFactory<D> factory) {
		return Registry.register(
				BuiltInRegistries.MENU,
				id,
				IMenuTypeExtension.create((syncId, inv, buf) -> factory.create(syncId, inv, packetCodec.decode(buf))));
	}

	public static <D> void openExtended(
			Player player,
			D data,
			Component displayName,
			ScreenHandlerBridge.MenuFactory menuFactory,
			StreamCodec<? super RegistryFriendlyByteBuf, D> packetCodec) {
		if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		serverPlayer.openMenu(
				new SimpleMenuProvider((syncId, inv, menuPlayer) -> menuFactory.create(syncId, inv, menuPlayer), displayName),
				buf -> packetCodec.encode(buf, data));
	}
}
