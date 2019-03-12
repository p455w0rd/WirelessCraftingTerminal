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
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import appeng.api.config.Actionable;
import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import p455w0rd.ae2wtlib.api.client.gui.widgets.GuiMECheckbox;
import p455w0rd.ae2wtlib.api.client.gui.widgets.GuiTabButton;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.client.gui.widgets.GuiBinaryModeButton;
import p455w0rd.wct.container.ContainerMagnet;
import p455w0rd.wct.init.*;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.items.ItemMagnet.MagnetItemMode;
import p455w0rd.wct.sync.packets.*;

public class GuiMagnet extends GuiContainer {

	private GuiBinaryModeButton modeBtn;
	public boolean listMode = true;
	public boolean ignoreNBT = false;
	public boolean ignoreMeta = false;
	public boolean useOreDict = false;
	private ItemStack magnetItem = ItemStack.EMPTY;
	private GuiMECheckbox ignoreNBTBox;
	private GuiMECheckbox ignoreMetaBox;
	private GuiMECheckbox useOreDictBox;
	private GuiTabButton originalGuiBtn;
	private ItemStack myIcon = null;
	private final ContainerMagnet container;

	public GuiMagnet(ContainerMagnet c) {
		super(c);
		container = c;
		xSize = 176;
		width = xSize;
		ySize = 208;
		height = ySize;
		magnetItem = c.magnetItem;
		ItemStack is = new ItemStack(ModItems.WCT);
		((IWirelessCraftingTerminalItem) is.getItem()).injectAEPower(is, 6400001, Actionable.MODULATE);
		myIcon = is;
		loadSettings();
	}

	public ContainerMagnet getContainer() {
		return container;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, final float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);

