package p455w0rd.wct.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import p455w0rd.wct.Globals;
import p455w0rd.wct.client.gui.widgets.WCTGuiButton;
import p455w0rd.wct.client.gui.widgets.WCTGuiCheckBox;
import p455w0rd.wct.container.ContainerMagnet;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketMagnetFilter;

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
		xSize = 176;
		width = xSize;
		ySize = 208;
		height = ySize;
		magnetItem = c.magnetItem;
		loadSettings();
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
	}

	String getLabel(boolean filterMode) {
		return filterMode ? "Whitelisting" : "Blacklisting";
	}

	private void loadSettings() {
		if (magnetItem == null) {
			return;
		}
		if (!magnetItem.hasTagCompound()) {
			NetworkHandler.instance().sendToServer(new PacketMagnetFilter(0, false));
		}
		mode = getMode(1);
		ignoreNBT = getMode(2);
		ignoreMeta = getMode(3);
		useOreDict = getMode(4);
	}

	@Override
	protected void actionPerformed(final GuiButton btn) {
		if (btn == modeBtn) {
			btn.displayString = getLabel(!mode);
			mode = !mode;
			updateMode(1, mode);
		}
		else if (btn == ignoreNBTBox) {
			ignoreNBT = !ignoreNBT;
			updateMode(2, ignoreNBT);
		}
		else if (btn == ignoreMetaBox) {
			ignoreMeta = !ignoreMeta;
			updateMode(3, ignoreMeta);
		}
		else if (btn == useOreDictBox) {
			useOreDict = !useOreDict;
			updateMode(4, useOreDict);
		}
		else {
			return;
		}
	}

	private void updateMode(int type, boolean mode) {
		NetworkHandler.instance().sendToServer(new PacketMagnetFilter(type, mode));
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
		final ResourceLocation loc = new ResourceLocation(Globals.MODID, "textures/gui/" + file);
		mc.getTextureManager().bindTexture(loc);
	}

}
