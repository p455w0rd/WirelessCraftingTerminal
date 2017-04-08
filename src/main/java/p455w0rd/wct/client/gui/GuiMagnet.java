package p455w0rd.wct.client.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.client.gui.widgets.GuiTabButton;
import p455w0rd.wct.client.gui.widgets.WCTGuiButton;
import p455w0rd.wct.client.gui.widgets.WCTGuiCheckBox;
import p455w0rd.wct.container.ContainerMagnet;
import p455w0rd.wct.handlers.GuiHandler;
import p455w0rd.wct.init.ModGlobals;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModKeybindings;
import p455w0rd.wct.items.ItemWCT;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketMagnetFilter;
import p455w0rd.wct.sync.packets.PacketSwitchGuis;
import p455w0rd.wct.util.WCTUtils;

public class GuiMagnet extends GuiContainer {

	private WCTGuiButton modeBtn;
	public boolean mode = true;
	public boolean ignoreNBT = false;
	public boolean ignoreMeta = false;
	public boolean useOreDict = false;
	private ItemStack magnetItem;
	private WCTGuiCheckBox ignoreNBTBox;
	private WCTGuiCheckBox ignoreMetaBox;
	private WCTGuiCheckBox useOreDictBox;
	private GuiTabButton originalGuiBtn;
	private ItemStack myIcon = null;

	public GuiMagnet(ContainerMagnet c, ItemStack magnetStack) {
		super(c);
		xSize = 176;
		width = xSize;
		ySize = 208;
		height = ySize;
		magnetItem = magnetStack;
		ItemStack is = new ItemStack(ModItems.WCT);
		((ItemWCT) is.getItem()).injectAEPower(is, 6400001);
		myIcon = is;
		loadSettings();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, final float partialTicks) {
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
		mc.fontRendererObj.drawString(I18n.format("gui.title.magnet_filter"), 7, 5, 4210752);
		mc.fontRendererObj.drawString(I18n.format("container.inventory"), 7, 114, 4210752);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(modeBtn = new WCTGuiButton(0, guiLeft + 104, guiTop + 4, 64, 14, getLabel(mode)));
		buttonList.add(ignoreNBTBox = new WCTGuiCheckBox(1, guiLeft + 61, guiTop + 20, "Ignore NBT Data", ignoreNBT, 107));
		buttonList.add(ignoreMetaBox = new WCTGuiCheckBox(2, guiLeft + 61, guiTop + 32, "Ignore Meta Data", ignoreMeta, 107));
		buttonList.add(useOreDictBox = new WCTGuiCheckBox(3, guiLeft + 61, guiTop + 44, "Use Ore Dictionary", useOreDict, 107));
		if (myIcon != null && GuiWCT.isSwitchingGuis()) {
			buttonList.add(originalGuiBtn = new GuiTabButton(guiLeft + 1, guiTop + 30, myIcon, myIcon.getDisplayName(), itemRender));
			originalGuiBtn.setHideEdge(13);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == ModKeybindings.openMagnetFilter.getKeyCode()) {
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
			NetworkHandler.instance().sendToServer(new PacketMagnetFilter(0, true));
		}
		mode = getMode(1);
		ignoreNBT = getMode(2);
		ignoreMeta = getMode(3);
		useOreDict = getMode(4);
	}

	@Override
	protected void actionPerformed(final GuiButton btn) {
		//updateMode(0, true);
		if (btn == modeBtn) {
			btn.displayString = getLabel(!mode);
			mode = !mode;
			updateMode(1, mode);
		}
		if (btn == ignoreNBTBox) {
			ignoreNBT = !ignoreNBT;
			updateMode(2, ignoreNBT);
		}
		if (btn == ignoreMetaBox) {
			ignoreMeta = !ignoreMeta;
			updateMode(3, ignoreMeta);
		}
		if (btn == useOreDictBox) {
			useOreDict = !useOreDict;
			updateMode(4, useOreDict);
		}
		if (btn == originalGuiBtn) {
			NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiHandler.GUI_WCT));
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

	private void updateMode(int type, boolean mode) {
		if (magnetItem != null) {
			if (!magnetItem.hasTagCompound()) {
				magnetItem.setTagCompound(new NBTTagCompound());
			}
			if (type == 0) {
				magnetItem.getTagCompound().setBoolean("Initialized", mode);
			}
			else if (type == 1) {
				magnetItem.getTagCompound().setBoolean("Whitelisting", mode);
			}
			else if (type == 2) {
				magnetItem.getTagCompound().setBoolean("IgnoreNBT", mode);
			}
			else if (type == 3) {
				magnetItem.getTagCompound().setBoolean("IgnoreMeta", mode);
			}
			else if (type == 4) {
				magnetItem.getTagCompound().setBoolean("UseOreDict", mode);
			}
			else {
				return;
			}

			if (WCTUtils.isMagnetInstalled(((ContainerMagnet) inventorySlots).inventoryPlayer)) {
				ItemStack wct = WCTUtils.getWirelessTerm(((ContainerMagnet) inventorySlots).inventoryPlayer);
				NBTTagCompound newNBT = wct.getTagCompound();
				NBTTagList magnetNBTForm = wct.getTagCompound().getTagList("MagnetSlot", 10);
				if (magnetNBTForm.getCompoundTagAt(0) != null) {
					magnetNBTForm.set(0, magnetItem.serializeNBT());
				}
				newNBT.setTag("MagnetSlot", magnetNBTForm);
			}
		}
		NetworkHandler.instance().sendToServer(new PacketMagnetFilter(type, mode, magnetItem));
	}

	private boolean getMode(int type) {
		if (!magnetItem.hasTagCompound()) {
			magnetItem.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbtTC = magnetItem.getTagCompound();
		if (type == 1) {
			if (nbtTC.hasKey("Whitelisting")) {
				return nbtTC.getBoolean("Whitelisting");
			}
			else {
				return true;
			}
		}
		else if (type == 2) {
			if (nbtTC.hasKey("IgnoreNBT")) {
				return nbtTC.getBoolean("IgnoreNBT");
			}
			else {
				return false;
			}
		}
		else if (type == 3) {
			if (nbtTC.hasKey("IgnoreMeta")) {
				return nbtTC.getBoolean("IgnoreMeta");
			}
			else {
				return false;
			}
		}
		else if (type == 4) {
			if (nbtTC.hasKey("UseOreDict")) {
				return nbtTC.getBoolean("UseOreDict");
			}
			else {
				return false;
			}
		}
		else {
			return true;
		}
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

		this.drawHoveringText(lines, x, y, fontRendererObj);
	}

}
