/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2016, p455w0rd
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
package p455w0rd.wct.client.render;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

/**
 * @author p455w0rd
 *
 */
public class StackSizeRenderer {

	private static final ReadableNumberConverter CONVERTER = ReadableNumberConverter.INSTANCE;

	public void renderStackSize(FontRenderer fontRenderer, IAEItemStack aeStack, ItemStack is, int xPos, int yPos) {
		if (is != null) {
			final float scaleFactor = AEConfig.instance().useTerminalUseLargeFont() ? 0.85f : 0.5f;
			final float inverseScaleFactor = 1.0f / scaleFactor;
			final int offset = AEConfig.instance().useTerminalUseLargeFont() ? 0 : -1;

			final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
			fontRenderer.setUnicodeFlag(false);

			if (is.getCount() == 0) {
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

			final long amount = aeStack != null ? aeStack.getStackSize() : is.getCount();
			if (amount != 0) {
				final String stackSize = getToBeRenderedStackSize(amount);

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
			return CONVERTER.toSlimReadableForm(originalSize);
		}
		else {
			return CONVERTER.toWideReadableForm(originalSize);
		}
	}

	public enum ReadableNumberConverter {

			INSTANCE;

		/**
		 * String representation of the sorted suffixes K = thousand M = Million B =
		 * Billion T = Trillion Qa = Quadrillion Qi = Quintillion
		 */
		private static final String[] ENCODED_SUFFIXES = {
				"K",
				"M",
				"B",
				"T",
				"Qa",
				"Qi"
		};

		private final Format format;

		/**
		 * Initializes the specific decimal format with special format for negative
		 * and positive numbers
		 */
		ReadableNumberConverter() {
			final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
			symbols.setDecimalSeparator('.');
			final DecimalFormat format = new DecimalFormat(".#;0.#");
			format.setDecimalFormatSymbols(symbols);
			format.setRoundingMode(RoundingMode.DOWN);

			this.format = format;
		}

		public String toSlimReadableForm(final long number) {
			return toReadableFormRestrictedByWidth(number, 3);
		}

		/**
		 * restricts a string representation of a number to a specific width
		 *
		 * @param number
		 *            to be formatted number
		 * @param width
		 *            width limitation of the resulting number
		 *
		 * @return formatted number restricted by the width limitation
		 */
		private String toReadableFormRestrictedByWidth(final long number, final int width) {
			assert number >= 0;

			// handles low numbers more efficiently since no format is needed
			final String numberString = Long.toString(number);
			int numberSize = numberString.length();
			if (numberSize <= width) {
				return numberString;
			}

			long base = number;
			double last = base * 1000;
			int exponent = -1;
			String postFix = "";

			while (numberSize > width) {
				last = base;
				base /= 1000;

				exponent++;

				// adds +1 due to the postfix
				numberSize = Long.toString(base).length() + 1;
				postFix = ENCODED_SUFFIXES[exponent];
			}

			final String withPrecision = format.format(last / 1000) + postFix;
			final String withoutPrecision = Long.toString(base) + postFix;

			final String slimResult = (withPrecision.length() <= width) ? withPrecision : withoutPrecision;

			// post condition
			assert slimResult.length() <= width;

			return slimResult;
		}

		public String toWideReadableForm(final long number) {
			return toReadableFormRestrictedByWidth(number, 4);
		}

	}

}
