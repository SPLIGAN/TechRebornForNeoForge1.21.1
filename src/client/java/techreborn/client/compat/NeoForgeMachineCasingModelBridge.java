/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.client.compat;

import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * NeoForge replacement for Fabric {@code ModelLoadingPlugin} wiring for machine casing connected textures.
 * TODO CLI-04: register {@link techreborn.client.render.MachineCasingModel} via
 * {@link net.neoforged.neoforge.client.event.RegisterBlockStateModels} and blockstate definition JSON.
 */
public final class NeoForgeMachineCasingModelBridge {
	private NeoForgeMachineCasingModelBridge() {
	}

	public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
		// Connected casing models require CustomBlockModelDefinition registration (26.1 API).
	}
}
