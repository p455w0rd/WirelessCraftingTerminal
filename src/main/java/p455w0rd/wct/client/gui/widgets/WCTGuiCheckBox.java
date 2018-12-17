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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiUtils;

public class WCTGuiCheckBox extends GuiCheckBox {

	private boolean isChecked;
	private int boxWidth;

	public WCTGuiCheckBox(int id, int xPos, int yPos, String displayString, boolean isChecked, int width) {
		super(id, xPos, yPos, displayString, isChecked);
		this.isChecked = isChecked;
		boxWidth = 11;
		height = 11;
		//this.width = this.boxWidth + 2 + Minecraft.getMinecraft().fontRenderer.getStringWidth(displayString);
		this.width = width;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partial) {
		if (visible) {
			hovered = mouseX >= x && mouseY >= y && mouseX < x + boxWidth && mouseY < y + height;
			GuiUtils.drawContinuousTexturedBox(BUTTON_TEXTURES, x + (width - boxWidth), y, 0, 46, boxWidth, height, 200, 20, 2, 3, 2, 2, zLevel);
			mouseDragged(mc, mouseX, mouseY);
			int color = 14737632;

			if (!enabled) {
				color = 10526880;
			}
			else {
				color = 4210752;
			}
			if (isChecked) {
				//drawCenteredString(mc.fontRenderer, "✔", x + (width - boxWidth) + boxWidth / 2 + 1, y + 1, 0x00FF00);
				drawCenteredString(mc.fontRenderer, "x", x + (width - boxWidth) + boxWidth / 2 + 1, y + 1, 0x00FF00);
			}

			drawStringNoShadow(mc.fontRenderer, displayString, x, y + 2, color);
		}
	}

	public void drawStringNoShadow(FontRenderer p_73731_1_, String p_73731_2_, int p_73731_3_, int p_73731_4_, int p_73731_5_) {
		p_73731_1_.drawString(p_73731_2_, p_73731_3_, p_73731_4_, p_73731_5_);
	}

	@Override
	public boolean mousePressed(Minecraft p_146116_1_, int p_146116_2_, int p_146116_3_) {
		if (enabled && visible && p_146116_2_ >= x && p_146116_3_ >= y && p_146116_2_ < x + width && p_146116_3_ < y + height) {
			isChecked = !isChecked;
			return true;
		}

		return false;
	}

	@Override
	public boolean isChecked() {
		return isChecked;
	}

	@Override
	public void setIsChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
