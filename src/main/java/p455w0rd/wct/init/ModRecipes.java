/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2016, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.init;

import appeng.core.Api;
import appeng.core.ApiDefinitions;
import appeng.core.api.definitions.ApiMaterials;
import appeng.core.api.definitions.ApiParts;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * @author p455w0rd
 *
 */
public class ModRecipes {

	public static void init() {
		if (ModConfig.WCT_MINETWEAKER_OVERRIDE) {
			return;
		}
		ApiDefinitions defs = Api.INSTANCE.definitions();
		ApiMaterials materials = defs.materials();
		ApiParts parts = defs.parts();
		ItemStack wap = materials.wirelessReceiver().maybeStack(1).get();
		ItemStack fluixPearl = materials.fluixPearl().maybeStack(1).get();
		ItemStack fluixCrystal = materials.fluixCrystal().maybeStack(1).get();
		ItemStack singularity = materials.singularity().maybeStack(1).get();
		ItemStack craftingTerminal = parts.craftingTerminal().maybeStack(1).get();
		ItemStack component64k = materials.cell64kPart().maybeStack(1).get();
		ItemStack advancedCard = materials.advCard().maybeStack(1).get();
		ItemStack annihilationPlane = parts.annihilationPlane().maybeStack(1).get();

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.WCT), new Object[] {
				"aba", "cdc", "efe", Character.valueOf('a'), fluixPearl, Character.valueOf('b'), wap, Character.valueOf('c'), fluixCrystal, Character.valueOf('d'), craftingTerminal, Character.valueOf('e'), component64k, Character.valueOf('f'), singularity
		}));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.MAGNET_CARD), new Object[] {
				"abc", "ded", "ddd", Character.valueOf('a'), "blockRedstone", Character.valueOf('b'), annihilationPlane, Character.valueOf('c'), "blockLapis", Character.valueOf('d'), "blockIron", Character.valueOf('e'), advancedCard
		}));
	}

}
