package p455w0rd.wct.client.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.ITooltip;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.InventoryAction;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.client.gui.widgets.GuiScrollbar;
import p455w0rd.wct.client.me.InternalSlotME;
import p455w0rd.wct.client.me.SlotDisconnected;
import p455w0rd.wct.client.me.SlotME;
import p455w0rd.wct.client.render.StackSizeRenderer;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.WCTBaseContainer;
import p455w0rd.wct.container.slot.AppEngCraftingSlot;
import p455w0rd.wct.container.slot.AppEngSlot;
import p455w0rd.wct.container.slot.AppEngSlot.hasCalculatedValidness;
import p455w0rd.wct.container.slot.OptionalSlotFake;
import p455w0rd.wct.container.slot.SlotCraftingTerm;
import p455w0rd.wct.container.slot.SlotDisabled;
import p455w0rd.wct.container.slot.SlotFake;
import p455w0rd.wct.container.slot.SlotInaccessible;
import p455w0rd.wct.container.slot.SlotOutput;
import p455w0rd.wct.container.slot.SlotSingleItem;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketInventoryAction;
import p455w0rd.wct.sync.packets.PacketSwapSlots;
import p455w0rd.wct.util.WCTUtils;

public abstract class WCTBaseGui extends GuiContainer {
	protected static boolean switchingGuis;
	public List<InternalSlotME> meSlots = new LinkedList<InternalSlotME>();
	// drag y
	private final Set<Slot> drag_click = new HashSet<Slot>();
	private GuiScrollbar scrollBar = null;
	private boolean disableShiftClick = false;
	private Stopwatch dbl_clickTimer = Stopwatch.createStarted();
	private ItemStack dbl_whichItem;
	private Slot bl_clicked;
	public boolean subGui;
	private final StackSizeRenderer stackSizeRenderer = new StackSizeRenderer();
	public static String memoryText = "";

	public WCTBaseGui(final Container container) {
		super(container);
		subGui = switchingGuis;
		switchingGuis = false;
	}

	protected static String join(final Collection<String> toolTip, final String delimiter) {
		final Joiner joiner = Joiner.on(delimiter);

		return joiner.join(toolTip);
	}

	protected int getQty(final GuiButton btn) {
		try {
			final DecimalFormat df = new DecimalFormat("+#;-#");
			return df.parse(btn.displayString).intValue();
		}
		catch (final ParseException e) {
			return 0;
		}
	}

	public boolean isSubGui() {
		return subGui;
	}

	@Override
	public void initGui() {
		super.initGui();

		final List<Slot> slots = getInventorySlots();
		final Iterator<Slot> i = slots.iterator();
		while (i.hasNext()) {
			if (i.next() instanceof SlotME) {
				i.remove();
			}
		}

		for (final InternalSlotME me : meSlots) {
			slots.add(new SlotME(me));
		}
	}

	private List<Slot> getInventorySlots() {
		return inventorySlots.inventorySlots;
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

	@Override
	protected void drawGuiContainerForegroundLayer(final int x, final int y) {
		final int ox = guiLeft; // (width - xSize) / 2;
		final int oy = guiTop; // (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		if (scrollBar != null) {
			scrollBar.draw(this);
		}

		drawFG(ox, oy, x, y);
	}

	public abstract void drawFG(int offsetX, int offsetY, int mouseX, int mouseY);

