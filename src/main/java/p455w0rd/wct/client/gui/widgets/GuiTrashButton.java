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
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiTrashButton extends GuiButton implements ITooltip {

	public GuiTrashButton(int xIn, int yIn) {
		super(xIn, yIn, 8, "");
		x = xIn;
		y = yIn;
		width = 8;
		height = 8;
	}

	@Override
	public void drawButton(final Minecraft mc, final int par2, final int par3, float partial) {
		if (visible) {
			GL11.glPushMatrix();
			GL11.glTranslatef(x, y, 0.0F);
			GL11.glScalef(0.5f, 0.5f, 0.5f);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			mc.renderEngine.bindTexture(new ResourceLocation("appliedenergistics2", "textures/guis/states.png"));
			hovered = par2 >= x && par3 >= y && par2 < x + width && par3 < y + height;
			this.drawTexturedModalRect(0, 0, 256 - 16, 256 - 16, 16, 16);
			this.drawTexturedModalRect(0, 0, 6 * 16, 0, 16, 16);
			mouseDragged(mc, par2, par3);
			GL11.glPopMatrix();
		}
	}

	@Override
	public String getMessage() {
		return TextFormatting.WHITE + "" + I18n.format("gui.emptytrash") + "\n" + TextFormatting.GRAY + "" + I18n.format("gui.emptytrash.desc");
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
