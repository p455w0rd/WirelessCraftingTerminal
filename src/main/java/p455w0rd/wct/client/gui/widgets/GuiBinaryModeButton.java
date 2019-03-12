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
package p455w0rd.wct.client.gui.widgets;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class GuiBinaryModeButton extends GuiButton {

	protected static final ResourceLocation buttonTextures = new ResourceLocation("minecraft", "textures/gui/widgets.png");

	public GuiBinaryModeButton(int p_i1020_1_, int p_i1020_2_, int p_i1020_3_, String p_i1020_4_) {
		this(p_i1020_1_, p_i1020_2_, p_i1020_3_, 200, 20, p_i1020_4_);
	}

	public GuiBinaryModeButton(int p_i1021_1_, int p_i1021_2_, int p_i1021_3_, int p_i1021_4_, int p_i1021_5_, String p_i1021_6_) {
		super(p_i1021_1_, p_i1021_2_, p_i1021_3_, p_i1021_4_, p_i1021_5_, p_i1021_6_);
		width = 200;
		height = 20;
		enabled = true;
		visible = true;
		id = p_i1021_1_;
		x = p_i1021_2_;
		y = p_i1021_3_;
		width = p_i1021_4_;
		height = p_i1021_5_;
		displayString = p_i1021_6_;
	}

	@Override
	public void drawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_, float partial) {
		if (visible) {
			FontRenderer fontrenderer = p_146112_1_.fontRenderer;
			p_146112_1_.getTextureManager().bindTexture(buttonTextures);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			hovered = p_146112_2_ >= x && p_146112_3_ >= y && p_146112_2_ < x + width && p_146112_3_ < y + height;
			int k = getHoverState(hovered);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			this.drawTexturedModalRect(x, y, 0, 46 + k * 20, width / 2, height);
			this.drawTexturedModalRect(x + width / 2, y, 200 - width / 2, 46 + k * 20, width / 2, height - 2);
			this.drawTexturedModalRect(x + 2, y + (height - 2), 200 - (width - 2), 64 + k * 20, width - 2, 2);
			this.drawTexturedModalRect(x, y + (height - 1), 200 - width, 65 + k * 20, width, 1);
			mouseDragged(p_146112_1_, p_146112_2_, p_146112_3_);
			int l = 14737632;

			if (packedFGColour != 0) {
				l = packedFGColour;
			}
			else if (!enabled) {
				l = 10526880;
			}
			else if (hovered) {
				l = 16777120;
			}

			drawCenteredString(fontrenderer, displayString, x + width / 2, y + (height - 8) / 2, l);
		}
	}

}
