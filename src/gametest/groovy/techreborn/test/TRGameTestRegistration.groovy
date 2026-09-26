/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2026 TechReborn
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

import java.util.function.Consumer
import net.minecraft.core.Holder
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.gametest.framework.TestData
import net.minecraft.gametest.framework.TestEnvironmentDefinition
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.event.RegisterGameTestsEvent
import techreborn.test.machine.GrinderTest
import techreborn.test.machine.IronAlloyFurnaceTest
import techreborn.test.machine.IronFurnaceTest

/**
 * Registers TechReborn GameTests when {@code neoforge.enableGameTest} / gameTestServer is active.
 */
final class TRGameTestRegistration {
	private static final Identifier EMPTY_STRUCTURE = Identifier.fromNamespaceAndPath("techreborn", "empty")

	private TRGameTestRegistration() {
	}

	static void register(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
			Identifier.fromNamespaceAndPath("techreborn", "default"),
			new TestEnvironmentDefinition.AllOf()
		)

		registerOne(event, environment, "grind_2_ocs", 150) { helper ->
			TRGameTest.run(helper) { ctx -> new GrinderTest().testGrind2OCs(ctx) }
		}
		registerOne(event, environment, "iron_furnace_smelt_raw_iron", 2000) { helper ->
			TRGameTest.run(helper) { ctx -> new IronFurnaceTest().testIronFurnaceSmeltRawIron(ctx) }
		}
		registerOne(event, environment, "iron_alloy_furnace_electrum", 2000) { helper ->
			TRGameTest.run(helper) { ctx -> new IronAlloyFurnaceTest().testIronAlloyFurnaceElectrumAlloyIngot(ctx) }
		}

		registerOne(event, environment, "fe_block_insert_extract", 100) { helper -> InteropGameTests.feBlockInsertExtract(helper) }
		registerOne(event, environment, "fe_fallback_transfer", 100) { helper -> InteropGameTests.feFallbackTransfer(helper) }
		registerOne(event, environment, "fe_machine_pushes_to_neighbour", 100) { helper -> InteropGameTests.feMachinePushesToNeighbour(helper) }
		registerOne(event, environment, "fe_item_capability", 100) { helper -> InteropGameTests.feItemCapability(helper) }
		registerOne(event, environment, "hopper_inserts_into_machine", 200) { helper -> InteropGameTests.hopperInsertsIntoMachine(helper) }
		registerOne(event, environment, "machine_item_extract", 100) { helper -> InteropGameTests.machineItemExtract(helper) }
		registerOne(event, environment, "storage_unit_transactions", 100) { helper -> InteropGameTests.storageUnitTransactions(helper) }
		registerOne(event, environment, "storage_unit_hopper_conservation", 300) { helper -> InteropGameTests.storageUnitHopperConservation(helper) }
		registerOne(event, environment, "tank_unit_fluid_transactions", 100) { helper -> InteropGameTests.tankUnitFluidTransactions(helper) }
		registerOne(event, environment, "creative_units_op_only", 100) { helper -> InteropGameTests.creativeUnitsOpOnly(helper) }
		registerOne(event, environment, "auto_slot_input_conserves_items", 200) { helper -> InteropGameTests.autoSlotInputConservesItems(helper) }
		registerOne(event, environment, "auto_slot_output_conserves_items", 200) { helper -> InteropGameTests.autoSlotOutputConservesItems(helper) }
		registerOne(event, environment, "fluid_cell_capability", 100) { helper -> InteropGameTests.fluidCellCapability(helper) }
		registerOne(event, environment, "tank_unit_fills_cells", 200) { helper -> InteropGameTests.tankUnitFillsCells(helper) }
		registerOne(event, environment, "tank_unit_drains_cells", 200) { helper -> InteropGameTests.tankUnitDrainsCells(helper) }
		registerOne(event, environment, "tank_unit_hand_fill_adds", 100) { helper -> InteropGameTests.tankUnitHandFillAdds(helper) }
		registerOne(event, environment, "battery_charge_conserves_energy", 200) { helper -> InteropGameTests.batteryChargeConservesEnergy(helper) }
		registerOne(event, environment, "creative_units_block_automation", 100) { helper -> InteropGameTests.creativeUnitsBlockAutomation(helper) }
		registerOne(event, environment, "machine_config_permissions", 100) { helper -> InteropGameTests.machineConfigPermissions(helper) }
		registerOne(event, environment, "tank_unit_hand_bucket", 100) { helper -> InteropGameTests.tankUnitHandBucket(helper) }
	}

	private static void registerOne(
		RegisterGameTestsEvent event,
		Holder<TestEnvironmentDefinition<?>> environment,
		String path,
		int maxTicks,
		Consumer<GameTestHelper> function
	) {
		TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
			environment,
			EMPTY_STRUCTURE,
			maxTicks,
			0,
			true
		)
		event.registerTest(
			Identifier.fromNamespaceAndPath("techreborn", path),
			new ConsumerGameTestInstance(function, data)
		)
	}
}
