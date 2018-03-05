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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.wct.api.IModelHolder;
import p455w0rd.wct.items.ItemInfinityBooster;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.items.ItemWCT;
import p455w0rd.wct.items.ItemWCTCreative;

/**
 * @author p455w0rd
 *
 */
public class ModItems {

	private static List<Item> ITEM_LIST = new ArrayList<Item>();

	public static ItemWCT WCT;
	public static ItemWCTCreative CREATIVE_WCT;
	public static ItemMagnet MAGNET_CARD;
	public static ItemInfinityBooster BOOSTER_CARD;

	public static void preInit() {
		ITEM_LIST.add(WCT = new ItemWCT());
		ITEM_LIST.add(CREATIVE_WCT = new ItemWCTCreative());
		ITEM_LIST.add(MAGNET_CARD = new ItemMagnet());
		ITEM_LIST.add(BOOSTER_CARD = new ItemInfinityBooster());
	}

	@SideOnly(Side.CLIENT)
	public static void preInitModels() {
		for (Item item : ITEM_LIST) {
			if (item instanceof IModelHolder) {
				((IModelHolder) item).initModel();
			}
		}
	}

}
