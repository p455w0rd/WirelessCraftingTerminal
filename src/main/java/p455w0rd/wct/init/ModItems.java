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

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import p455w0rd.ae2wtlib.api.IModelHolder;
import p455w0rd.ae2wtlib.client.render.ItemLayerWrapper;
import p455w0rd.ae2wtlib.client.render.WTItemRenderer;
import p455w0rd.wct.items.*;

/**
 * @author p455w0rd
 *
 */
public class ModItems {

	public static final ItemWCT WCT = new ItemWCT();
	public static final ItemWCTCreative CREATIVE_WCT = new ItemWCTCreative();
	public static final ItemMagnet MAGNET_CARD = new ItemMagnet();

	private static final Item[] ITEM_ARRAY = new Item[] {
			WCT, CREATIVE_WCT, MAGNET_CARD
	};

	public static final List<Item> getList() {
		return Lists.newArrayList(ITEM_ARRAY);
	}

	public static final void register(IForgeRegistry<Item> registry) {
		registry.registerAll(ITEM_ARRAY);
	}

	@SideOnly(Side.CLIENT)
	public static final void initModels(ModelBakeEvent event) {
		for (Item item : getList()) {
			if (item instanceof IModelHolder) {
				IModelHolder holder = (IModelHolder) item;
				holder.initModel();
				if (holder.shouldUseInternalTEISR()) {
					IBakedModel wtModel = event.getModelRegistry().getObject(holder.getModelResource());
					holder.setWrappedModel(new ItemLayerWrapper(wtModel));
					if (item.getTileEntityItemStackRenderer() instanceof WTItemRenderer) {
						WTItemRenderer renderer = (WTItemRenderer) item.getTileEntityItemStackRenderer();
						renderer.setModel(holder.getWrappedModel());
					}
					event.getModelRegistry().putObject(holder.getModelResource(), holder.getWrappedModel());
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static final void registerTEISRs(ModelRegistryEvent event) {
		for (Item item : getList()) {
			if (item instanceof IModelHolder) {
				IModelHolder holder = (IModelHolder) item;
				if (holder.shouldUseInternalTEISR()) {
					item.setTileEntityItemStackRenderer(new WTItemRenderer());
				}
			}
		}
	}

}
