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

import appeng.api.AEApi;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import p455w0rd.wct.WCT;
import p455w0rd.wct.handlers.GuiHandler;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModEvents;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModRecipes;
import p455w0rd.wct.sync.network.NetworkHandler;

/**
 * @author p455w0rd
 *
 */
public class CommonProxy {

	public void preInit(FMLPreInitializationEvent e) {
		ModConfig.init();
		ModItems.init();
		NetworkHandler.init();
		ModEvents.init();
		AEApi.instance().registries().wireless().registerWirelessHandler(ModItems.WCT);
		ModRecipes.init();
	}

	public void init(FMLInitializationEvent e) {

	}

	public void postInit(FMLPostInitializationEvent e) {
		NetworkRegistry.INSTANCE.registerGuiHandler(WCT.INSTANCE, new GuiHandler());
	}

}
