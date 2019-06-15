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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketSetShiftCraft;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class GuiImgButtonShiftCraft extends GuiButton implements ITooltip {

	private ItemStack wirelessTerminal = ItemStack.EMPTY;
	private boolean currentValue = true;
	int iconIndex = 0;

	public GuiImgButtonShiftCraft(final int x, final int y, final ItemStack wirelessTerminal) {
		super(0, x, y, "");
		this.x = x;
		this.y = y;
		width = 16;
		height = 16;
		this.wirelessTerminal = wirelessTerminal;
		currentValue = getCurrentValue();
	}

	public void setVisibility(final boolean vis) {
		visible = vis;
		enabled = vis;
	}

	@Override
	public void drawButton(final Minecraft mc, final int mouseX, final int mouseY, final float partial) {
		if (visible) {
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			mc.renderEngine.bindTexture(WTApi.instance().getConstants().getStatesTexture());
			this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
			this.drawTexturedModalRect(x, y, (!getCurrentValue() ? 7 : 8) * 16, 0, 16, 16);
			mouseDragged(mc, mouseX, mouseY);
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public boolean getCurrentValue() {
		if (!getWirelessTerminal().hasTagCompound()) {
			getWirelessTerminal().setTagCompound(new NBTTagCompound());
		}
		if (!getWirelessTerminal().getTagCompound().hasKey(WCTUtils.SHIFTCRAFT_NBT)) {
			setValue(currentValue);
		}
		currentValue = getWirelessTerminal().getTagCompound().getBoolean(WCTUtils.SHIFTCRAFT_NBT);
		return currentValue;
	}

	@Override
	public String getMessage() {
		return I18n.format("tooltip.shiftcraft.title") + "\n" + TextFormatting.GRAY + "" + I18n.format("tooltip.shiftcraft.desc_pre").replace("\\n", "\n") + "\n" + TextFormatting.AQUA + "" + (!getCurrentValue() ? I18n.format("tooltip.shiftcraft.to_network") : I18n.format("tooltip.shiftcraft.to_inventory"));
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
		setValue(!getCurrentValue());
	}

	private void setValue(final boolean value) {
		if (!getWirelessTerminal().hasTagCompound()) {
			getWirelessTerminal().setTagCompound(new NBTTagCompound());
		}
		getWirelessTerminal().getTagCompound().setBoolean(WCTUtils.SHIFTCRAFT_NBT, value);
		ModNetworking.instance().sendToServer(new PacketSetShiftCraft(value));
	}

}
