/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
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

package techreborn;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reborncore.common.blockentity.RedstoneConfiguration;
import reborncore.common.config.Configuration;
import reborncore.common.recipes.RecipeCrafter;
import reborncore.common.util.Torus;
import techreborn.blockentity.GuiType;
import techreborn.config.TechRebornConfig;
import techreborn.events.ApplyArmorToDamageHandler;
import techreborn.events.UseBlockHandler;
import techreborn.init.FuelRecipes;
import techreborn.init.ModLoot;
import techreborn.init.ModRecipes;
import techreborn.init.ModSounds;
import techreborn.init.TRBlockEntities;
import techreborn.init.TRCauldronBehavior;
import techreborn.init.TRContent;
import techreborn.init.TRDispenserBehavior;
import techreborn.init.template.TechRebornTemplates;
import techreborn.items.DynamicCellItem;
import techreborn.utils.PoweredCraftingHandler;
public class TechReborn {
	public static final String MOD_ID = "techreborn";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Fabric {@code main} entry — runs on dedicated server and on the game client (before joining a world).
	 * Payload codecs and registry setup must stay free of {@code net.minecraft.client} types so both environments load cleanly.
	 */
	public void onInitialize() {
		new Configuration(TechRebornConfig.class, "techreborn");
		TRContent.register();

		// Done to force the class to load
		//noinspection ResultOfMethodCallIgnored
		ModRecipes.GRINDER.hashCode();
		TRContent.SCRAP_BOX.asItem();

		if (TechRebornConfig.machineSoundVolume > 0) {
			if (TechRebornConfig.machineSoundVolume > 1) TechRebornConfig.machineSoundVolume = 1F;
			RecipeCrafter.soundHandler = new ModSounds.SoundHandler();
		}
		ModLoot.init();
		//Force loads the block entities at the right time
		//noinspection ResultOfMethodCallIgnored
		TRBlockEntities.THERMAL_GEN.toString();
		//noinspection ResultOfMethodCallIgnored
		GuiType.AESU.getIdentifier();
		TRDispenserBehavior.init();
		TRCauldronBehavior.init();
		PoweredCraftingHandler.setup();
		UseBlockHandler.init();
		ApplyArmorToDamageHandler.init();
		FuelRecipes.init();


		Torus.genSizeMap(TechRebornConfig.fusionControlComputerMaxCoilSize);

		RedstoneConfiguration.fluidStack = DynamicCellItem.getCellWithFluid(Fluids.LAVA);
		RedstoneConfiguration.powerStack = new ItemStack(TRContent.RED_CELL_BATTERY);

		ComposterBlock.COMPOSTABLES.put(TRContent.RUBBER_SAPLING.asItem(), 0.3F);
		ComposterBlock.COMPOSTABLES.put(TRContent.RUBBER_LEAVES.asItem(), 0.3F);
		ComposterBlock.COMPOSTABLES.put(TRContent.Parts.PLANTBALL.asItem(), 1F);
		ComposterBlock.COMPOSTABLES.put(TRContent.Parts.COMPRESSED_PLANTBALL.asItem(), 1F);
		ComposterBlock.COMPOSTABLES.put(TRContent.Dusts.SAW.asItem(), 0.3F);
		ComposterBlock.COMPOSTABLES.put(TRContent.SmallDusts.SAW.asItem(), 0.1F);

		TechRebornTemplates.init();

		LOGGER.info("TechReborn setup done!");
	}
}
