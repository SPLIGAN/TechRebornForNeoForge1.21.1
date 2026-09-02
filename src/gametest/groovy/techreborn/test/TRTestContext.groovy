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

package techreborn.test

import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import reborncore.common.blockentity.MachineBaseBlockEntity
import reborncore.common.recipes.RecipeCrafter
import techreborn.blockentity.machine.GenericMachineBlockEntity
import techreborn.init.TRContent

class TRTestContext {
	final GameTestHelper helper

	TRTestContext(GameTestHelper helper) {
		this.helper = helper
	}

	/**
	 * Place a machine with a creative solar panel
	 */
	def poweredMachine(TRContent.Machine machine, @DelegatesTo(MachineContext) Closure machineContextClosure) {
		def machinePos = new BlockPos(2, 2, 2)
		helper.setBlock(machinePos, machine.block)
		helper.setBlock(machinePos.below(), TRContent.SolarPanels.CREATIVE.block)

		helper.runAfterDelay(5) {
			try {
				new MachineContext(machinePos).with(machineContextClosure)
			} catch (e) {
				e.printStackTrace()
				throw e
			}
		}
	}

	def machine(TRContent.Machine machine, @DelegatesTo(MachineContext) Closure machineContextClosure) {
		def machinePos = new BlockPos(2, 1, 2)
		helper.setBlock(machinePos, machine.block)

		helper.runAfterDelay(5) {
			try {
				new MachineContext(machinePos).with(machineContextClosure)
			} catch (e) {
				e.printStackTrace()
				throw e
			}
		}
	}

	class MachineContext {
		final BlockPos machinePos

		MachineContext(BlockPos machinePos) {
			this.machinePos = machinePos
		}

		def input(ItemLike item, int slot = -1) {
			this.input(new ItemStack(item), slot)
		}

		def input(ItemStack stack, int slot = -1) {
			if (slot == -1) {
				slot = recipeCrafter.inputSlots[0]
			}

			inventory.setItem(slot, stack)
		}

		def expectOutput(ItemLike item, int ticks, int slot = -1) {
			expectOutput(new ItemStack(item), ticks, slot)
		}

		def expectOutput(ItemStack stack, int ticks, int slot = -1) {
			if (slot == -1) {
				slot = recipeCrafter.outputSlots[0]
			}

			// Account for the 5-tick setup delay used by machine()/poweredMachine().
			helper.runAtTickTime(ticks + 5) {
				def actual = inventory.getItem(slot)
				if (!ItemStack.isSameItem(actual, stack)) {
					helper.fail("Failed to find $stack in slot $slot (found $actual)")
				}
				helper.succeed()
			}
		}

		def withUpgrades(TRContent.Upgrades upgrade, int count = -1) {
			count = (count != -1 ? count : blockEntity.getUpgradeSlotCount()) - 1

			(0..count).each {
				blockEntity.upgradeInventory.setItem(it, new ItemStack(upgrade))
			}
		}

		MachineBaseBlockEntity getBlockEntity() {
			MachineBaseBlockEntity be = helper.getBlockEntity(machinePos, MachineBaseBlockEntity.class)

			if (be == null) {
				helper.fail("Failed to get machine block entity", machinePos)
			}

			return be
		}

		private def getInventory() {
			def be = blockEntity
			if (be.metaClass.hasProperty(be, "inventory") && be.inventory != null) {
				return be.inventory
			}
			throw new TRGameTest.TRGameTestException("Machine has no inventory field", null)
		}

		private RecipeCrafter getRecipeCrafter() {
			def be = blockEntity
			if (be instanceof GenericMachineBlockEntity && be.crafter != null) {
				return be.crafter
			}
			if (be.metaClass.hasProperty(be, "crafter") && be.crafter != null) {
				return be.crafter as RecipeCrafter
			}
			throw new TRGameTest.TRGameTestException("Machine has no recipe crafter", null)
		}
	}
}
