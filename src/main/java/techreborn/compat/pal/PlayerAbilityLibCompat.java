/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2025 TechReborn
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

package techreborn.compat.pal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reborncore.common.util.LoaderBridge;

public class PlayerAbilityLibCompat {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerAbilityLibCompat.class);

	public void onInitialize() {
		if (!LoaderBridge.isModLoaded("playerabilitylib")) {
			return;
		}

		// PAL flight bridge requires Ladysnake PAL on the compile classpath; NeoForge coords pending — vanilla handler stays active.
		LOGGER.debug("PlayerAbilityLib loaded but PAL flight compat is not compiled into this NeoForge build yet");
	}
}
