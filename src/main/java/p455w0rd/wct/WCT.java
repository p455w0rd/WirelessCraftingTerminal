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
package p455w0rd.wct;

import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import p455w0rd.wct.proxy.CommonProxy;

/**
 * @author p455w0rd
 *
 */
@Mod(modid = Globals.MODID, name = Globals.NAME, version = Globals.VERSION, dependencies = Globals.DEP_LIST, acceptedMinecraftVersions = "[1.10.2]")
public class WCT {

	@SidedProxy(clientSide = Globals.CLIENT_PROXY, serverSide = Globals.SERVER_PROXY)
	public static CommonProxy PROXY;

	@Mod.Instance(Globals.MODID)
	public static WCT INSTANCE;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		INSTANCE = this;
		PROXY.preInit(e);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e) {
		PROXY.init(e);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		PROXY.postInit(e);
	}

}
