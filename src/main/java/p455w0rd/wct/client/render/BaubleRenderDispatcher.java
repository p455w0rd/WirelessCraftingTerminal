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
package p455w0rd.wct.client.render;

import java.util.Map;
import java.util.WeakHashMap;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.api.IBaubleItem;
import p455w0rd.wct.api.IBaubleRender;

/**
 * from EnderIO
 *
 */
public class BaubleRenderDispatcher implements LayerRenderer<AbstractClientPlayer> {

	public final static BaubleRenderDispatcher instance = new BaubleRenderDispatcher(null);

	private final RenderPlayer renderPlayer;

	public BaubleRenderDispatcher(RenderPlayer renderPlayer) {
		this.renderPlayer = renderPlayer;
	}

	private static final Map<RenderPlayer, Object> REGISTRY = new WeakHashMap<RenderPlayer, Object>();

	public static final Map<RenderPlayer, Object> getRegistry() {
		return REGISTRY;
	}

	@Override
	public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
		IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(entitylivingbaseIn);
		if (baubles != null) {
			for (int i = 0; i < baubles.getSlots(); i++) {
				ItemStack piece = baubles.getStackInSlot(i);
				if (piece != null && piece.getItem() instanceof IBaubleItem) {
					IBaubleRender render = ((IBaubleItem) piece.getItem()).getRender();
					if (render != null) {
						render.doRenderLayer(renderPlayer, piece, entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale);
					}
				}
			}
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return true;
	}

}