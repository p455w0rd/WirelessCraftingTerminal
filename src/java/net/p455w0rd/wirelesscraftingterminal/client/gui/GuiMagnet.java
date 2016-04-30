package net.p455w0rd.wirelesscraftingterminal.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerMagnet;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketMagnetFilterMode;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;

public class GuiMagnet extends GuiContainer {

	private GuiButton modeBtn;
	public static boolean mode = true;
	private ItemStack magnetItem;

	public GuiMagnet(ContainerMagnet c) {
		super(c);
		this.xSize = 176;
		this.width = this.xSize;
		this.ySize = 168;
		this.height = this.ySize;
		magnetItem = c.heldItem;
		GuiMagnet.mode = getMode();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		bindTexture("magnetfilter.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 168);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.mc.fontRenderer.drawString(LocaleHandler.MagnetFilterTitle.getLocal(), 7, 5, 4210752);
		this.mc.fontRenderer.drawString(I18n.format("container.inventory"), 7, 74, 4210752);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(this.modeBtn = new GuiButton(1, this.guiLeft + 108, this.guiTop + 3, 60, 10, getLabel(GuiMagnet.mode)));
	}
	
	String getLabel(boolean filterMode) {
		return filterMode ? "Whitelisting" : "Blacklisting";
	}

	@Override
	protected void actionPerformed(final GuiButton btn) {
		if (btn == this.modeBtn) {
			btn.displayString = getLabel(!GuiMagnet.mode);
			GuiMagnet.mode = !GuiMagnet.mode;
			setMode(GuiMagnet.mode);
		}
	}
	
	private synchronized void setMode(boolean mode) {
		final PacketMagnetFilterMode p = new PacketMagnetFilterMode(mode);
		NetworkHandler.instance.sendToServer( p );
	}
	
	private synchronized boolean getMode() {
		NBTTagCompound nbtTC = magnetItem.getTagCompound();
		if (nbtTC.hasKey("Whitelisting")) {
			return nbtTC.getBoolean("Whitelisting");
		}
		return false;
	}
	

	public void bindTexture(final String file) {
		final ResourceLocation loc = new ResourceLocation("ae2wct", "textures/gui/" + file);
		this.mc.getTextureManager().bindTexture(loc);
	}

}
