/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import reborncore.client.ClientJumpEvent;
import reborncore.client.gui.GuiBase;
import techreborn.client.ClientGuiType;
import techreborn.client.ClientboundPacketHandlers;
import techreborn.client.TechRebornNeoForgeClient;
import techreborn.client.events.ClientJumpHandler;
import techreborn.client.keybindings.KeyBindings;
import techreborn.client.events.StackToolTipHandler;
import techreborn.network.TechRebornClientActions;
import techreborn.init.TRContent;
import techreborn.items.DynamicCellItem;

public class TechRebornClient {

	public void onInitializeClient() {
		TechRebornNeoForgeClient.registerRenderLayers();

		StackToolTipHandler.setup();
		TechRebornClientActions.openManual = (payload, context) -> Minecraft.getInstance().setScreen(new techreborn.client.gui.GuiManual());
		ClientboundPacketHandlers.init();

		GuiBase.wrenchStack = ItemStackTemplate.fromNonEmptyStack(new ItemStack(TRContent.WRENCH));
		GuiBase.fluidCellProvider = DynamicCellItem::getCellWithFluid;

		// TODO CLI-03: 26.1 item model definitions replace ItemProperties/ClampedItemPropertyFunction.
		// Migrate batpack/battery/cell/chainsaw/nanosaber predicates to assets/techreborn/items/*.json.

		ClientGuiType.AESU.toString();

		ClientJumpEvent.EVENT.register(new ClientJumpHandler());
	}

	public static void onClientTickPost(ClientTickEvent.Post event) {
		while (KeyBindings.suitNightVision.consumeClick()) {
		 KeyBindings.handleSuitNVToggle();
		}
		while (KeyBindings.quantumSuitSprint.consumeClick()) {
			KeyBindings.handleQuantumSuitSprintToggle();
		}
	}
}
