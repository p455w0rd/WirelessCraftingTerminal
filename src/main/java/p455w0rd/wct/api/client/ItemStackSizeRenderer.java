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
package p455w0rd.wct.api.client;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import p455w0rd.ae2wtlib.api.client.StackSizeRenderer;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class ItemStackSizeRenderer extends StackSizeRenderer<IAEItemStack> {

	private static final ItemStackSizeRenderer INSTANCE = new ItemStackSizeRenderer();

	public static final ItemStackSizeRenderer getInstance() {
		return INSTANCE;
	}

	@Override
	public void renderStackSize(FontRenderer fontRenderer, IAEItemStack aeStack, int xPos, int yPos) {
		if (aeStack != null) {
			final float scaleFactor = AEConfig.instance().useTerminalUseLargeFont() ? 0.85f : 0.5f;
			final float inverseScaleFactor = 1.0f / scaleFactor;
			final int offset = AEConfig.instance().useTerminalUseLargeFont() ? 0 : -1;

			final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
			fontRenderer.setUnicodeFlag(false);

			if (aeStack.getStackSize() == 0 && aeStack.isCraftable()) {
				final String craftLabelText = AEConfig.instance().useTerminalUseLargeFont() ? GuiText.LargeFontCraft.getLocal() : GuiText.SmallFontCraft.getLocal();
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableBlend();
				GlStateManager.pushMatrix();
				GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
				final int X = (int) (((float) xPos + offset + 16.0f - fontRenderer.getStringWidth(craftLabelText) * scaleFactor) * inverseScaleFactor);
				final int Y = (int) (((float) yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
				fontRenderer.drawStringWithShadow(craftLabelText, X, Y, 16777215);
				GlStateManager.popMatrix();
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
				GlStateManager.enableBlend();
			}

			if (aeStack.getStackSize() > 0) {
				final String stackSize = getToBeRenderedStackSize(aeStack.getStackSize());

				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableBlend();
				GlStateManager.pushMatrix();
				GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
				final int X = (int) (((float) xPos + offset + 16.0f - fontRenderer.getStringWidth(stackSize) * scaleFactor) * inverseScaleFactor);
				final int Y = (int) (((float) yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
				fontRenderer.drawStringWithShadow(stackSize, X, Y, 16777215);
				GlStateManager.popMatrix();
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
				GlStateManager.enableBlend();
			}

			fontRenderer.setUnicodeFlag(unicodeFlag);
		}
	}

	private String getToBeRenderedStackSize(final long originalSize) {
		if (AEConfig.instance().useTerminalUseLargeFont()) {
			return getSlimConverter().toSlimReadableForm(originalSize);
		}
		else {
			return getWideConverter().toWideReadableForm(originalSize);
		}
	}
}
