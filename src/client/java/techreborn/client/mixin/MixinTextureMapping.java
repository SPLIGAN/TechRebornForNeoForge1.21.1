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

package techreborn.client.mixin;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import techreborn.datagen.ModelPathOverrides;

@Mixin(TextureMapping.class)
public class MixinTextureMapping {
	@Inject(method = "getItemTexture(Lnet/minecraft/world/item/Item;)Lnet/minecraft/client/resources/model/sprite/Material;", at = @At("HEAD"), cancellable = true)
	private static void getItemTexture(Item item, CallbackInfoReturnable<Material> cir) {
		ModelPathOverrides.ifPresent(item, (Identifier id) -> cir.setReturnValue(new Material(id)));
	}

	@Inject(method = "getItemTexture(Lnet/minecraft/world/item/Item;Ljava/lang/String;)Lnet/minecraft/client/resources/model/sprite/Material;", at = @At("HEAD"), cancellable = true)
	private static void getItemTexture(Item item, String suffix, CallbackInfoReturnable<Material> cir) {
		ModelPathOverrides.ifPresent(item, suffix, (Identifier id) -> cir.setReturnValue(new Material(id)));
	}

	@Inject(method = "getBlockTexture(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/client/resources/model/sprite/Material;", at = @At("HEAD"), cancellable = true)
	private static void getBlockTexture(Block block, CallbackInfoReturnable<Material> cir) {
		ModelPathOverrides.ifPresentOrAlias(block, (Identifier id) -> cir.setReturnValue(new Material(id)));
	}

	@Inject(method = "getBlockTexture(Lnet/minecraft/world/level/block/Block;Ljava/lang/String;)Lnet/minecraft/client/resources/model/sprite/Material;", at = @At("HEAD"), cancellable = true)
	private static void getBlockTexture(Block block, String suffix, CallbackInfoReturnable<Material> cir) {
		ModelPathOverrides.ifPresent(block, suffix, (Identifier id) -> cir.setReturnValue(new Material(id)));
	}
}
