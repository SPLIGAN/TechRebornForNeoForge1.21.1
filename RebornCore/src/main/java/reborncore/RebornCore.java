/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 TeamReborn
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

package reborncore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reborncore.api.ToolManager;
import reborncore.api.blockentity.UnloadHandler;
import reborncore.api.items.ArmorRemoveHandler;
import reborncore.common.RebornCoreCommands;
import reborncore.common.RebornCoreConfig;
import reborncore.common.blocks.BlockWrenchEventHandler;
import reborncore.common.chunkloading.ChunkLoaderManager;
import reborncore.common.config.Configuration;
import reborncore.common.event.ServerLifecycleBridge;
import reborncore.common.misc.RebornCoreTags;
import reborncore.common.multiblock.MultiblockRegistry;
import reborncore.common.recipes.PaddedShapedRecipe;
import reborncore.common.screen.ServerPlayerEntityScreenHandlerHelper;
import reborncore.common.util.CalenderUtils;
import reborncore.common.util.GenericWrenchHelper;
import reborncore.common.util.LoaderBridge;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RebornCore {

	public static final String MOD_ID = "reborncore";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Locale locale = Locale.ROOT;

	public void onInitialize() {
		new Configuration(RebornCoreConfig.class, MOD_ID);
		CalenderUtils.loadCalender(); // Done early as some features need this

		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("intergrateddynamics:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("thermal:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("rftoolsbase:smartwrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("redstone_arsenal:flux_wrench"), false));

		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("ad_astra:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("ae2:certus_quartz_wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("ae2:nether_quartz_wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("bitsandchisels:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("create:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("indrev:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(ResourceLocation.parse("modern_industialization:wrench"), false));

		BlockWrenchEventHandler.setup();

		/*
		This is a generic multiblock tick handler. If you are using this code on your
		own, you will need to register this with the Forge TickRegistry on both the
		client AND server sides. Note that different types of ticks run on different
		parts of the system. CLIENT ticks only run on the client, at the start/end of
		each game loop. SERVER and WORLD ticks only run on the server. WORLDLOAD
		ticks run only on the server, and only when worlds are loaded.
		 */
		ServerLifecycleBridge.onStartWorldTick(MultiblockRegistry::tickStart);

		RebornCoreCommands.setup();

		//noinspection ResultOfMethodCallIgnored
		RebornCoreTags.WATER_EXPLOSION_ITEM.toString();
		//noinspection ResultOfMethodCallIgnored
		PaddedShapedRecipe.PADDED.toString();

		/* register UnloadHandler */
		ServerLifecycleBridge.onBlockEntityUnload((blockEntity, world) -> {
			if (blockEntity instanceof UnloadHandler) ((UnloadHandler) blockEntity).onUnload();
		});

		ServerLifecycleBridge.onWorldLoad((server, world) -> ChunkLoaderManager.get(world).onServerWorldLoad(world));
		ServerLifecycleBridge.onStartWorldTick(world -> ChunkLoaderManager.get(world).onServerWorldTick(world));

		ServerLifecycleBridge.onEquipmentChange((livingEntity, equipmentSlot, previousStack, currentStack) -> {
			if (livingEntity instanceof Player playerEntity
				&& previousStack.getItem() instanceof ArmorRemoveHandler armorRemoveHandler
				&& !ItemStack.isSameItem(previousStack, currentStack)) {
				armorRemoveHandler.onRemoved(playerEntity);
			}
		});

		//noinspection ResultOfMethodCallIgnored
		ServerPlayerEntityScreenHandlerHelper.class.getName();
	}

	public static LoaderBridge.Side getSide() {
		return LoaderBridge.getEnvironmentType();
	}
}
