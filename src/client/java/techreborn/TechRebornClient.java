/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;
import reborncore.client.ClientJumpEvent;
import reborncore.client.gui.GuiBase;
import reborncore.common.energy.api.base.SimpleEnergyItem;
import techreborn.client.ClientGuiType;
import techreborn.client.ClientboundPacketHandlers;
import techreborn.client.TechRebornNeoForgeClient;
import techreborn.client.events.ClientJumpHandler;
import techreborn.client.keybindings.KeyBindings;
import techreborn.client.events.StackToolTipHandler;
import techreborn.network.TechRebornClientActions;
import techreborn.component.TRDataComponentTypes;
import techreborn.init.TRContent;
import techreborn.items.BatteryItem;
import techreborn.items.DynamicCellItem;
import techreborn.items.FrequencyTransmitterItem;
import techreborn.items.armor.BatpackItem;
import techreborn.items.tool.ChainsawItem;
import techreborn.items.tool.industrial.NanosaberItem;
import reborncore.common.powerSystem.RcEnergyItem;

public class TechRebornClient {

	public void onInitializeClient() {
		TechRebornNeoForgeClient.registerRenderLayers();

		StackToolTipHandler.setup();
		TechRebornClientActions.openManual = (payload, context) -> Minecraft.getInstance().setScreen(new techreborn.client.gui.GuiManual());
		ClientboundPacketHandlers.init();

		GuiBase.wrenchStack = new ItemStack(TRContent.WRENCH);
		GuiBase.fluidCellProvider = DynamicCellItem::getCellWithFluid;

		registerPredicateProvider(
				BatpackItem.class,
				ResourceLocation.parse("techreborn:empty"),
				(item, stack, world, entity, seed) -> {
					if (!stack.isEmpty() && SimpleEnergyItem.getStoredEnergyUnchecked(stack) == 0) {
						return 1.0F;
					}
					return 0.0F;
				}
		);

		registerPredicateProvider(
				BatteryItem.class,
				ResourceLocation.parse("techreborn:empty"),
				(item, stack, world, entity, seed) -> {
					if (!stack.isEmpty() && SimpleEnergyItem.getStoredEnergyUnchecked(stack) == 0) {
						return 1.0F;
					}
					return 0.0F;
				}
		);

		registerPredicateProvider(
				FrequencyTransmitterItem.class,
				ResourceLocation.parse("techreborn:coords"),
				(item, stack, world, entity, seed) -> {
					GlobalPos globalPos = stack.getOrDefault(TRDataComponentTypes.FREQUENCY_TRANSMITTER, null);
					if (globalPos != null) {
						return 1.0F;
					}
					return 0.0F;
				}
		);

		registerPredicateProvider(
				ChainsawItem.class,
				ResourceLocation.parse("techreborn:animated"),
				(item, stack, world, entity, seed) -> {
					if (!stack.isEmpty() && SimpleEnergyItem.getStoredEnergyUnchecked(stack) >= item.getCost() && entity != null && entity.getMainHandItem().equals(stack)) {
						return 1.0F;
					}
					return 0.0F;
				}
		);

		registerPredicateProvider(
				NanosaberItem.class,
				ResourceLocation.parse("techreborn:active"),
				(item, stack, world, entity, seed) -> {
					if (stack.get(TRDataComponentTypes.IS_ACTIVE) == Boolean.TRUE) {
						RcEnergyItem energyItem = (RcEnergyItem) stack.getItem();
						if (energyItem.getEnergyCapacity(stack) - energyItem.getStoredEnergy(stack) >= 0.9 * item.getEnergyCapacity(stack)) {
							return 0.5F;
						}
						return 1.0F;
					}
					return 0.0F;
				}
		);

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

	private static <T extends Item> void registerPredicateProvider(Class<T> itemClass, ResourceLocation identifier, ItemModelPredicateProvider<T> modelPredicateProvider) {
		BuiltInRegistries.ITEM.stream()
				.filter(itemClass::isInstance)
				.forEach(item -> ItemProperties.register(item, identifier, modelPredicateProvider));
	}

	private interface ItemModelPredicateProvider<T extends Item> extends ClampedItemPropertyFunction {

		float call(T item, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed);

		@Override
		default float unclampedCall(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
			//noinspection unchecked
			return call((T) stack.getItem(), stack, world, entity, seed);
		}
	}
}
