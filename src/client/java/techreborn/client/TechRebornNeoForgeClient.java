/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 */

package techreborn.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import reborncore.client.multiblock.MultiblockRenderer;
import techreborn.TechRebornClient;
import techreborn.client.compat.NeoForgeMachineCasingModelBridge;
import techreborn.client.render.ActiveProperty;
import techreborn.client.render.ItemBucketModel;
import techreborn.client.render.ItemCellModel;
import techreborn.client.render.entitys.CableCoverRenderer;
import techreborn.client.render.entitys.NukeRenderer;
import techreborn.client.render.entitys.StorageUnitRenderer;
import techreborn.client.render.entitys.TurbineRenderer;
import techreborn.init.TRBlockEntities;
import techreborn.init.TRContent;

@ApiStatus.Internal
public final class TechRebornNeoForgeClient {
	private TechRebornNeoForgeClient() {
	}

	public static void subscribeModBus(IEventBus modBus) {
		modBus.addListener(TechRebornNeoForgeClient::registerItemModels);
		modBus.addListener(TechRebornNeoForgeClient::registerSelectItemProperties);
		modBus.addListener(TechRebornNeoForgeClient::modifyBakingResult);
		modBus.addListener(TechRebornNeoForgeClient::registerRenderers);
	}

	public static void subscribeGameBus() {
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> TechRebornClient.onClientTickPost(e));
	}

	private static void registerItemModels(RegisterItemModelsEvent event) {
		event.register(ItemCellModel.ID, ItemCellModel.Unbaked.CODEC);
		event.register(ItemBucketModel.ID, ItemBucketModel.Unbaked.CODEC);
	}

	private static void registerSelectItemProperties(RegisterSelectItemModelPropertyEvent event) {
		event.register(ActiveProperty.ID, ActiveProperty.TYPE);
	}

	private static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
		NeoForgeMachineCasingModelBridge.onModifyBakingResult(event);
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
		// 26.1: render layers are defined in block/fluid model JSON; ItemBlockRenderTypes removed.
	}
}
