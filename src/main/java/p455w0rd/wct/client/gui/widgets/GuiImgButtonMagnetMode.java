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

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import p455w0rd.wct.init.ModGlobals;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class GuiImgButtonMagnetMode extends GuiButton implements ITooltip {

	private ItemStack wirelessTerminal = ItemStack.EMPTY;
	private int currentValue = 0;
	int iconIndex = 0;

	public GuiImgButtonMagnetMode(final int x, final int y, ItemStack wirelessTerminal) {
		super(0, x, y, "");
		this.x = x;
		this.y = y;
		width = 16;
		height = 16;
		this.wirelessTerminal = wirelessTerminal;
		currentValue = WCTUtils.getMagnetMode(wirelessTerminal);
	}

	public void setVisibility(final boolean vis) {
		visible = vis;
		enabled = vis;
	}

	@Override
	public void drawButton(final Minecraft mc, final int mouseX, final int mouseY, float partial) {
		if (visible) {
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			//GlStateManager.pushMatrix();
			mc.renderEngine.bindTexture(new ResourceLocation(ModGlobals.MODID, "textures/gui/states.png"));
			this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
			this.drawTexturedModalRect(x, y, 16, 0, 16, 16);
			//GlStateManager.popMatrix();

			mouseDragged(mc, mouseX, mouseY);
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public int getCurrentValue() {
		return currentValue;
	}

	@Override
	public String getMessage() {
		return I18n.format("gui.magnetmode") + "\n" + TextFormatting.GRAY + "" + ItemMagnet.getMessage(WCTUtils.getMagnetMode(getWirelessTerminal())).replace(" - ", "\n");
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

	public ItemStack getWirelessTerminal() {
		return wirelessTerminal;
	}

	public void cycleValue() {
		ItemMagnet.switchMagnetMode(WCTUtils.getMagnet(getWirelessTerminal()));
		currentValue = WCTUtils.getMagnetMode(getWirelessTerminal());
	}

}
