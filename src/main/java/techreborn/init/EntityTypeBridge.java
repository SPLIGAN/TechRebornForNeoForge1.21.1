/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import techreborn.TechReborn;
import techreborn.entities.EntityNukePrimed;

public final class EntityTypeBridge {
	private EntityTypeBridge() {
	}

	public static EntityType<EntityNukePrimed> createNukeType() {
		ResourceKey<EntityType<?>> key = ResourceKey.create(
			Registries.ENTITY_TYPE,
			Identifier.fromNamespaceAndPath(TechReborn.MOD_ID, "nuke")
		);
		return EntityType.Builder.<EntityNukePrimed>of(EntityNukePrimed::new, MobCategory.MISC)
			.sized(1f, 1f)
			.clientTrackingRange(160)
			.build(key);
	}
}
