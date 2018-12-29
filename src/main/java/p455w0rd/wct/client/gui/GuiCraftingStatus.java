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

import org.lwjgl.input.Mouse;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.ITerminalHost;
import appeng.core.localization.GuiText;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import p455w0rd.ae2wtlib.api.WTGuiObject;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.client.gui.widgets.GuiTabButton;
import p455w0rd.wct.container.ContainerCraftingStatus;
import p455w0rd.wct.init.*;
import p455w0rd.wct.sync.packets.PacketSwitchGuis;
import p455w0rd.wct.sync.packets.PacketValueConfig;

public class GuiCraftingStatus extends GuiCraftingCPU {

	private final ContainerCraftingStatus status;
	private GuiButton selectCPU;

	private GuiTabButton originalGuiBtn;
	private int originalGui;
	private ItemStack myIcon = null;

	public GuiCraftingStatus(final InventoryPlayer inventoryPlayer, final ITerminalHost te, int wtSlot, boolean isWTBauble) {
		super(new ContainerCraftingStatus(inventoryPlayer, te, wtSlot, isWTBauble));
		status = (ContainerCraftingStatus) inventorySlots;
		final Object target = status.getTarget();
		final IDefinitions definitions = AEApi.instance().definitions();
		if (target instanceof WTGuiObject) {
			myIcon = definitions.items().wirelessTerminal().maybeStack(1).orElse(null);
			ItemStack is = new ItemStack(ModItems.WCT);
			((IWirelessCraftingTerminalItem) is.getItem()).injectAEPower(is, 6400001, Actionable.MODULATE);
			myIcon = is;
			originalGui = ModGuiHandler.GUI_WCT;
		}
	}

	@Override
	protected void actionPerformed(final GuiButton btn) throws IOException {
		super.actionPerformed(btn);

		final boolean backwards = Mouse.isButtonDown(1);

		if (btn == selectCPU) {
			try {
				ModNetworking.instance().sendToServer(new PacketValueConfig("Terminal.Cpu", backwards ? "Prev" : "Next"));
			}
			catch (final IOException e) {
			}
		}

		if (btn == originalGuiBtn) {
			ModNetworking.instance().sendToServer(new PacketSwitchGuis(originalGui));
		}
	}

	@Override
	public void initGui() {
		super.initGui();

		selectCPU = new GuiButton(0, guiLeft + 8, guiTop + ySize - 25, 150, 20, GuiText.CraftingCPU.getLocal() + ": " + GuiText.NoCraftingCPUs);
		// selectCPU.enabled = false;
		buttonList.add(selectCPU);

		if (myIcon != null) {
			buttonList.add(originalGuiBtn = new GuiTabButton(guiLeft + 213, guiTop - 4, myIcon, myIcon.getDisplayName(), itemRender));
			originalGuiBtn.setHideEdge(13);
		}
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float btn) {
		updateCPUButtonText();
		super.drawScreen(mouseX, mouseY, btn);
	}

	private void updateCPUButtonText() {
		String btnTextText = GuiText.NoCraftingJobs.getLocal();

		if (status.selectedCpu >= 0)// && status.selectedCpu < status.cpus.size() )
		{
			if (status.myName.length() > 0) {
				final String name = status.myName.substring(0, Math.min(20, status.myName.length()));
				btnTextText = GuiText.CPUs.getLocal() + ": " + name;
			}
			else {
				btnTextText = GuiText.CPUs.getLocal() + ": #" + status.selectedCpu;
			}
		}

		if (status.noCPU) {
			btnTextText = GuiText.NoCraftingJobs.getLocal();
		}

		selectCPU.displayString = btnTextText;
	}

	@Override
	protected String getGuiDisplayName(final String in) {
		return in; // the cup name is on the button
	}
}
