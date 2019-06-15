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

import java.awt.Color;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wct.api.IWCTContainer;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.items.ItemMagnet.MagnetFunctionMode;
import p455w0rd.wct.sync.packets.PacketSetMagnetWCT;

/**
 * @author p455w0rd
 *
 */
public class GuiImgButtonMagnetMode extends GuiButton implements ITooltip {

	private MagnetFunctionMode currentValue = MagnetFunctionMode.INACTIVE;
	final IWCTContainer container;
	int iconIndex = 0;

	public GuiImgButtonMagnetMode(final int x, final int y, final IWCTContainer container) {
		super(0, x, y, "");
		this.container = container;
		this.x = x;
		this.y = y;
		width = 16;
		height = 16;
		currentValue = ItemMagnet.getMagnetFunctionMode(container.getMagnet());
		visible = false;
	}

	public void setVisibility(final boolean vis) {
		visible = vis;
		enabled = vis;
		currentValue = ItemMagnet.getMagnetFunctionMode(container.getMagnet());
	}

	@Override
	public void drawButton(final Minecraft mc, final int mouseX, final int mouseY, final float partial) {
		if (visible) {
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			GlStateManager.pushMatrix();
			mc.renderEngine.bindTexture(WTApi.instance().getConstants().getStatesTexture());
			this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
			this.drawTexturedModalRect(x, y, 16, 0, 16, 16);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			renderGlint(mc);
			GlStateManager.popMatrix();
			mouseDragged(mc, mouseX, mouseY);
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	@Override
	public String getMessage() {
		return I18n.format("gui.magnetmode") + "\n" + TextFormatting.GRAY + "" + currentValue.getMessage().replace(" - ", "\n");
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

	public void cycleValue() {
		currentValue = ItemMagnet.cycleMagnetFunctionModeWCT(container.getPlayer(), container.getWTSlot(), container.isWTBauble());
		ModNetworking.instance().sendToServer(new PacketSetMagnetWCT(currentValue));
	}

	public void renderGlint(final Minecraft mc) {
		if (currentValue == MagnetFunctionMode.INACTIVE) {
			return;
		}
		final Color color = new Color(-8372020);
		mc.renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
		GlStateManager.enableBlend();
		GlStateManager.depthFunc(514);
		GlStateManager.depthMask(true);
		GlStateManager.color(0.1F, 0.1F, 0.1F, 1.0F);
		for (int i = 0; i < 2; ++i) {
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
			GlStateManager.color(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 1.0F);
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			GlStateManager.scale(0.00001F, 0.00001F, 0.00001F);
			GlStateManager.rotate(30.0F - i * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, Minecraft.getSystemTime() % 3000L / 3000.0F / 3.0F, 0.0F);
			GlStateManager.matrixMode(5888);
			mc.renderEngine.bindTexture(WTApi.instance().getConstants().getStatesTexture());
			//this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
			this.drawTexturedModalRect(x, y, 16, 0, 16, 16);
		}
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		//GlStateManager.enableLighting();
		//GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
		GlStateManager.disableBlend();
	}

}
