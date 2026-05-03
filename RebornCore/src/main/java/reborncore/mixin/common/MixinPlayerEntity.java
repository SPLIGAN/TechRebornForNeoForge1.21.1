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

package reborncore.mixin.common;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import reborncore.api.items.ArmorBlockEntityTicker;
import reborncore.common.powerSystem.RcEnergyItem;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends LivingEntity {

	@Shadow
	public abstract Iterable<ItemStack> getArmorSlots();

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> type, Level world) {
		super(type, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo info) {
		Player player = (Player) (Object) this;
		if (player.isSpectator()) return;
		if (!player.inventoryMenu.active) {
			ItemStack stack = player.inventoryMenu.getCarried();
			if (stack.getItem() instanceof ArmorBlockEntityTicker ticker) {
				stack.remove(DataComponents.CUSTOM_DATA);
				ticker.tickArmor(stack, false, player);
			}
			return;
		}

		Class<?> suit = null;
		int count = 0;
		for (ItemStack stack : getArmorSlots()) {
			if (!(stack.getItem() instanceof RcEnergyItem item)) {
				break;
			}
			if (item.getStoredEnergy(stack) == 0) {
				break;
			}
			if (suit == null) {
				suit = item.getClass();
			} else if (suit != item.getClass()) {
				break;
			}
			count++;
		}

		for (ItemStack stack : getArmorSlots()) {
			if (!stack.isEmpty() && stack.getItem() instanceof ArmorBlockEntityTicker) {
				// mark tick
				if (!stack.has(DataComponents.CUSTOM_DATA)) {
					stack.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
				}
				((ArmorBlockEntityTicker) stack.getItem()).tickArmor(stack, count == 4, player);
			}
		}
	}
}
