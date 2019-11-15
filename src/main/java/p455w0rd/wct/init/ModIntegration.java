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
package p455w0rd.wct.init;

import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.ICustomWirelessTerminalItem;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wct.integration.ItemScroller;
import p455w0rdslib.LibGlobals.Mods;
import p455w0rdslib.api.client.IModelHolder;
import p455w0rdslib.api.client.ItemRenderingRegistry;

/**
 * @author p455w0rd
 *
 */
public class ModIntegration {

	public static void preInit() {
		WTApi.instance().getWirelessTerminalRegistry().registerWirelessTerminal(ModItems.WCT, ModItems.CREATIVE_WCT);
	}

	@SideOnly(Side.CLIENT)
	public static void preInitClient() {
		for (final Item item : ModItems.getList()) {
			if (item instanceof IModelHolder && !(item instanceof ICustomWirelessTerminalItem)) {
				ItemRenderingRegistry.registerCustomRenderingItem((IModelHolder) item);
			}
		}
	}

	public static void postInit() {
		if (Mods.ITEMSCROLLER.isLoaded()) {
			ItemScroller.blackListSlots();
		}
	}

}
