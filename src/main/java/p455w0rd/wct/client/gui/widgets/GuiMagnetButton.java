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

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import p455w0rd.wct.init.ModGlobals;

public class GuiMagnetButton extends GuiButton implements ITooltip {

	public GuiMagnetButton(int xIn, int yIn) {
		super(xIn, yIn, 8, "");
		x = xIn;
		y = yIn;
		width = 8;
		height = 8;
		visible = false;
	}

	@Override
	public void drawButton(final Minecraft mc, final int par2, final int par3, float partial) {
		if (visible) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0.0F);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			//GlStateManager.scale(0.025F, 0.025F, 0.025F);
			//GL11.glScalef(2.0f, 2.0f, 2.0f);
			mc.getTextureManager().bindTexture(new ResourceLocation(ModGlobals.MODID, "textures/gui/states.png"));
			hovered = par2 >= x && par3 >= y && par2 < x + width && par3 < y + height;
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			this.drawTexturedModalRect(0, 0, 0, 0, 16, 16);

			drawTexturedModalRect(0, 0, 16, 0, 16, 16);
			mouseDragged(mc, par2, par3);
			GL11.glPopMatrix();
		}
	}

	@Override
	public String getMessage() {
		return TextFormatting.WHITE + "" + I18n.format("gui.openmagnetgui") + "\n" + TextFormatting.GRAY + "" + I18n.format("gui.openmagnetgui.desc") + "\n" + I18n.format("gui.openmagnetgui.desc2");
	}

	@Override
	public int xPos() {
		return x;
	}

	@Override
	public int yPos() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

}