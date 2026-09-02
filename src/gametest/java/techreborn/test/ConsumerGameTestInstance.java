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

package techreborn.test;

import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Code-only {@link GameTestInstance} that runs a {@link Consumer} directly,
 * avoiding the datapack {@code TEST_FUNCTION} registry indirection.
 */
public final class ConsumerGameTestInstance extends GameTestInstance {
	private final Consumer<GameTestHelper> function;

	public ConsumerGameTestInstance(Consumer<GameTestHelper> function, TestData<Holder<TestEnvironmentDefinition<?>>> info) {
		super(info);
		this.function = function;
	}

	@Override
	public void run(GameTestHelper helper) {
		function.accept(helper);
	}

	@Override
	public MapCodec<? extends GameTestInstance> codec() {
		throw new UnsupportedOperationException("TechReborn ConsumerGameTestInstance is code-registered only");
	}

	@Override
	protected MutableComponent typeDescription() {
		return Component.literal("TechReborn function");
	}
}
