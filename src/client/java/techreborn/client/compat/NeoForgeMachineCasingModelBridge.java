/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.client.compat;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import techreborn.blocks.misc.BlockMachineCasing;
import techreborn.client.render.MachineCasingModel;
import techreborn.init.TRContent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * NeoForge replacement for Fabric {@code ModelLoadingPlugin} wiring for {@link MachineCasingModel}.
 * Uses {@link ModelEvent.ModifyBakingResult}; obtains {@link ModelBaker} from {@link ModelBakery} reflectively (MC accessor naming varies).
 */
public final class NeoForgeMachineCasingModelBridge {
	private NeoForgeMachineCasingModelBridge() {
	}

	public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
		ModelBaker baker = modelBakerFrom(event.getModelBakery());
		var spriteGetter = event.getTextureGetter();
		Map<ModelResourceLocation, BakedModel> models = event.getModels();

		for (TRContent.MachineBlocks mb : TRContent.MachineBlocks.values()) {
			BlockMachineCasing block = (BlockMachineCasing) mb.casing;
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				ModelResourceLocation stateLoc = BlockModelShaper.stateToModelLocation(state);
				MachineCasingModel.Unbaked unbaked = MachineCasingModel.unbakedFor(block, state);
				models.put(stateLoc, unbaked.bake(baker, spriteGetter, BlockModelRotation.X0_Y0));
			}
		}
	}

	private static ModelBaker modelBakerFrom(ModelBakery bakery) {
		for (Method m : ModelBakery.class.getDeclaredMethods()) {
			if (m.getParameterCount() != 0 || !ModelBaker.class.isAssignableFrom(m.getReturnType())) {
				continue;
			}
			try {
				m.setAccessible(true);
				Object r = m.invoke(bakery);
				if (r instanceof ModelBaker mb) {
					return mb;
				}
			} catch (ReflectiveOperationException ignored) {
				// try next
			}
		}
		for (Field f : ModelBakery.class.getDeclaredFields()) {
			if (!ModelBaker.class.isAssignableFrom(f.getType())) {
				continue;
			}
			try {
				f.setAccessible(true);
				Object r = f.get(bakery);
				if (r instanceof ModelBaker mb) {
					return mb;
				}
			} catch (ReflectiveOperationException ignored) {
				// try next
			}
		}
		throw new IllegalStateException("Could not obtain ModelBaker from ModelBakery");
	}
}
