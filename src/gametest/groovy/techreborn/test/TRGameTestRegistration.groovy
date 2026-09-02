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
