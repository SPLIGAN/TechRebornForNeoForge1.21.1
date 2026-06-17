/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
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

package techreborn.client.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import reborncore.client.input.ClientInputBridge;
import reborncore.client.network.ClientNetworkingBridge;
import techreborn.TechReborn;
import techreborn.packets.serverbound.QuantumSuitSprintPayload;
import techreborn.packets.serverbound.SuitNightVisionPayload;

public class KeyBindings {
	public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
		Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, "main"));

	public static KeyMapping suitNightVision;
	public static KeyMapping quantumSuitSprint;

	public static void registerKeys(RegisterKeyMappingsEvent event) {
		event.registerCategory(CATEGORY);

		suitNightVision = ClientInputBridge.register(event,
			new KeyMapping("key.techreborn.suitNightVision",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				CATEGORY));

		quantumSuitSprint = ClientInputBridge.register(event,
			new KeyMapping("key.techreborn.quantumSuitSprint",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				CATEGORY));
	}

	public static void handleSuitNVToggle() {
		ClientNetworkingBridge.sendToServer(new SuitNightVisionPayload());
	}

	public static void handleQuantumSuitSprintToggle() {
		ClientNetworkingBridge.sendToServer(new QuantumSuitSprintPayload());
	}
}