	@Override
	protected void drawGuiContainerBackgroundLayer(final float f, final int x, final int y) {
		final int ox = guiLeft; // (width - xSize) / 2;
		final int oy = guiTop; // (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawBG(ox, oy, x, y);

		final List<Slot> slots = getInventorySlots();
		for (final Slot slot : slots) {
			if (slot instanceof OptionalSlotFake) {
				final OptionalSlotFake fs = (OptionalSlotFake) slot;
				if (fs.renderDisabled()) {
					if (fs.isEnabled()) {
						this.drawTexturedModalRect(ox + fs.xPos - 1, oy + fs.yPos - 1, fs.getSourceX() - 1, fs.getSourceY() - 1, 18, 18);
					}
					else {
						GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.4F);
						GL11.glEnable(GL11.GL_BLEND);
						this.drawTexturedModalRect(ox + fs.xPos - 1, oy + fs.yPos - 1, fs.getSourceX() - 1, fs.getSourceY() - 1, 18, 18);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
						GL11.glPopAttrib();
					}
				}
			}
		}
	}

	@Override
	protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
		drag_click.clear();

		if (btn == 1) {
			for (final Object o : buttonList) {
				final GuiButton guibutton = (GuiButton) o;
				if (guibutton.mousePressed(mc, xCoord, yCoord)) {
					super.mouseClicked(xCoord, yCoord, 0);
					return;
				}
			}
		}

		super.mouseClicked(xCoord, yCoord, btn);
	}

	@Override
	protected void mouseClickMove(final int x, final int y, final int c, final long d) {
		final Slot slot = getSlot(x, y);
		final ItemStack itemstack = WCTUtils.player().inventory.getItemStack();

		if (slot instanceof SlotFake && itemstack != null) {
			drag_click.add(slot);
			if (drag_click.size() > 1) {
				for (final Slot dr : drag_click) {
					final PacketInventoryAction p = new PacketInventoryAction(c == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE, dr.slotNumber, 0);
					NetworkHandler.instance().sendToServer(p);
				}
			}
		}
		else {
			super.mouseClickMove(x, y, c, d);
		}
	}

	@Override
	protected void handleMouseClick(final Slot slot, final int slotIdx, final int mouseButton, final ClickType clickType) {
		final EntityPlayer player = WCTUtils.player();

		if (slot instanceof SlotFake) {
			final InventoryAction action = clickType == ClickType.QUICK_CRAFT ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;

			if (drag_click.size() > 1) {
				return;
			}

			final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
			NetworkHandler.instance().sendToServer(p);

			return;
		}
		/*
				if (slot instanceof SlotPatternTerm) {
					if (mouseButton == 6) {
						return; // prevent weird double clicks..
					}
		
					try {
						NetworkHandler.instance().sendToServer(((SlotPatternTerm) slot).getRequest(isShiftKeyDown()));
					}
					catch (final IOException e) {
					}
		
				}
				*/
		else if (slot instanceof SlotCraftingTerm) {
			if (mouseButton == 6) {
				return; // prevent weird double clicks..
			}

			InventoryAction action = null;
			if (isShiftKeyDown()) {
				action = InventoryAction.CRAFT_SHIFT;
			}
			else {
				// Craft stack on right-click, craft single on left-click
				action = (mouseButton == 1) ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
			}

			final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
			NetworkHandler.instance().sendToServer(p);

			return;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			if (enableSpaceClicking()) {
				IAEItemStack stack = null;
				if (slot instanceof SlotME) {
					stack = ((SlotME) slot).getAEStack();
				}

				int slotNum = getInventorySlots().size();

				if (!(slot instanceof SlotME) && slot != null) {
					slotNum = slot.slotNumber;
				}
				if (inventorySlots instanceof ContainerWCT) {
					((ContainerWCT) inventorySlots).setTargetStack(stack);
				}
				else {
					((WCTBaseContainer) inventorySlots).setTargetStack(stack);
				}
				final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, slotNum, 0);
				NetworkHandler.instance().sendToServer(p);
				return;
			}
		}

		if (slot instanceof SlotDisconnected) {
			InventoryAction action = null;

			switch (clickType) {
			case PICKUP: // pickup / set-down.
				action = (mouseButton == 1) ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				break;
			case QUICK_MOVE:
				action = (mouseButton == 1) ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
				break;

			case CLONE: // creative dupe:

				if (player.capabilities.isCreativeMode) {
					action = InventoryAction.CREATIVE_DUPLICATE;
				}

				break;

			default:
			case THROW: // drop item:
			}

			if (action != null) {
				final PacketInventoryAction p = new PacketInventoryAction(action, slot.getSlotIndex(), ((SlotDisconnected) slot).getSlot().getId());
				NetworkHandler.instance().sendToServer(p);
			}

			return;
		}

		if (slot instanceof SlotME) {
			InventoryAction action = null;
			IAEItemStack stack = null;

			switch (clickType) {
			case PICKUP: // pickup / set-down.
				action = (mouseButton == 1) ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				stack = ((SlotME) slot).getAEStack();

				if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && stack.getStackSize() == 0 && player.inventory.getItemStack() == null) {
					action = InventoryAction.AUTO_CRAFT;
				}

				break;
			case QUICK_MOVE:
				action = (mouseButton == 1) ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
				stack = ((SlotME) slot).getAEStack();
				break;

			case CLONE: // creative dupe:

				stack = ((SlotME) slot).getAEStack();
				if (stack != null && stack.isCraftable()) {
					action = InventoryAction.AUTO_CRAFT;
				}
				else if (player.capabilities.isCreativeMode) {
					final IAEItemStack slotItem = ((SlotME) slot).getAEStack();
					if (slotItem != null) {
						action = InventoryAction.CREATIVE_DUPLICATE;
					}
				}
				break;

			default:
			case THROW: // drop item:
			}

			if (action != null) {
				if (inventorySlots instanceof ContainerWCT) {
					((ContainerWCT) inventorySlots).setTargetStack(stack);
				}
				else {
					((AEBaseContainer) inventorySlots).setTargetStack(stack);
				}
				final PacketInventoryAction p = new PacketInventoryAction(action, getInventorySlots().size(), 0);
				NetworkHandler.instance().sendToServer(p);
			}

			return;
		}

		if (!disableShiftClick && isShiftKeyDown()) {
			disableShiftClick = true;

			if (dbl_whichItem == null || bl_clicked != slot || dbl_clickTimer.elapsed(TimeUnit.MILLISECONDS) > 150) {
				// some simple double click logic.
				bl_clicked = slot;
				dbl_clickTimer = Stopwatch.createStarted();
				if (slot != null) {
					dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : null;
				}
				else {
					dbl_whichItem = null;
				}
			}
			else if (dbl_whichItem != null) {
				// a replica of the weird broken vanilla feature.

				final List<Slot> slots = getInventorySlots();
				for (final Slot inventorySlot : slots) {
					if (inventorySlot != null && inventorySlot.canTakeStack(WCTUtils.player()) && inventorySlot.getHasStack() && inventorySlot.inventory == slot.inventory && Container.canAddItemToSlot(inventorySlot, dbl_whichItem, true)) {
						super.handleMouseClick(inventorySlot, inventorySlot.slotNumber, 1, clickType);
					}
				}
			}

			disableShiftClick = false;
		}

		super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
	}

	@Override
	protected boolean checkHotbarKeys(final int keyCode) {
		final Slot theSlot;

		try {
			theSlot = ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, this, "theSlot", "field_147006_u", "f");
		}
		catch (final Throwable t) {
			return false;
		}

		if (WCTUtils.player().inventory.getItemStack() == null && theSlot != null) {
			for (int j = 0; j < 9; ++j) {
				if (keyCode == mc.gameSettings.keyBindsHotbar[j].getKeyCode()) {
					final List<Slot> slots = getInventorySlots();
					InventoryPlayer playerInv = inventorySlots instanceof ContainerWCT ? ((ContainerWCT) inventorySlots).getPlayerInv() : ((WCTBaseContainer) inventorySlots).getPlayerInv();
					for (final Slot s : slots) {
						if (s.getSlotIndex() == j && s.inventory == playerInv) {
							//disable hotbar key-swapping WCT
							if (!s.canTakeStack(WCTUtils.player(playerInv)) || (s.getStack() != null && s.getStack().getItem() instanceof IWirelessCraftingTerminalItem)) {
								return false;
							}
						}
					}

					if (theSlot.getSlotStackLimit() == 64) {
						handleMouseClick(theSlot, theSlot.slotNumber, j, ClickType.SWAP);
						return true;
					}
					else {
						for (final Slot s : slots) {
							if (s.getSlotIndex() == j && s.inventory == playerInv) {
								NetworkHandler.instance().sendToServer(new PacketSwapSlots(s.slotNumber, theSlot.slotNumber));
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		subGui = true; // in case the gui is reopened later ( i'm looking at you NEI )
	}

	protected Slot getSlot(final int mouseX, final int mouseY) {
		final List<Slot> slots = getInventorySlots();
		for (final Slot slot : slots) {
			// isPointInRegion
			if (isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY)) {
				return slot;
			}
		}

		return null;
	}

	public abstract void drawBG(int offsetX, int offsetY, int mouseX, int mouseY);

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		final int i = Mouse.getEventDWheel();
		if (i != 0 && isShiftKeyDown()) {
			final int x = Mouse.getEventX() * width / mc.displayWidth;
			final int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
			mouseWheelEvent(x, y, i / Math.abs(i));
		}
		else if (i != 0 && scrollBar != null) {

			scrollBar.wheel(i);
		}
	}

	private void mouseWheelEvent(final int x, final int y, final int wheel) {
		final Slot slot = getSlot(x, y);
		if (slot instanceof SlotME) {
			final IAEItemStack item = ((SlotME) slot).getAEStack();
			if (item != null) {
				if (inventorySlots instanceof ContainerWCT) {
					((ContainerWCT) inventorySlots).setTargetStack(item);
				}
				else {
					((WCTBaseContainer) inventorySlots).setTargetStack(item);
				}
				final InventoryAction direction = wheel > 0 ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
				final int times = Math.abs(wheel);
				final int inventorySize = getInventorySlots().size();
				for (int h = 0; h < times; h++) {
					final PacketInventoryAction p = new PacketInventoryAction(direction, inventorySize, 0);
					NetworkHandler.instance().sendToServer(p);
				}
			}
		}
	}

	protected boolean enableSpaceClicking() {
		return true;
	}

	public void bindTexture(final String base, final String file) {
		final ResourceLocation loc = new ResourceLocation(base, "textures/" + file);
		mc.getTextureManager().bindTexture(loc);
	}

	protected void drawItem(final int x, final int y, final ItemStack is) {
		zLevel = 100.0F;
		itemRender.zLevel = 100.0F;

		//GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glEnable(GL11.GL_LIGHTING);
		//GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.renderItemAndEffectIntoGUI(is, x, y);
		//GL11.glPopAttrib();

		itemRender.zLevel = 0.0F;
		zLevel = 0.0F;
	}

	protected String getGuiDisplayName(final String in) {
		return hasCustomInventoryName() ? getInventoryName() : in;
	}

	private boolean hasCustomInventoryName() {
		if (inventorySlots instanceof WCTBaseContainer) {
			return ((WCTBaseContainer) inventorySlots).getCustomName() != null;
		}
		return false;
	}

	private String getInventoryName() {
		return ((WCTBaseContainer) inventorySlots).getCustomName();
	}

	@Override
	public void drawSlot(final Slot s) {
		if (s instanceof SlotME) {
			try {
				zLevel = 100.0F;
				itemRender.zLevel = 100.0F;

				if (!isPowered()) {
					GL11.glDisable(GL11.GL_LIGHTING);
					drawRect(s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111);
					GL11.glEnable(GL11.GL_LIGHTING);
				}

				zLevel = 0.0F;
				itemRender.zLevel = 0.0F;

				super.drawSlot(new SlotSingleItem(s));
				//super.drawSlot(s);

				stackSizeRenderer.renderStackSize(fontRenderer, ((SlotME) s).getAEStack(), s.getStack(), s.xPos, s.yPos);
			}
			catch (final Exception err) {
			}
			return;
		}
		else {
			try {
				final ItemStack is = s.getStack();
				if (s instanceof AppEngSlot && (((AppEngSlot) s).renderIconWithItem() || is == null) && (((AppEngSlot) s).shouldDisplay())) {
					final AppEngSlot aes = (AppEngSlot) s;
					if (aes.getIcon() >= 0) {
						this.bindTexture("guis/states.png");

						GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
						final Tessellator tessellator = Tessellator.getInstance();
						final BufferBuilder vb = tessellator.getBuffer();
						try {
							final int uv_y = (int) Math.floor(aes.getIcon() / 16);
							final int uv_x = aes.getIcon() - uv_y * 16;

							GlStateManager.enableBlend();
							GlStateManager.disableLighting();
							GlStateManager.enableTexture2D();
							GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
							GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
							final float par1 = aes.xPos;
							final float par2 = aes.yPos;
							final float par3 = uv_x * 16;
							final float par4 = uv_y * 16;

							vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

							final float f1 = 0.00390625F;
							final float f = 0.00390625F;
							final float par6 = 16;
							vb.pos(par1 + 0, par2 + par6, zLevel).tex((par3 + 0) * f, (par4 + par6) * f1).color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon()).endVertex();
							final float par5 = 16;
							vb.pos(par1 + par5, par2 + par6, zLevel).tex((par3 + par5) * f, (par4 + par6) * f1).color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon()).endVertex();
							vb.pos(par1 + par5, par2 + 0, zLevel).tex((par3 + par5) * f, (par4 + 0) * f1).color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon()).endVertex();
							vb.pos(par1 + 0, par2 + 0, zLevel).tex((par3 + 0) * f, (par4 + 0) * f1).color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon()).endVertex();
							tessellator.draw();
						}
						catch (final Exception err) {
						}
						GL11.glPopAttrib();
					}
				}

				if (is != null && s instanceof AppEngSlot) {
					if (((AppEngSlot) s).getIsValid() == hasCalculatedValidness.NotAvailable) {
						boolean isValid = s.isItemValid(is) || s instanceof SlotOutput || s instanceof AppEngCraftingSlot || s instanceof SlotDisabled || s instanceof SlotInaccessible || s instanceof SlotFake || s instanceof SlotRestrictedInput || s instanceof SlotDisconnected;
						if (isValid && s instanceof SlotRestrictedInput) {
							try {
								isValid = ((SlotRestrictedInput) s).isValid(is, WCTUtils.world());
							}
							catch (final Exception err) {
							}
						}
						((AppEngSlot) s).setIsValid(isValid ? hasCalculatedValidness.Valid : hasCalculatedValidness.Invalid);
					}

					if (((AppEngSlot) s).getIsValid() == hasCalculatedValidness.Invalid) {
						zLevel = 100.0F;
						itemRender.zLevel = 100.0F;

						GL11.glDisable(GL11.GL_LIGHTING);
						drawRect(s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66ff6666);
						GL11.glEnable(GL11.GL_LIGHTING);

						zLevel = 0.0F;
						itemRender.zLevel = 0.0F;
					}
				}

				if (s instanceof AppEngSlot) {
					((AppEngSlot) s).setDisplay(true);
					super.drawSlot(s);
				}
				else {
					super.drawSlot(s);
				}

				return;
			}
			catch (final Exception err) {
			}
		}
		// do the usual for non-ME Slots.
		super.drawSlot(s);
	}

	protected boolean isPowered() {
		return true;
	}

	public void bindTexture(final String file) {
		final ResourceLocation loc = new ResourceLocation("appliedenergistics2", "textures/" + file);
		mc.getTextureManager().bindTexture(loc);
	}

	protected GuiScrollbar getScrollBar() {
		return scrollBar;
	}

	protected void setScrollBar(final GuiScrollbar myScrollBar) {
		scrollBar = myScrollBar;
	}

	protected List<InternalSlotME> getMeSlots() {
		return meSlots;
	}

	public static final synchronized boolean isSwitchingGuis() {
		return switchingGuis;
	}

	public static final synchronized void setSwitchingGuis(final boolean switchingGuisIn) {
		switchingGuis = switchingGuisIn;
	}
}
