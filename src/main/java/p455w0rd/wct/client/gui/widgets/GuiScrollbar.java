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

import appeng.client.gui.widgets.IScrollSource;
import net.minecraft.client.renderer.GlStateManager;
import p455w0rd.wct.client.gui.WCTBaseGui;

public class GuiScrollbar implements IScrollSource {

	private int displayX = 0;
	private int displayY = 0;
	private int width = 12;
	private int height = 16;
	private int pageSize = 1;

	private int maxScroll = 0;
	private int minScroll = 0;
	private int currentScroll = 0;

	public void draw(final WCTBaseGui g) {
		g.bindTexture("minecraft", "gui/container/creative_inventory/tabs.png");
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		if (getRange() == 0) {
			g.drawTexturedModalRect(displayX, displayY, 232 + width, 0, width, 15);
		}
		else {
			final int offset = (currentScroll - minScroll) * (height - 15) / getRange();
			g.drawTexturedModalRect(displayX, offset + displayY, 232, 0, width, 15);
		}
	}

	private int getRange() {
		return maxScroll - minScroll;
	}

	public int getLeft() {
		return displayX;
	}

	public GuiScrollbar setLeft(final int v) {
		displayX = v;
		return this;
	}

	public int getTop() {
		return displayY;
	}

	public GuiScrollbar setTop(final int v) {
		displayY = v;
		return this;
	}

	public int getWidth() {
		return width;
	}

	public GuiScrollbar setWidth(final int v) {
		width = v;
		return this;
	}

	public int getHeight() {
		return height;
	}

	public GuiScrollbar setHeight(final int v) {
		height = v;
		return this;
	}

	public void setRange(final int min, final int max, final int pageSize) {
		minScroll = min;
		maxScroll = max;
		this.pageSize = pageSize;

		if (minScroll > maxScroll) {
			maxScroll = minScroll;
		}

		applyRange();
	}

	private void applyRange() {
		currentScroll = Math.max(Math.min(currentScroll, maxScroll), minScroll);
	}

	@Override
	public int getCurrentScroll() {
		return currentScroll;
	}

	public void click(final WCTBaseGui aeBaseGui, final int x, final int y) {
		if (getRange() == 0) {
			return;
		}

		if (x > displayX && x <= displayX + width) {
			if (y > displayY && y <= displayY + height) {
				currentScroll = (y - displayY);
				currentScroll = minScroll + ((currentScroll * 2 * getRange() / height));
				currentScroll = (currentScroll + 1) >> 1;
				applyRange();
			}
		}
	}

	public void wheel(int delta) {
		delta = Math.max(Math.min(-delta, 1), -1);
		currentScroll += delta * pageSize;
		applyRange();
	}
}