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
package p455w0rd.wct.integration;

import com.google.common.collect.Lists;

import p455w0rd.wct.init.ModIntegration.Mods;

/**
 * @author p455w0rd
 *
 */
public class ItemScroller {

	public static void blackListSlots() {
		if (Mods.ITEMSCROLLER.isLoaded()) {
			fi.dy.masa.itemscroller.config.Configs.SLOT_BLACKLIST.addAll(Lists.<String>newArrayList("p455w0rd.wct.client.me.SlotME", "p455w0rd.wct.container.slot.SlotBooster", "p455w0rd.wct.container.slot.SlotMagnet"));
		}
	}

}
