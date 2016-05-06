package net.p455w0rd.wirelesscraftingterminal.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.WCTGuiButton;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.WCTGuiCheckBox;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerMagnet;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketMagnetFilter;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;

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

	public GuiMagnet(ContainerMagnet c) {
		super(c);
		this.xSize = 176;
		this.width = this.xSize;
		this.ySize = 208;
		this.height = this.ySize;
		this.magnetItem = c.magnetItem;
		this.loadSettings();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		bindTexture("magnetfilter.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 208);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.mc.fontRenderer.drawString(LocaleHandler.MagnetFilterTitle.getLocal(), 7, 5, 4210752);
		this.mc.fontRenderer.drawString(I18n.format("container.inventory"), 7, 114, 4210752);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(this.modeBtn = new WCTGuiButton(0, this.guiLeft + 104, this.guiTop + 4, 64, 14, getLabel(this.mode)));
		this.buttonList.add(this.ignoreNBTBox = new WCTGuiCheckBox(1, this.guiLeft + 61, this.guiTop + 20, "Ignore NBT Data", this.ignoreNBT, 107));
		this.buttonList.add(this.ignoreMetaBox = new WCTGuiCheckBox(2, this.guiLeft + 61, this.guiTop + 32, "Ignore Meta Data", this.ignoreMeta, 107));
		this.buttonList.add(this.useOreDictBox = new WCTGuiCheckBox(3, this.guiLeft + 61, this.guiTop + 44, "Use Ore Dictionary", this.useOreDict, 107));
	}

	String getLabel(boolean filterMode) {
		return filterMode ? "Whitelisting" : "Blacklisting";
	}

	private void loadSettings() {
		if (magnetItem == null) {
			return;
		}
		if (!magnetItem.hasTagCompound()) {
			NetworkHandler.instance.sendToServer(new PacketMagnetFilter(0, false));
		}
		this.mode = getMode(1);
		this.ignoreNBT = getMode(2);
		this.ignoreMeta = getMode(3);
		this.useOreDict = getMode(4);
	}

	@Override
	protected void actionPerformed(final GuiButton btn) {
		if (btn == this.modeBtn) {
			btn.displayString = getLabel(!this.mode);
			this.mode = !this.mode;
			updateMode(1, this.mode);
		}
		else if (btn == this.ignoreNBTBox) {
			this.ignoreNBT = !this.ignoreNBT;
			updateMode(2, this.ignoreNBT);
		}
		else if (btn == this.ignoreMetaBox) {
			this.ignoreMeta = !this.ignoreMeta;
			updateMode(3, this.ignoreMeta);
		}
		else if (btn == this.useOreDictBox) {
			this.useOreDict = !this.useOreDict;
			updateMode(4, this.useOreDict);
		}
		else {
			return;
		}
	}

	private void updateMode(int type, boolean mode) {
		NetworkHandler.instance.sendToServer(new PacketMagnetFilter(type, mode));
	}

	private boolean getMode(int type) {
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
		final ResourceLocation loc = new ResourceLocation("ae2wct", "textures/gui/" + file);
		this.mc.getTextureManager().bindTexture(loc);
	}

}
