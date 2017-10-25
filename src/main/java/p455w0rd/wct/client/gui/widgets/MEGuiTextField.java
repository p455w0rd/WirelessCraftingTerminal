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

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/**
 * A modified version of the Minecraft text field.
 * You can initialize it over the full element span.
 * The mouse click area is increased to the full element
 * subtracted with the defined padding.
 *
 * The rendering does pay attention to the size of the '_' caret.
 */
public class MEGuiTextField extends GuiTextField {
	private static final int PADDING = 2;

	private final int _xPos;
	private final int _yPos;
	private final int _width;
	private final int _height;
	private int selectionColor = 0xFF00FF00;

	/**
	 * Uses the values to instantiate a padded version of a text field.
	 * Pays attention to the '_' caret.
	 *
	 * @param fontRenderer renderer for the strings
	 * @param xPos absolute left position
	 * @param yPos absolute top position
	 * @param width absolute width
	 * @param height absolute height
	 */
	public MEGuiTextField(final FontRenderer fontRenderer, final int xPos, final int yPos, final int width, final int height) {
		super(0, fontRenderer, xPos + PADDING, yPos + PADDING, width - 2 * PADDING - fontRenderer.getCharWidth('_'), height - 2 * PADDING);

		_xPos = xPos;
		_yPos = yPos;
		_width = width;
		_height = height;
	}

	@Override
	public boolean mouseClicked(final int xPos, final int yPos, final int button) {
		super.mouseClicked(xPos, yPos, button);

		final boolean requiresFocus = isMouseIn(xPos, yPos);

		if (!isFocused()) {
			setFocused(requiresFocus);
		}
		return true;
	}

	/**
	 * Checks if the mouse is within the element
	 *
	 * @param xCoord current x coord of the mouse
	 * @param yCoord current y coord of the mouse
	 *
	 * @return true if mouse position is within the text field area
	 */
	public boolean isMouseIn(final int xCoord, final int yCoord) {
		final boolean withinXRange = xCoord >= _xPos && xCoord <= _xPos + _width;
		final boolean withinYRange = _yPos <= yCoord && yCoord < _yPos + _height;

		return withinXRange && withinYRange;
	}

	public void selectAll() {
		setCursorPosition(0);
		setSelectionPos(getMaxStringLength());
	}

	public void setSelectionColor(int color) {
		selectionColor = color;
	}

	@Override
	public void drawSelectionBox(int startX, int startY, int endX, int endY) {
		if (startX < endX) {
			int i = startX;
			startX = endX;
			endX = i;
		}

		startX += 1;
		endX -= 1;

		if (startY < endY) {
			int j = startY;
			startY = endY;
			endY = j;
		}

		startY -= PADDING;

		if (endX > x + width) {
			endX = x + width;
		}

		if (startX > x + width) {
			startX = x + width;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();

		float red = (selectionColor >> 16 & 255) / 255.0F;
		float blue = (selectionColor >> 8 & 255) / 255.0F;
		float green = (selectionColor & 255) / 255.0F;
		float alpha = (selectionColor >> 24 & 255) / 255.0F;

		GlStateManager.color(red, green, blue, alpha);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(startX, endY, 0.0D).endVertex();
		bufferbuilder.pos(endX, endY, 0.0D).endVertex();
		bufferbuilder.pos(endX, startY, 0.0D).endVertex();
		bufferbuilder.pos(startX, startY, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}
}
