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

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import p455w0rd.wct.api.IModelHolder;
import p455w0rd.wct.items.ItemInfinityBooster;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.items.ItemWCT;
import p455w0rd.wct.items.ItemWCTCreative;
import p455w0rd.wct.items.ItemWFT;
import p455w0rd.wct.items.ItemWFTCreative;

/**
 * @author p455w0rd
 *
 */
public class ModItems {

	public static final ItemWCT WCT = new ItemWCT();
	public static final ItemWCTCreative CREATIVE_WCT = new ItemWCTCreative();
	public static final ItemMagnet MAGNET_CARD = new ItemMagnet();
	public static final ItemInfinityBooster BOOSTER_CARD = new ItemInfinityBooster();
	public static final ItemWFT WFT = new ItemWFT();
	public static final ItemWFTCreative CREATIVE_WFT = new ItemWFTCreative();

	private static final List<Item> ITEM_LIST = Lists.newArrayList(WCT, CREATIVE_WCT, MAGNET_CARD, BOOSTER_CARD, WFT, CREATIVE_WFT);

	public static final List<Item> getList() {
		return ITEM_LIST;
	}

	private static final Item[] getArray() {
		return getList().toArray(new Item[getList().size()]);
	}

	public static final void register(IForgeRegistry<Item> registry) {
		registry.registerAll(getArray());
	}

	@SideOnly(Side.CLIENT)
	public static final void initModels() {
		for (Item item : getList()) {
			if (item instanceof IModelHolder) {
				((IModelHolder) item).initModel();
			}
		}
	}

}
