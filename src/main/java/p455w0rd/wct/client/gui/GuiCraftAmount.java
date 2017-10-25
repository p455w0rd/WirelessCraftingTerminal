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
package p455w0rd.wct.client.gui;

import java.io.IOException;

import appeng.api.config.Actionable;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.helpers.Reflected;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.client.gui.widgets.GuiTabButton;
import p455w0rd.wct.container.ContainerCraftAmount;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.items.ItemWCT;
import p455w0rd.wct.sync.packets.PacketCraftRequest;
import p455w0rd.wct.sync.packets.PacketSwitchGuis;

public class GuiCraftAmount extends WCTBaseGui {

	private GuiNumberBox amountToCraft;
	private GuiTabButton originalGuiBtn;

	private GuiButton next;

	private GuiButton plus1;
	private GuiButton plus10;
	private GuiButton plus100;
	private GuiButton plus1000;
	private GuiButton minus1;
	private GuiButton minus10;
	private GuiButton minus100;
	private GuiButton minus1000;

	private ItemStack myIcon;

	private int originalGui;

	@Reflected
	public GuiCraftAmount(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
		super(new ContainerCraftAmount(inventoryPlayer, te));
		ItemStack is = new ItemStack(ModItems.WCT);
		((ItemWCT) is.getItem()).injectAEPower(is, 6400001, Actionable.MODULATE);
		myIcon = is;
	}

	@Override
	public void initGui() {
		super.initGui();

		final int a = AEConfig.instance().craftItemsByStackAmounts(0);
		final int b = AEConfig.instance().craftItemsByStackAmounts(1);
		final int c = AEConfig.instance().craftItemsByStackAmounts(2);
		final int d = AEConfig.instance().craftItemsByStackAmounts(3);

		buttonList.add(plus1 = new GuiButton(0, guiLeft + 20, guiTop + 26, 22, 20, "+" + a));
		buttonList.add(plus10 = new GuiButton(0, guiLeft + 48, guiTop + 26, 28, 20, "+" + b));
		buttonList.add(plus100 = new GuiButton(0, guiLeft + 82, guiTop + 26, 32, 20, "+" + c));
		buttonList.add(plus1000 = new GuiButton(0, guiLeft + 120, guiTop + 26, 38, 20, "+" + d));

		buttonList.add(minus1 = new GuiButton(0, guiLeft + 20, guiTop + 75, 22, 20, "-" + a));
		buttonList.add(minus10 = new GuiButton(0, guiLeft + 48, guiTop + 75, 28, 20, "-" + b));
		buttonList.add(minus100 = new GuiButton(0, guiLeft + 82, guiTop + 75, 32, 20, "-" + c));
		buttonList.add(minus1000 = new GuiButton(0, guiLeft + 120, guiTop + 75, 38, 20, "-" + d));

		buttonList.add(next = new GuiButton(0, guiLeft + 128, guiTop + 51, 38, 20, GuiText.Next.getLocal()));
		final Object target = ((ContainerCraftAmount) inventorySlots).getTarget();

		if (target instanceof WCTGuiObject) {
			originalGui = ModGuiHandler.GUI_WCT;
		}

		if (originalGui == 0 && myIcon != null) {
			buttonList.add(originalGuiBtn = new GuiTabButton(guiLeft + 154, guiTop - 4, myIcon, myIcon.getDisplayName(), itemRender));
		}

		amountToCraft = new GuiNumberBox(fontRenderer, guiLeft + 62, guiTop + 57, 59, fontRenderer.FONT_HEIGHT, Integer.class);
		amountToCraft.setEnableBackgroundDrawing(false);
		amountToCraft.setMaxStringLength(16);
		amountToCraft.setTextColor(0xFFFFFF);
		amountToCraft.setVisible(true);
		amountToCraft.setFocused(true);
		amountToCraft.setText("1");
	}

	@Override
	public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		fontRenderer.drawString(GuiText.SelectAmount.getLocal(), 8, 6, 4210752);
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		next.displayString = isShiftKeyDown() ? GuiText.Start.getLocal() : GuiText.Next.getLocal();

		this.bindTexture("appliedenergistics2", "guis/craft_amt.png");
		this.drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, ySize);

		try {
			Long.parseLong(amountToCraft.getText());
			next.enabled = !amountToCraft.getText().isEmpty();
		}
		catch (final NumberFormatException e) {
			next.enabled = false;
		}

		amountToCraft.drawTextBox();
	}

	@Override
	protected void keyTyped(final char character, final int key) throws IOException {
		if (!checkHotbarKeys(key)) {
			if (key == 28) {
				actionPerformed(next);
			}
			if ((key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit(character)) && amountToCraft.textboxKeyTyped(character, key)) {
				try {
					String out = amountToCraft.getText();

					boolean fixed = false;
					while (out.startsWith("0") && out.length() > 1) {
						out = out.substring(1);
						fixed = true;
					}

					if (fixed) {
						amountToCraft.setText(out);
					}

					if (out.isEmpty()) {
						out = "0";
					}

					final long result = Long.parseLong(out);
					if (result < 0) {
						amountToCraft.setText("1");
					}
				}
				catch (final NumberFormatException e) {
					// :P
				}
			}
			else {
				super.keyTyped(character, key);
			}
		}
	}

	@Override
	protected void actionPerformed(final GuiButton btn) throws IOException {
		super.actionPerformed(btn);

		try {

			if (btn == originalGuiBtn) {
				ModNetworking.instance().sendToServer(new PacketSwitchGuis(originalGui));
			}

			if (btn == next) {
				ModNetworking.instance().sendToServer(new PacketCraftRequest(Integer.parseInt(amountToCraft.getText()), isShiftKeyDown()));
			}
		}
		catch (final NumberFormatException e) {
			// nope..
			amountToCraft.setText("1");
		}

		final boolean isPlus = btn == plus1 || btn == plus10 || btn == plus100 || btn == plus1000;
		final boolean isMinus = btn == minus1 || btn == minus10 || btn == minus100 || btn == minus1000;

		if (isPlus || isMinus) {
			addQty(getQty(btn));
		}
	}

	private void addQty(final int i) {
		try {
			String out = amountToCraft.getText();

			boolean fixed = false;
			while (out.startsWith("0") && out.length() > 1) {
				out = out.substring(1);
				fixed = true;
			}

			if (fixed) {
				amountToCraft.setText(out);
			}

			if (out.isEmpty()) {
				out = "0";
			}

			long result = Integer.parseInt(out);

			if (result == 1 && i > 1) {
				result = 0;
			}

			result += i;
			if (result < 1) {
				result = 1;
			}

			out = Long.toString(result);
			Integer.parseInt(out);
			amountToCraft.setText(out);
		}
		catch (final NumberFormatException e) {
			// :P
		}
	}

	protected String getBackground() {
		return "guis/craftAmt.png";
	}
}