		for (final Object c : buttonList) {
			if (c instanceof ITooltip) {
				final ITooltip tooltip = (ITooltip) c;
				final int x = tooltip.xPos(); // ((GuiImgButton) c).xPosition;
				int y = tooltip.yPos(); // ((GuiImgButton) c).yPosition;

				if (x < mouseX && x + tooltip.getWidth() > mouseX && tooltip.isVisible()) {
					if (y < mouseY && y + tooltip.getHeight() > mouseY) {
						if (y < 15) {
							y = 15;
						}

						final String msg = tooltip.getMessage();
						if (msg != null) {
							this.drawTooltip(x + 11, y + 4, msg);
						}
					}
				}
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		bindTexture("magnetfilter.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 208);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		mc.fontRenderer.drawString(I18n.format("gui.title.magnet_filter"), 7, 5, 4210752);
		mc.fontRenderer.drawString(I18n.format("container.inventory"), 7, 114, 4210752);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(modeBtn = new GuiBinaryModeButton(0, guiLeft + 104, guiTop + 4, 64, 14, getLabel(listMode)));
		buttonList.add(ignoreNBTBox = new GuiMECheckbox(1, guiLeft + 61, guiTop + 20, "Ignore NBT Data", ignoreNBT, 107));
		buttonList.add(ignoreMetaBox = new GuiMECheckbox(2, guiLeft + 61, guiTop + 32, "Ignore Meta Data", ignoreMeta, 107));
		buttonList.add(useOreDictBox = new GuiMECheckbox(3, guiLeft + 61, guiTop + 44, "Use Ore Dictionary", useOreDict, 107));
		if (myIcon != null && GuiWCT.isSwitchingGuis()) {
			buttonList.add(originalGuiBtn = new GuiTabButton(guiLeft + 1, guiTop + 30, myIcon, myIcon.getDisplayName(), itemRender));
			originalGuiBtn.setHideEdge(13);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == ModKeybindings.openTerminal.getKeyCode()) {
			mc.player.closeScreen();
		}
		else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	String getLabel(boolean filterMode) {
		return filterMode ? "Whitelisting" : "Blacklisting";
	}

	private void loadSettings() {
		if (magnetItem == null) {
			return;
		}
		if (!magnetItem.hasTagCompound()) {
			if (container.isMagnetHeld()) {
				ModNetworking.instance().sendToServer(new PacketMagnetFilterHeld(MagnetItemMode.INIT, true));
			}
			else {
				ModNetworking.instance().sendToServer(new PacketMagnetFilterWCT(MagnetItemMode.INIT, true, container.isWTBauble(), container.getWTSlot()));
			}
		}
		listMode = ItemMagnet.getItemMode(magnetItem, 1);
		ignoreNBT = ItemMagnet.getItemMode(magnetItem, 2);
		ignoreMeta = ItemMagnet.getItemMode(magnetItem, 3);
		useOreDict = ItemMagnet.getItemMode(magnetItem, 4);
	}

	@Override
	protected void actionPerformed(final GuiButton btn) {
		if (btn == originalGuiBtn) {
			ModNetworking.instance().sendToServer(new PacketSwitchGuis(ModGuiHandler.GUI_WCT));
			return;
		}
		MagnetItemMode modeType = MagnetItemMode.INIT;
		boolean newMode = false;
		if (btn == modeBtn) {
			listMode = !listMode;
			modeType = MagnetItemMode.WHITELIST;
			newMode = listMode;
			btn.displayString = getLabel(listMode);
			ItemMagnet.setItemMode(magnetItem, modeType, listMode);
		}
		if (btn == ignoreNBTBox) {
			ignoreNBT = !ignoreNBT;
			modeType = MagnetItemMode.IGNORENBT;
			newMode = ignoreNBT;
			ItemMagnet.setItemMode(magnetItem, modeType, ignoreNBT);
		}
		if (btn == ignoreMetaBox) {
			ignoreMeta = !ignoreMeta;
			modeType = MagnetItemMode.IGNOREMETA;
			newMode = ignoreMeta;
			ItemMagnet.setItemMode(magnetItem, modeType, ignoreMeta);
		}
		if (btn == useOreDictBox) {
			useOreDict = !useOreDict;
			modeType = MagnetItemMode.USEOREDICT;
			newMode = useOreDict;
			ItemMagnet.setItemMode(magnetItem, modeType, useOreDict);
		}
		if (container.isMagnetHeld()) {
			ModNetworking.instance().sendToServer(new PacketMagnetFilterHeld(modeType, newMode));
		}
		else {
			ModNetworking.instance().sendToServer(new PacketMagnetFilterWCT(modeType, newMode, container.isWTBauble(), container.getWTSlot()));
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (getSlotUnderMouse() == null || (getSlotUnderMouse() != null && getSlotUnderMouse().getStack() != null && !(getSlotUnderMouse().getStack().getItem() instanceof IWirelessCraftingTerminalItem))) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		/*
		if (mouseButton == 0) {
			for (int i = 0; i < buttonList.size(); ++i) {
				GuiButton guibutton = buttonList.get(i);

				if (guibutton.mousePressed(mc, mouseX, mouseY)) {
					//net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.buttonList);
					//if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
					//    break;
					//guibutton = event.getButton();
					//this.selectedButton = guibutton;
					guibutton.playPressSound(mc.getSoundHandler());
					actionPerformed(guibutton);
					//if (this.equals(this.mc.currentScreen))
					//    net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), this.buttonList));
				}
			}
		}
		*/
	}

	public void bindTexture(final String file) {
		final ResourceLocation loc = new ResourceLocation(ModGlobals.MODID, "textures/gui/" + file);
		mc.getTextureManager().bindTexture(loc);
	}

	protected void drawTooltip(int x, int y, String message) {
		String[] lines = message.split("\n");
		this.drawTooltip(x, y, Arrays.asList(lines));
	}

	protected void drawTooltip(int x, int y, List<String> lines) {
		if (lines.isEmpty()) {
			return;
		}

		// For an explanation of the formatting codes, see http://minecraft.gamepedia.com/Formatting_codes
		lines = Lists.newArrayList(lines); // Make a copy

		// Make the first line white
		lines.set(0, TextFormatting.WHITE + lines.get(0));

		// All lines after the first are colored gray
		for (int i = 1; i < lines.size(); i++) {
			lines.set(i, TextFormatting.GRAY + lines.get(i));
		}

		this.drawHoveringText(lines, x, y, fontRenderer);
	}

}
