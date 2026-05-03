/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import reborncore.client.multiblock.MultiblockRenderer;
import techreborn.TechRebornClient;
import techreborn.client.compat.NeoForgeMachineCasingModelBridge;
import techreborn.client.render.DynamicCellBakedModel;
import techreborn.client.render.DynamicBucketBakedModel;
import techreborn.client.render.entitys.CableCoverRenderer;
import techreborn.client.render.entitys.NukeRenderer;
import techreborn.client.render.entitys.StorageUnitRenderer;
import techreborn.client.render.entitys.TurbineRenderer;
import techreborn.init.ModFluids;
import techreborn.init.TRBlockEntities;
import techreborn.init.TRContent;

import java.util.Map;

@ApiStatus.Internal
public final class TechRebornNeoForgeClient {
	private TechRebornNeoForgeClient() {
	}

	public static void subscribeModBus(IEventBus modBus) {
		modBus.addListener(TechRebornNeoForgeClient::registerAdditionalModels);
		modBus.addListener(TechRebornNeoForgeClient::modifyBakingResult);
		modBus.addListener(TechRebornNeoForgeClient::registerRenderers);
	}

	public static void subscribeGameBus() {
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> TechRebornClient.onClientTickPost(e));
	}

	private static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		event.register(ModelResourceLocation.standalone(DynamicCellBakedModel.CELL_BASE));
		event.register(ModelResourceLocation.standalone(DynamicCellBakedModel.CELL_BACKGROUND));
		event.register(ModelResourceLocation.standalone(DynamicCellBakedModel.CELL_FLUID));
		event.register(ModelResourceLocation.standalone(DynamicCellBakedModel.CELL_GLASS));
		event.register(ModelResourceLocation.standalone(DynamicBucketBakedModel.BUCKET_BASE));
		event.register(ModelResourceLocation.standalone(DynamicBucketBakedModel.BUCKET_FLUID));
		event.register(ModelResourceLocation.standalone(DynamicBucketBakedModel.BUCKET_BACKGROUND));
	}

	private static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
		NeoForgeMachineCasingModelBridge.onModifyBakingResult(event);

		Map<ModelResourceLocation, BakedModel> models = event.getModels();

		models.put(ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(TRContent.CELL)), new DynamicCellBakedModel());

		for (ModFluids fluid : ModFluids.values()) {
			Fluid still = fluid.getFluid();
			if (still == Fluids.EMPTY) {
				continue;
			}
			Item bucket = fluid.getBucket();
			ModelResourceLocation bucketMrl = ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(bucket));
			if (models.containsKey(bucketMrl)) {
				models.put(bucketMrl, new DynamicBucketBakedModel());
			}
		}
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(TRBlockEntities.INDUSTRIAL_GRINDER, MultiblockRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.FUSION_CONTROL_COMPUTER, MultiblockRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.INDUSTRIAL_BLAST_FURNACE, MultiblockRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.VACUUM_FREEZER, MultiblockRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.FLUID_REPLICATOR, MultiblockRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.INDUSTRIAL_SAWMILL, MultiblockRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.DISTILLATION_TOWER, MultiblockRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.IMPLOSION_COMPRESSOR, MultiblockRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.GREENHOUSE_CONTROLLER, MultiblockRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.STORAGE_UNIT, StorageUnitRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.CABLE, CableCoverRenderer::new);
		event.registerBlockEntityRenderer(TRBlockEntities.WIND_MILL, TurbineRenderer::new);

		event.registerEntityRenderer(TRContent.ENTITY_NUKE, NukeRenderer::new);
	}

	public static void registerRenderLayers() {
		for (TRContent.Cables cable : TRContent.Cables.values()) {
			ItemBlockRenderTypes.setRenderLayer(cable.block, RenderType.cutout());
		}

		ItemBlockRenderTypes.setRenderLayer(TRContent.Machine.LAMP_INCANDESCENT.block, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TRContent.Machine.LAMP_LED.block, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TRContent.Machine.ALARM.block, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TRContent.RUBBER_SAPLING, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TRContent.REINFORCED_GLASS, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TRContent.Machine.RESIN_BASIN.block, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TRContent.POTTED_RUBBER_SAPLING, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TRContent.Machine.FISHING_STATION.block, RenderType.cutout());

		ItemBlockRenderTypes.setRenderLayer(TRContent.RUBBER_LEAVES, RenderType.cutoutMipped());

		for (ModFluids fluid : ModFluids.values()) {
			ItemBlockRenderTypes.setRenderLayer(fluid.getBlock(), RenderType.translucent());
		}
	}
}
