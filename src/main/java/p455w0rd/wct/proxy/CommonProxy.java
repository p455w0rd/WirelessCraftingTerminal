/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
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

import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.event.*;
import p455w0rd.wct.init.ModIntegration;
import p455w0rd.wct.init.ModNetworking;

/**
 * @author p455w0rd
 *
 */
public class CommonProxy {

	private static LoaderState WCT_STATE = LoaderState.NOINIT;

	public void preInit(FMLPreInitializationEvent e) {
		WCT_STATE = LoaderState.PREINITIALIZATION;
		ModNetworking.preInit();
		ModIntegration.preInit();
	}

	public void init(FMLInitializationEvent e) {
		WCT_STATE = LoaderState.INITIALIZATION;
	}

	public void postInit(FMLPostInitializationEvent e) {
		WCT_STATE = LoaderState.POSTINITIALIZATION;
		ModNetworking.postInit();
	}

	public LoaderState getLoaderState() {
		return WCT_STATE;
	}

}
