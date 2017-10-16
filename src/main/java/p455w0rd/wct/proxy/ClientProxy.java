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
package p455w0rd.wct.proxy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import p455w0rd.wct.init.ModCreativeTab;
import p455w0rd.wct.init.ModGlobals.Mods;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModKeybindings;
import p455w0rd.wct.integration.ItemScroller;

/**
 * @author p455w0rd
 *
 */
public class ClientProxy extends CommonProxy {

	public static CreativeTabs CREATIVE_TAB;

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
		ModItems.initModels();
		ModKeybindings.init();
		CREATIVE_TAB = new ModCreativeTab();
	}

	@Override
	public void init(FMLInitializationEvent e) {
		super.init(e);
	}

	@Override
	public void postInit(FMLPostInitializationEvent e) {
		if (Mods.ITEMSCROLLER.isLoaded()) {
			ItemScroller.blackListMESlot();
		}
		super.postInit(e);
	}

}
