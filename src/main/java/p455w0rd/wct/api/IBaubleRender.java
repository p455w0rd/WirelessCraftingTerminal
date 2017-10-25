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
package p455w0rd.wct.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBaubleRender {

	/**
	 * Credit to EnderIO team, used from EnderIO
	 *
	 * A few helper methods for rendering. Credit to Vazkii, used from Botania.
	 * {@link #translateToHeadLevel(EntityPlayer)} edited to remove sneaking
	 * translation.
	 */
	public static class Helper {

		public static void rotateIfSneaking(EntityPlayer player) {
			if (player.isSneaking()) {
				applySneakingRotation();
			}
		}

		public static void applySneakingRotation() {
			GlStateManager.rotate(28.64789F, 1.0F, 0.0F, 0.0F);
		}

		public static void translateToHeadLevel(EntityPlayer player) {
			GlStateManager.translate(0, (player != Minecraft.getMinecraft().player ? 1.7F : 0) - player.getDefaultEyeHeight(), 0);
		}
	}

	void doRenderLayer(RenderPlayer renderPlayer, ItemStack piece, AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale);

}
