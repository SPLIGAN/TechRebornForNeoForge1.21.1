/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 TeamReborn
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

package reborncore;

import reborncore.api.blockentity.UnloadHandler;
import reborncore.client.*;
import reborncore.client.event.ClientLifecycleBridge;
import reborncore.common.util.LoaderBridge;

import java.util.Locale;

public class RebornCoreClient {

	public void onInitializeClient() {
		ClientBoundPacketHandlers.init();
		ClientLifecycleBridge.onHudRender(new ItemStackRenderer());
		ClientLifecycleBridge.registerTooltipAppender(new StackToolTipHandler());
		ClientLifecycleBridge.onBlockOutline(BlockOutlineRenderer::onBlockOutline);

		/* register UnloadHandler */
		ClientLifecycleBridge.onBlockEntityUnload((blockEntity, world) -> {
			if (blockEntity instanceof UnloadHandler) ((UnloadHandler) blockEntity).onUnload();
		});

		ClientLifecycleBridge.onClientStarted(client -> {
			String strangeMcLang = client.options.languageCode;
			RebornCore.locale = Locale.forLanguageTag(strangeMcLang.substring(0, 2));
		});

		ClientLifecycleBridge.onEndClientTick(client -> {
			if (client.options.keyJump.isDown()) {
				ClientJumpEvent.EVENT.invoker().jump();
			}
		});

		ClientLifecycleBridge.registerBuiltinResourcePack(
			net.minecraft.resources.Identifier.fromNamespaceAndPath("reborncore", "reborncore_darkmode"),
			LoaderBridge.getModContainer("reborncore").orElseThrow()
		);
	}
}
