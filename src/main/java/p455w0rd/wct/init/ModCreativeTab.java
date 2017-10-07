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

import appeng.api.config.Actionable;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * @author p455w0rd
 *
 */
public class ModCreativeTab extends CreativeTabs {

	public ModCreativeTab() {
		super(ModGlobals.MODID);
		//setBackgroundImageName(Globals.MODID + ".png");
	}

	@Override
	public ItemStack getIconItemStack() {
		ItemStack is = new ItemStack(ModItems.WCT);
		((AEBasePoweredItem) is.getItem()).injectAEPower(is, ModConfig.WCT_MAX_POWER, Actionable.MODULATE);
		return is;
	}

	@Override
	public void displayAllRelevantItems(NonNullList<ItemStack> items) {

		items.add(new ItemStack(ModItems.WCT));
		ItemStack is = new ItemStack(ModItems.WCT);
		((AEBasePoweredItem) is.getItem()).injectAEPower(is, ModConfig.WCT_MAX_POWER, Actionable.MODULATE);
		items.add(is);
		items.add(new ItemStack(ModItems.MAGNET_CARD));
		if (ModConfig.WCT_BOOSTER_ENABLED) {
			items.add(new ItemStack(ModItems.BOOSTER_CARD));
		}

		super.displayAllRelevantItems(items);
	}

	@Override
	public ItemStack getTabIconItem() {
		return getIconItemStack();
	}

}