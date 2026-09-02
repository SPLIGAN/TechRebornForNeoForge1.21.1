/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2026 TeamReborn
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

package reborncore.common.fluid.container;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import reborncore.common.fluid.FluidValue;
import reborncore.common.transfer.RcFluidVariant;

public record FluidInstance(Fluid fluid, FluidValue amount) {
	public static final FluidInstance EMPTY = new FluidInstance(Fluids.EMPTY, FluidValue.EMPTY);

	/**
	 * Fabric FluidVariant JSON is either a bare id or {@code {"fluid":"mod:id"}}.
	 * Accept both so upstream machine recipes parse on NeoForge.
	 */
	private static final Codec<Fluid> FLUID_CODEC = Codec.withAlternative(
		BuiltInRegistries.FLUID.byNameCodec(),
		RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(f -> f)
		).apply(instance, f -> f))
	);

	private static final Codec<FluidInstance> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		FLUID_CODEC.fieldOf("fluid").forGetter(FluidInstance::fluid),
		FluidValue.RECIPE_AMOUNT_CODEC.optionalFieldOf("amount", FluidValue.fromMillibuckets(1000)).forGetter(FluidInstance::getAmount)
	).apply(instance, FluidInstance::new));

	/** JSON may be {@code {"fluid":"...","amount":...}}, Fabric nested fluid, or a bare fluid id (defaults to 1000 mB). */
	public static final Codec<FluidInstance> CODEC = Codec.withAlternative(
		OBJECT_CODEC,
		BuiltInRegistries.FLUID.byNameCodec().xmap(
			f -> new FluidInstance(f, FluidValue.fromMillibuckets(1000)),
			FluidInstance::fluid
		)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidInstance> PACKET_CODEC = StreamCodec.composite(
		ByteBufCodecs.registry(Registries.FLUID), FluidInstance::fluid,
		FluidValue.PACKET_CODEC, FluidInstance::getAmount,
		FluidInstance::new
	);

	public FluidInstance(Fluid fluid) {
		this(fluid, FluidValue.EMPTY);
	}

	public FluidInstance() {
		this(Fluids.EMPTY);
	}

	public RcFluidVariant fluidVariant() {
		if (isEmpty()) {
			return RcFluidVariant.blank();
		}
		return RcFluidVariant.of(fluid);
	}

	public Fluid fluid() {
		return fluid;
	}

	public FluidValue getAmount() {
		return amount;
	}

	public FluidInstance withFluid(Fluid fluid) {
		return new FluidInstance(fluid, this.amount);
	}

	public FluidInstance withAmount(FluidValue amount) {
		return new FluidInstance(this.fluid, amount);
	}

	public FluidInstance subtractAmount(FluidValue amount) {
		return new FluidInstance(this.fluid, this.amount.subtract(amount));
	}

	public FluidInstance addAmount(FluidValue amount) {
		return new FluidInstance(this.fluid, this.amount.add(amount));
	}

	public boolean isEmpty() {
		return isEmptyFluid() || this.getAmount().isEmpty();
	}

	public boolean isEmptyFluid() {
		return this.fluid() == Fluids.EMPTY;
	}
}
