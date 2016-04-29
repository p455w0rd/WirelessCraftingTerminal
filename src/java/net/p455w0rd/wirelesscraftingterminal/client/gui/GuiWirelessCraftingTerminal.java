package net.p455w0rd.wirelesscraftingterminal.client.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;

import appeng.api.config.ActionItems;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.ITooltip;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.GuiScrollbar;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.GuiTabButton;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.GuiTrashButton;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.MEGuiTextField;
import net.p455w0rd.wirelesscraftingterminal.client.me.InternalSlotME;
import net.p455w0rd.wirelesscraftingterminal.client.me.ItemRepo;
import net.p455w0rd.wirelesscraftingterminal.client.me.SlotDisconnected;
import net.p455w0rd.wirelesscraftingterminal.client.me.SlotME;
import net.p455w0rd.wirelesscraftingterminal.client.me.WCTRenderItem;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.AppEngCraftingSlot;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.AppEngSlot;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.AppEngSlot.hasCalculatedValidness;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.OptionalSlotFake;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotCraftingMatrix;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotCraftingTerm;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotDisabled;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotFake;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotInaccessible;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotOutput;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotTrash;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryBooster;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketEmptyTrash;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketInventoryAction;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwapSlots;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketValueConfig;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationRegistry;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationType;
import net.p455w0rd.wirelesscraftingterminal.integration.abstraction.INEI;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class GuiWirelessCraftingTerminal extends GuiContainer implements ISortSource, IConfigManagerHost {

	private float xSize_lo;
	private float ySize_lo;
	public static int tick = 0, GUI_HEIGHT = 240, GUI_WIDTH = 198, AE_ROW_HEIGHT = 18, AE_NUM_ROWS = 3,
			GUI_UPPER_HEIGHT = 35, GUI_SEARCH_ROW = 35, SEARCH_X = 81, SEARCH_Y = 6, SEARCH_WIDTH = 88,
			SEARCH_HEIGHT = 10, SEARCH_MAXCHARS = 15, GUI_LOWER_HEIGHT, AE_TOTAL_ROWS_HEIGHT,
			BUTTON_SEARCH_MODE_POS_X = -18, BUTTON_SEARCH_MODE_POS_Y = 68, BUTTON_SIZE = 16, NEI_EXTRA_SPACE = 30,
			slotYOffset;
	protected static final int BUTTON_SEARCH_MODE_ID = 5;
	protected static final long TOOLTIP_UPDATE_INTERVAL = 3000L;
	private int currScreenWidth, currScreenHeight, previousMouseX = 0, previousMouseY = 0;
	private static final String bgTexturePath = "gui/crafting.png";
	private final WCTInventoryBooster inventory;
	private final ContainerWirelessCraftingTerminal containerWCT;
	private boolean isFullScreen, init = true, reInit, wasResized = false;
	public static GuiWirelessCraftingTerminal INSTANCE;
	private static boolean switchingGuis;
	private final List<InternalSlotME> meSlots = new LinkedList<InternalSlotME>();
	private final Set<Slot> drag_click = new HashSet<Slot>();
	private final WCTRenderItem aeRenderItem = new WCTRenderItem();
	private GuiScrollbar scrollBar = null;
	private boolean disableShiftClick = false;
	private Stopwatch dbl_clickTimer = Stopwatch.createStarted();
	private ItemStack dbl_whichItem;
	private Slot bl_clicked;
	private boolean subGui;
	public static int craftingGridOffsetX = 80;
	public static int craftingGridOffsetY;
	private static String memoryText = "";
	private static String memoryText2 = "";
	private final ItemRepo repo;
	private final int offsetX = 8;
	private final IConfigManager configSrc;
	private final ItemStack[] myCurrentViewCells = new ItemStack[5];
	private GuiTabButton craftingStatusBtn;
	private MEGuiTextField searchField;
	private int perRow = 9;
	private boolean customSortOrder = true;
	private int maxRows = Integer.MAX_VALUE;
	private GuiImgButton ViewBox;
	private GuiImgButton SortByBox;
	private GuiImgButton SortDirBox;
	private GuiImgButton searchBoxSettings;
	private GuiImgButton terminalStyleBox;
	private GuiImgButton clearBtn;
	private GuiTrashButton trashBtn;
	public boolean devicePowered = false;
	private boolean isNEIEnabled;
	private boolean wasTextboxFocused = false;
	private int screenResTicks = 0;

	public GuiWirelessCraftingTerminal(Container container) {
		super(container);
		this.INSTANCE = this;
		this.containerWCT = (ContainerWirelessCraftingTerminal) container;
		this.inventory = containerWCT.boosterInventory;
		this.scrollBar = new GuiScrollbar();
		this.subGui = switchingGuis;
		switchingGuis = false;
		this.setScrollBar(scrollBar);
		this.repo = new ItemRepo(scrollBar, this);
		this.configSrc = ((IConfigurableObject) this.inventorySlots).getConfigManager();
		this.devicePowered = containerWCT.isPowered();
		((ContainerWirelessCraftingTerminal) this.inventorySlots).setGui(this);
		this.isNEIEnabled = IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI);
	}

	public void postUpdate(final List<IAEItemStack> list) {
		for (final IAEItemStack is : list) {
			this.repo.postUpdate(is);
		}

		this.repo.updateView();
		this.setScrollBar();
	}

	private void setScrollBar() {
		this.getScrollBar().setTop(18).setLeft(174).setHeight((AE_NUM_ROWS + 3) * 18 - 2);
		this.getScrollBar().setRange(0, (this.repo.size() + this.perRow - 1) / this.perRow - (AE_NUM_ROWS + 3), Math.max(1, (AE_NUM_ROWS + 3) / 6));
	}

	@Override
	protected void actionPerformed(final GuiButton btn) {
		if (btn == this.craftingStatusBtn) {
			NetworkHandler.instance.sendToServer(new PacketSwitchGuis(Reference.GUI_CRAFTING_STATUS));
		}

		if (btn instanceof GuiImgButton) {
			final boolean backwards = Mouse.isButtonDown(1);

			final GuiImgButton iBtn = (GuiImgButton) btn;
			if (iBtn.getSetting() != Settings.ACTIONS) {
				final Enum cv = iBtn.getCurrentValue();
				final Enum next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());

				if (btn == this.searchBoxSettings) {
					AEConfig.instance.settings.putSetting(iBtn.getSetting(), next);
				}
				else {
					try {
						NetworkHandler.instance.sendToServer(new PacketValueConfig(iBtn.getSetting().name(), next.name()));
					}
					catch (final IOException e) {
						WCTLog.debug(e.getMessage());
					}
				}

				iBtn.set(next);

				if (next.getClass() == SearchBoxMode.class) {
					this.reinitalize();
				}
			}
			if (this.clearBtn == btn) {
				Slot s = null;
				final Container c = this.inventorySlots;
				for (final Object j : c.inventorySlots) {
					if (j instanceof SlotCraftingMatrix) {
						s = (Slot) j;
					}
				}

				if (s != null) {
					final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, s.slotNumber, 0);
					NetworkHandler.instance.sendToServer(p);
				}
			}
		}
		if (btn instanceof GuiTrashButton) {
			if (this.trashBtn == btn) {
				Slot s = null;
				final Container c = this.inventorySlots;
				for (final Object j : c.inventorySlots) {
					if (j instanceof SlotTrash) {
						s = (Slot) j;
					}
				}

				if (s != null) {
					if (s.getHasStack()) {
						containerWCT.trashSlot.clearStack();
						final PacketEmptyTrash p = new PacketEmptyTrash();
						NetworkHandler.instance.sendToServer(p);
					}
				}
			}
		}
	}

	private void reinitalize() {
		this.buttonList.clear();
		this.initGui();
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
		return this.subGui;
	}

	/**
	 * Initializes the GUI
	 */
	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.getMeSlots().clear();
		for (int y = 0; y < AE_NUM_ROWS + 3; y++) {
			for (int x = 0; x < this.perRow; x++) {
				this.getMeSlots().add(new InternalSlotME(this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18));
			}
		}
		super.initGui();
		final List<Slot> slots = this.getInventorySlots();
		final Iterator<Slot> i = slots.iterator();
		while (i.hasNext()) {
			if (i.next() instanceof SlotME) {
				i.remove();
			}
		}

		for (final InternalSlotME me : this.meSlots) {
			slots.add(new SlotME(me));
		}
		int offset = this.guiTop + 8;

		this.buttonList.clear();
		this.buttonList.add(this.clearBtn = new GuiImgButton(this.guiLeft + 134, this.GUI_HEIGHT - this.GUI_LOWER_HEIGHT + 46, Settings.ACTIONS, ActionItems.STASH));
		this.buttonList.add(this.trashBtn = new GuiTrashButton(this.guiLeft + 98, this.GUI_HEIGHT - this.GUI_LOWER_HEIGHT + 103));
		this.clearBtn.setHalfSize(true);
		if (this.customSortOrder) {
			this.buttonList.add(this.SortByBox = new GuiImgButton(this.guiLeft - 18, offset, Settings.SORT_BY, this.configSrc.getSetting(Settings.SORT_BY)));
			offset += 20;
		}

		this.buttonList.add(this.ViewBox = new GuiImgButton(this.guiLeft - 18, offset, Settings.VIEW_MODE, this.configSrc.getSetting(Settings.VIEW_MODE)));
		offset += 20;

		this.buttonList.add(this.SortDirBox = new GuiImgButton(this.guiLeft - 18, offset, Settings.SORT_DIRECTION, this.configSrc.getSetting(Settings.SORT_DIRECTION)));
		offset += 20;

		this.buttonList.add(this.searchBoxSettings = new GuiImgButton(this.guiLeft - 18, offset, Settings.SEARCH_MODE, AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE)));
		offset += 20;

		this.searchField = new MEGuiTextField(this.fontRendererObj, SEARCH_X, SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT);

		this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setMaxStringLength(25);
		this.searchField.setTextColor(0xFFFFFF);
		this.searchField.setVisible(true);
		this.searchField.setEnabled(true);

		this.buttonList.add(this.craftingStatusBtn = new GuiTabButton(this.guiLeft + 169, this.guiTop - 4, 2 + 11 * 16, GuiText.CraftingStatus.getLocal(), itemRender));
		this.craftingStatusBtn.setHideEdge(13);

		final Enum setting = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
		this.searchField.setFocused(SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.NEI_AUTOSEARCH == setting);

		if (this.isSubGui() || this.init) {
			if (this.isSubGui()) {
				this.searchField.setText(memoryText);
				this.repo.setSearchString(memoryText);
			}
			this.repo.updateView();
			this.setScrollBar();
		}
	}

	@SuppressWarnings("unchecked")
	private List<Slot> getInventorySlots() {
		return this.inventorySlots.inventorySlots;
	}

	/**
	 * Called every tick. It is used in this instance to detect screen size
	 * changes to automatically update the gui height.
	 * 
	 * reInit variable is used to call initGui() on the next tick. It has to be
	 * called this way to update correctly.
	 * 
	 * @author p455w0rd
	 */
	@Override
	public void updateScreen() {
		this.devicePowered = containerWCT.isPowered();
		this.repo.setPower(devicePowered);
		//this.repo.setPower( true );

		super.updateScreen();
		if (this.init) {
			this.currScreenWidth = this.mc.displayWidth;
			this.currScreenHeight = this.mc.displayHeight;
			this.isFullScreen = this.mc.isFullScreen();
			this.reInit = true;
			this.init = false;
		}
		++screenResTicks;
		if (screenResTicks == 20) {
			this.wasTextboxFocused = this.searchField.isFocused();
			screenResTicks = 0;
		}
		if (this.reInit) {
			if (tick != 1) {
				tick++;
			}
			else {
				this.initGui();
				this.setScrollBar();
				if (this.wasResized == true) {
					this.searchField.setFocused(this.wasTextboxFocused);
					this.searchField.setText(this.repo.getSearchString());
					this.wasTextboxFocused = false;
					this.wasResized = false;
				}
				containerWCT.updateSlots(slotYOffset);
				this.reInit = false;
				tick = 0;
			}
		}
		if (hasScreenResChanged()) {
			this.reInit = true;
			this.wasResized = true;
		}
		if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead) {
			this.mc.thePlayer.closeScreen();
		}
		else {
			// this block of variables is used to automatically
			// reposition the slots in the container
			GUI_HEIGHT = this.mc.displayHeight / 2;
			AE_NUM_ROWS = Math.max(0, ((GUI_HEIGHT) - 240) / AE_ROW_HEIGHT);
			if (isNEIEnabled && AE_NUM_ROWS >= 2) {
				AE_NUM_ROWS -= 2;
				GUI_HEIGHT -= AE_ROW_HEIGHT * 2 + 5;
				this.clearBtn.yPosition = AE_TOTAL_ROWS_HEIGHT + GUI_SEARCH_ROW + GUI_UPPER_HEIGHT + 32;
				this.trashBtn.yPosition = AE_TOTAL_ROWS_HEIGHT + GUI_SEARCH_ROW + GUI_UPPER_HEIGHT + 89;
			}
			else {
				this.clearBtn.yPosition = AE_TOTAL_ROWS_HEIGHT + GUI_SEARCH_ROW + GUI_UPPER_HEIGHT + 13;
				this.trashBtn.yPosition = AE_TOTAL_ROWS_HEIGHT + GUI_SEARCH_ROW + GUI_UPPER_HEIGHT + 68;
			}
			AE_TOTAL_ROWS_HEIGHT = AE_NUM_ROWS * AE_ROW_HEIGHT;
			GUI_LOWER_HEIGHT = GUI_HEIGHT - AE_TOTAL_ROWS_HEIGHT - GUI_UPPER_HEIGHT;
			slotYOffset = AE_TOTAL_ROWS_HEIGHT;
			craftingGridOffsetY = slotYOffset + 83;
		}
	}

	/**
	 * Detects if the window has been resized
	 * 
	 * @return True if window has been resized
	 * @author p455w0rd
	 */
	private boolean hasScreenResChanged() {
		if ((this.currScreenWidth != this.mc.displayWidth) || (this.currScreenHeight != this.mc.displayHeight) || (this.isFullScreen != this.mc.isFullScreen())) {
			this.currScreenWidth = this.mc.displayWidth;
			this.currScreenHeight = this.mc.displayHeight;
			this.isFullScreen = this.mc.isFullScreen();
			return true;
		}
		return false;
	}

	/**
	 * Draws the screen and tooltips.
	 */
	public void drawScreen(int mouseX, int mouseY, float btn) {
		super.drawScreen(mouseX, mouseY, btn);
		this.xSize_lo = (float) mouseX;
		this.ySize_lo = (float) mouseY;
		this.xSize = GUI_WIDTH;
		this.ySize = GUI_HEIGHT;

		final boolean hasClicked = Mouse.isButtonDown(0);
		if (hasClicked && this.scrollBar != null) {
			this.scrollBar.click(this, mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		for (final Object c : this.buttonList) {
			if (c instanceof ITooltip) {
				final ITooltip tooltip = (ITooltip) c;
				final int x = tooltip.xPos();
				int y = tooltip.yPos();

				if (x < mouseX && x + tooltip.getWidth() > mouseX && tooltip.isVisible()) {
					if (y < mouseY && y + tooltip.getHeight() > mouseY) {
						if (y < 15) {
							y = 15;
						}

						final String msg = tooltip.getMessage();
						if (msg != null) {
							this.drawTooltip(x + 11, y + 4, 0, msg);
						}
					}
				}
			}
		}
	}

	public void drawTooltip(final int par2, final int par3, final int forceWidth, final String message) {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		final String[] var4 = message.split("\n");

		if (var4.length > 0) {
			int var5 = 0;
			int var6;
			int var7;

			for (var6 = 0; var6 < var4.length; ++var6) {
				var7 = this.fontRendererObj.getStringWidth(var4[var6]);

				if (var7 > var5) {
					var5 = var7;
				}
			}

			var6 = par2 + 12;
			var7 = par3 - 12;
			int var9 = 8;

			if (var4.length > 1) {
				var9 += 2 + (var4.length - 1) * 10;
			}

			if (this.guiTop + var7 + var9 + 6 > this.height) {
				var7 = this.height - var9 - this.guiTop - 6;
			}

			if (forceWidth > 0) {
				var5 = forceWidth;
			}

			this.zLevel = 300.0F;
			itemRender.zLevel = 300.0F;
			final int var10 = -267386864;
			this.drawGradientRect(var6 - 3, var7 - 4, var6 + var5 + 3, var7 - 3, var10, var10);
			this.drawGradientRect(var6 - 3, var7 + var9 + 3, var6 + var5 + 3, var7 + var9 + 4, var10, var10);
			this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 + var9 + 3, var10, var10);
			this.drawGradientRect(var6 - 4, var7 - 3, var6 - 3, var7 + var9 + 3, var10, var10);
			this.drawGradientRect(var6 + var5 + 3, var7 - 3, var6 + var5 + 4, var7 + var9 + 3, var10, var10);
			final int var11 = 1347420415;
			final int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
			this.drawGradientRect(var6 - 3, var7 - 3 + 1, var6 - 3 + 1, var7 + var9 + 3 - 1, var11, var12);
			this.drawGradientRect(var6 + var5 + 2, var7 - 3 + 1, var6 + var5 + 3, var7 + var9 + 3 - 1, var11, var12);
			this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 - 3 + 1, var11, var11);
			this.drawGradientRect(var6 - 3, var7 + var9 + 2, var6 + var5 + 3, var7 + var9 + 3, var12, var12);

			for (int var13 = 0; var13 < var4.length; ++var13) {
				String var14 = var4[var13];

				if (var13 == 0) {
					var14 = '\u00a7' + Integer.toHexString(15) + var14;
				}
				else {
					var14 = "\u00a77" + var14;
				}

				this.fontRendererObj.drawStringWithShadow(var14, var6, var7, -1);

				if (var13 == 0) {
					var7 += 2;
				}

				var7 += 10;
			}

			this.zLevel = 0.0F;
			itemRender.zLevel = 0.0F;
		}
		GL11.glPopAttrib();
	}

	public void bindTexture(final String base, final String file) {
		final ResourceLocation loc = new ResourceLocation(base, "textures/" + file);
		this.mc.getTextureManager().bindTexture(loc);
	}

	/**
	 * Draw GUI Labels and item tooltips
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		final int ox = this.guiLeft; // (width - xSize) / 2;
		final int oy = this.guiTop; // (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		if (this.scrollBar != null) {
			this.scrollBar.draw(this);
		}
		this.drawFG(ox, oy, mouseX, mouseY);
	}

	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
		String s = LocaleHandler.WirelessTermLabel.getLocal();
		this.mc.fontRenderer.drawString(s, 7, 5, 4210752);
		this.mc.fontRenderer.drawString(I18n.format("container.inventory"), 7, AE_TOTAL_ROWS_HEIGHT + GUI_SEARCH_ROW + GUI_UPPER_HEIGHT + 3, 4210752);
		if (this.searchField != null) {
			this.searchField.drawTextBox();
		}
	}

	/**
	 * Draw and piece together the background texture
	 */
	protected void drawGuiContainerBackgroundLayer(final float f, final int x, final int y) {
		final int ox = this.guiLeft; // (width - xSize) / 2;
		final int oy = this.guiTop; // (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawBG(ox, oy, x, y);

		final List<Slot> slots = this.getInventorySlots();
		for (final Slot slot : slots) {
			if (slot instanceof OptionalSlotFake) {
				final OptionalSlotFake fs = (OptionalSlotFake) slot;
				if (fs.renderDisabled()) {
					if (fs.isEnabled()) {
						this.drawTexturedModalRect(ox + fs.xDisplayPosition - 1, oy + fs.yDisplayPosition - 1, fs.getSourceX() - 1, fs.getSourceY() - 1, 18, 18);
					}
					else {
						GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.4F);
						GL11.glEnable(GL11.GL_BLEND);
						this.drawTexturedModalRect(ox + fs.xDisplayPosition - 1, oy + fs.yDisplayPosition - 1, fs.getSourceX() - 1, fs.getSourceY() - 1, 18, 18);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
						GL11.glPopAttrib();
					}
				}
			}
		}
	}

	public void drawBG(int ox, int oy, int x, int y) {
		this.bindTexture(bgTexturePath);

		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, GUI_WIDTH, GUI_UPPER_HEIGHT);

		for (int i = 0; i <= AE_NUM_ROWS; ++i) {
			int yPos = this.guiTop + GUI_UPPER_HEIGHT + (i * AE_ROW_HEIGHT);
			// Draw the texture
			this.drawTexturedModalRect(this.guiLeft, yPos, 0, GUI_SEARCH_ROW, GUI_WIDTH, AE_ROW_HEIGHT);
		}

		this.drawTexturedModalRect(this.guiLeft, this.guiTop + GUI_UPPER_HEIGHT + AE_TOTAL_ROWS_HEIGHT, 0, AE_ROW_HEIGHT + 17, GUI_WIDTH, GUI_LOWER_HEIGHT);
		if (Reference.WCT_BOOSTER_ENABLED) {
			this.drawTexturedModalRect(this.guiLeft + 132, this.guiTop + GUI_UPPER_HEIGHT + AE_TOTAL_ROWS_HEIGHT + 102, 237, 237, 19, 19);
		}
		GuiInventory.func_147046_a(this.guiLeft + 51, this.guiTop + GUI_UPPER_HEIGHT + AE_TOTAL_ROWS_HEIGHT + 112, 32, (float) (this.guiLeft + 51) - this.xSize_lo, (float) (this.guiTop + 95 + (AE_NUM_ROWS * AE_ROW_HEIGHT)) - this.ySize_lo, this.mc.thePlayer);
	}

	@Override
	protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {

		final Enum searchMode = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);

		if (searchMode != SearchBoxMode.AUTOSEARCH && searchMode != SearchBoxMode.NEI_AUTOSEARCH) {
			this.searchField.mouseClicked(xCoord - this.guiLeft, yCoord - this.guiTop, btn);
		}

		if (btn == 1 && this.searchField.isMouseIn(xCoord - this.guiLeft, yCoord - this.guiTop)) {
			this.searchField.setText("");
			this.repo.setSearchString("");
			this.repo.updateView();
			this.setScrollBar();
		}

		this.drag_click.clear();

		if (btn == 1) {
			for (final Object o : this.buttonList) {
				final GuiButton guibutton = (GuiButton) o;
				if (guibutton.mousePressed(this.mc, xCoord, yCoord)) {
					super.mouseClicked(xCoord, yCoord, 0);
					return;
				}
			}
		}

		super.mouseClicked(xCoord, yCoord, btn);
	}

	@Override
	protected void mouseClickMove(final int x, final int y, final int c, final long d) {
		final Slot slot = this.getSlot(x, y);
		final ItemStack itemstack = this.mc.thePlayer.inventory.getItemStack();

		if (slot instanceof SlotFake && itemstack != null) {
			this.drag_click.add(slot);
			if (this.drag_click.size() > 1) {
				for (final Slot dr : this.drag_click) {
					final PacketInventoryAction p = new PacketInventoryAction(c == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE, dr.slotNumber, 0);
					NetworkHandler.instance.sendToServer(p);
				}
			}
		}
		else {
			super.mouseClickMove(x, y, c, d);
		}
	}

	@Override
	protected void handleMouseClick(final Slot slot, final int slotIdx, final int ctrlDown, final int key) {
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		if (slot instanceof SlotFake) {
			final InventoryAction action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;

			if (this.drag_click.size() > 1) {
				return;
			}

			final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
			NetworkHandler.instance.sendToServer(p);

			return;
		}

		else if (slot instanceof SlotCraftingTerm) {
			if (key == 6) {
				return; // prevent weird double clicks..
			}

			InventoryAction action = null;
			if (key == 1) {
				action = InventoryAction.CRAFT_SHIFT;
			}
			else {
				action = ctrlDown == 1 ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
			}

			final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
			NetworkHandler.instance.sendToServer(p);

			return;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			if (this.enableSpaceClicking()) {
				IAEItemStack stack = null;
				if (slot instanceof SlotME) {
					stack = ((SlotME) slot).getAEStack();
				}

				int slotNum = this.getInventorySlots().size();

				if (!(slot instanceof SlotME) && slot != null) {
					slotNum = slot.slotNumber;
				}

				((ContainerWirelessCraftingTerminal) this.inventorySlots).setTargetStack(stack);
				final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, slotNum, 0);
				NetworkHandler.instance.sendToServer(p);
				return;
			}
		}

		if (slot instanceof SlotDisconnected) {
			InventoryAction action = null;

			switch (key) {
			case 0: // pickup / set-down.
				action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				break;
			case 1:
				action = ctrlDown == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
				break;

			case 3: // creative dupe:

				if (player.capabilities.isCreativeMode) {
					action = InventoryAction.CREATIVE_DUPLICATE;
				}

				break;

			default:
			case 4: // drop item:
			case 6:
			}

			if (action != null) {
				final PacketInventoryAction p = new PacketInventoryAction(action, slot.getSlotIndex(), ((SlotDisconnected) slot).getSlot().getId());
				NetworkHandler.instance.sendToServer(p);
			}

			return;
		}

		if (slot instanceof SlotME) {
			InventoryAction action = null;
			IAEItemStack stack = null;

			switch (key) {
			case 0: // pickup / set-down.
				action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				stack = ((SlotME) slot).getAEStack();

				if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && stack.getStackSize() == 0 && player.inventory.getItemStack() == null) {
					action = InventoryAction.AUTO_CRAFT;
				}

				break;
			case 1:
				action = ctrlDown == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
				stack = ((SlotME) slot).getAEStack();
				break;

			case 3: // creative dupe:

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
			case 4: // drop item:
			case 6:
			}

			if (action != null) {
				((ContainerWirelessCraftingTerminal) this.inventorySlots).setTargetStack(stack);
				final PacketInventoryAction p = new PacketInventoryAction(action, this.getInventorySlots().size(), 0);
				NetworkHandler.instance.sendToServer(p);
			}

			return;
		}

		if (!this.disableShiftClick && isShiftKeyDown()) {
			this.disableShiftClick = true;

			if (this.dbl_whichItem == null || this.bl_clicked != slot || this.dbl_clickTimer.elapsed(TimeUnit.MILLISECONDS) > 150) {
				// some simple double click logic.
				this.bl_clicked = slot;
				this.dbl_clickTimer = Stopwatch.createStarted();
				if (slot != null) {
					this.dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : null;
				}
				else {
					this.dbl_whichItem = null;
				}
			}
			else if (this.dbl_whichItem != null) {
				// a replica of the weird broken vanilla feature.

				final List<Slot> slots = this.getInventorySlots();
				for (final Slot inventorySlot : slots) {
					if (inventorySlot != null && inventorySlot.canTakeStack(this.mc.thePlayer) && inventorySlot.getHasStack() && inventorySlot.inventory == slot.inventory && Container.func_94527_a(inventorySlot, this.dbl_whichItem, true)) {
						this.handleMouseClick(inventorySlot, inventorySlot.slotNumber, ctrlDown, 1);
					}
				}
			}

			this.disableShiftClick = false;
		}

		super.handleMouseClick(slot, slotIdx, ctrlDown, key);
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

		if (this.mc.thePlayer.inventory.getItemStack() == null && theSlot != null) {
			for (int j = 0; j < 9; ++j) {
				if (keyCode == this.mc.gameSettings.keyBindsHotbar[j].getKeyCode()) {
					final List<Slot> slots = this.getInventorySlots();
					for (final Slot s : slots) {
						if (s.getSlotIndex() == j && s.inventory == ((ContainerWirelessCraftingTerminal) this.inventorySlots).getPlayerInv()) {
							if (!s.canTakeStack(((ContainerWirelessCraftingTerminal) this.inventorySlots).getPlayerInv().player)) {
								return false;
							}
						}
					}

					if (theSlot.getSlotStackLimit() == 64) {
						this.handleMouseClick(theSlot, theSlot.slotNumber, j, 2);
						return true;
					}
					else {
						for (final Slot s : slots) {
							if (s.getSlotIndex() == j && s.inventory == ((ContainerWirelessCraftingTerminal) this.inventorySlots).getPlayerInv()) {
								NetworkHandler.instance.sendToServer(new PacketSwapSlots(s.slotNumber, theSlot.slotNumber));
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
		this.subGui = true; // in case the gui is reopened later ( i'm looking at you NEI )
		Keyboard.enableRepeatEvents(false);
		memoryText = this.searchField.getText();
	}

	protected Slot getSlot(final int mouseX, final int mouseY) {
		final List<Slot> slots = this.getInventorySlots();
		for (final Slot slot : slots) {
			// isPointInRegion
			if (this.func_146978_c(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY)) {
				return slot;
			}
		}

		return null;
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();

		final int i = Mouse.getEventDWheel();
		if (i != 0 && isShiftKeyDown()) {
			final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
			final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
			this.mouseWheelEvent(x, y, i / Math.abs(i));
		}
		else if (i != 0 && this.scrollBar != null) {
			this.scrollBar.wheel(i);
		}
	}

	private void mouseWheelEvent(final int x, final int y, final int wheel) {
		final Slot slot = this.getSlot(x, y);
		if (slot instanceof SlotME) {
			final IAEItemStack item = ((SlotME) slot).getAEStack();
			if (item != null) {
				((ContainerWirelessCraftingTerminal) this.inventorySlots).setTargetStack(item);
				final InventoryAction direction = wheel > 0 ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
				final int times = Math.abs(wheel);
				final int inventorySize = this.getInventorySlots().size();
				for (int h = 0; h < times; h++) {
					final PacketInventoryAction p = new PacketInventoryAction(direction, inventorySize, 0);
					NetworkHandler.instance.sendToServer(p);
				}
			}
		}
	}

	protected boolean enableSpaceClicking() {
		return true;
	}

	protected void drawItem(final int x, final int y, final ItemStack is) {
		this.zLevel = 100.0F;
		itemRender.zLevel = 100.0F;

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.renderEngine, is, x, y);
		GL11.glPopAttrib();

		itemRender.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	protected String getGuiDisplayName(final String in) {
		return this.hasCustomInventoryName() ? this.getInventoryName() : in;
	}

	private boolean hasCustomInventoryName() {
		if (this.inventorySlots instanceof ContainerWirelessCraftingTerminal) {
			return ((ContainerWirelessCraftingTerminal) this.inventorySlots).getCustomName() != null;
		}
		return false;
	}

	private String getInventoryName() {
		return ((ContainerWirelessCraftingTerminal) this.inventorySlots).getCustomName();
	}

	private void drawSlot(final Slot s) {
		if (s instanceof SlotME) {
			final RenderItem pIR = this.setItemRender(this.aeRenderItem);
			try {
				this.zLevel = 100.0F;
				itemRender.zLevel = 100.0F;

				if (!this.isPowered()) {
					GL11.glDisable(GL11.GL_LIGHTING);
					drawRect(s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66111111);
					GL11.glEnable(GL11.GL_LIGHTING);
				}

				this.zLevel = 0.0F;
				itemRender.zLevel = 0.0F;

				this.aeRenderItem.setAeStack(((SlotME) s).getAEStack());

				this.safeDrawSlot(s);
			}
			catch (final Exception err) {
				WCTLog.warning("WCT prevented crash while drawing slot: " + err.toString());
			}
			this.setItemRender(pIR);
			return;
		}
		else {
			this.itemRender = new RenderItem();
			try {
				final ItemStack is = s.getStack();
				if (s instanceof AppEngSlot && (((AppEngSlot) s).renderIconWithItem() || is == null) && (((AppEngSlot) s).shouldDisplay())) {
					final AppEngSlot aes = (AppEngSlot) s;
					if (aes.getIcon() >= 0) {
						this.bindTexture("appliedenergistics2", "guis/states.png");

						GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
						final Tessellator tessellator = Tessellator.instance;
						try {
							final int uv_y = (int) Math.floor(aes.getIcon() / 16);
							final int uv_x = aes.getIcon() - uv_y * 16;

							GL11.glEnable(GL11.GL_BLEND);
							GL11.glDisable(GL11.GL_LIGHTING);
							GL11.glEnable(GL11.GL_TEXTURE_2D);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
							GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
							final float par1 = aes.xDisplayPosition;
							final float par2 = aes.yDisplayPosition;
							final float par3 = uv_x * 16;
							final float par4 = uv_y * 16;

							tessellator.startDrawingQuads();
							tessellator.setColorRGBA_F(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon());
							final float f1 = 0.00390625F;
							final float f = 0.00390625F;
							final float par6 = 16;
							tessellator.addVertexWithUV(par1 + 0, par2 + par6, this.zLevel, (par3 + 0) * f, (par4 + par6) * f1);
							final float par5 = 16;
							tessellator.addVertexWithUV(par1 + par5, par2 + par6, this.zLevel, (par3 + par5) * f, (par4 + par6) * f1);
							tessellator.addVertexWithUV(par1 + par5, par2 + 0, this.zLevel, (par3 + par5) * f, (par4 + 0) * f1);
							tessellator.addVertexWithUV(par1 + 0, par2 + 0, this.zLevel, (par3 + 0) * f, (par4 + 0) * f1);
							tessellator.setColorRGBA_F(1.0f, 1.0f, 1.0f, 1.0f);
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
								isValid = ((SlotRestrictedInput) s).isValid(is, this.mc.theWorld);
							}
							catch (final Exception err) {
								WCTLog.debug(err.getMessage());
							}
						}
						((AppEngSlot) s).setIsValid(isValid ? hasCalculatedValidness.Valid : hasCalculatedValidness.Invalid);
					}

					if (((AppEngSlot) s).getIsValid() == hasCalculatedValidness.Invalid) {
						this.zLevel = 100.0F;
						itemRender.zLevel = 100.0F;

						GL11.glDisable(GL11.GL_LIGHTING);
						drawRect(s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66ff6666);
						GL11.glEnable(GL11.GL_LIGHTING);

						this.zLevel = 0.0F;
						itemRender.zLevel = 0.0F;
					}
				}

				if (s instanceof AppEngSlot) {
					((AppEngSlot) s).setDisplay(true);
					this.safeDrawSlot(s);
				}
				else {
					this.safeDrawSlot(s);
				}

				return;
			}
			catch (final Exception err) {
				WCTLog.warning("WCT prevented crash while drawing slot: " + err.toString());
			}
		}
		// do the usual for non-ME Slots.
		this.safeDrawSlot(s);
	}

	private RenderItem setItemRender(final RenderItem item) {
		if (isNEIEnabled) {
			return ((INEI) IntegrationRegistry.INSTANCE.getInstance(IntegrationType.NEI)).setItemRender(item);
		}
		else {
			final RenderItem ri = itemRender;
			itemRender = item;
			return ri;
		}
	}

	@Override
	protected void keyTyped(final char character, final int key) {
		if (!this.checkHotbarKeys(key)) {
			if (character == ' ' && this.searchField.getText().isEmpty()) {
				return;
			}

			if (this.searchField.textboxKeyTyped(character, key)) {
				this.repo.setSearchString(this.searchField.getText());
				this.repo.updateView();
				this.setScrollBar();
			}
			else {
				super.keyTyped(character, key);
			}
		}
	}

	protected boolean isPowered() {
		return this.repo.hasPower();
	}

	private void safeDrawSlot(final Slot s) {
		try {
			GuiContainer.class.getDeclaredMethod("func_146977_a_original", Slot.class).invoke(this, s);
		}
		catch (final Exception err) {
		}
	}

	public void bindTexture(final String file) {
		final ResourceLocation loc = new ResourceLocation(Reference.MODID, "textures/" + file);
		this.mc.getTextureManager().bindTexture(loc);
	}

	public void func_146977_a(final Slot s) {
		this.drawSlot(s);
	}

	protected GuiScrollbar getScrollBar() {
		return this.scrollBar;
	}

	protected void setScrollBar(final GuiScrollbar myScrollBar) {
		this.scrollBar = myScrollBar;
	}

	protected List<InternalSlotME> getMeSlots() {
		return this.meSlots;
	}

	public static final synchronized boolean isSwitchingGuis() {
		return switchingGuis;
	}

	public static final synchronized void setSwitchingGuis(final boolean switchingGuis) {
		GuiWirelessCraftingTerminal.switchingGuis = switchingGuis;
	}

	public List<String> handleItemTooltip(final ItemStack stack, final int mouseX, final int mouseY, final List<String> currentToolTip) {
		if (stack != null) {
			final Slot s = this.getSlot(mouseX, mouseY);
			if (s instanceof SlotME) {
				final int BigNumber = AEConfig.instance.useTerminalUseLargeFont() ? 999 : 9999;

				IAEItemStack myStack = null;

				try {
					final SlotME theSlotField = (SlotME) s;
					myStack = theSlotField.getAEStack();
				}
				catch (final Throwable ignore) {
				}

				if (myStack != null) {
					if (myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged())) {
						final String local = ButtonToolTips.ItemsStored.getLocal();
						final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(myStack.getStackSize());
						final String format = String.format(local, formattedAmount);

						currentToolTip.add("\u00a77" + format);
					}

					if (myStack.getCountRequestable() > 0) {
						final String local = ButtonToolTips.ItemsRequestable.getLocal();
						final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(myStack.getCountRequestable());
						final String format = String.format(local, formattedAmount);

						currentToolTip.add("\u00a77" + format);
					}
				}
				else if (stack.stackSize > BigNumber || (stack.stackSize > 1 && stack.isItemDamaged())) {
					final String local = ButtonToolTips.ItemsStored.getLocal();
					final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(stack.stackSize);
					final String format = String.format(local, formattedAmount);

					currentToolTip.add("\u00a77" + format);
				}
			}
		}
		return currentToolTip;
	}

	@Override
	protected void renderToolTip(final ItemStack stack, final int x, final int y) {
		final Slot s = this.getSlot(x, y);
		if (s instanceof SlotME && stack != null) {
			final int BigNumber = AEConfig.instance.useTerminalUseLargeFont() ? 999 : 9999;

			IAEItemStack myStack = null;

			try {
				final SlotME theSlotField = (SlotME) s;
				myStack = theSlotField.getAEStack();
			}
			catch (final Throwable ignore) {
			}

			if (myStack != null) {
				@SuppressWarnings("unchecked")
				final List<String> currentToolTip = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

				if (myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged())) {
					currentToolTip.add("Items Stored: " + NumberFormat.getNumberInstance(Locale.US).format(myStack.getStackSize()));
				}

				if (myStack.getCountRequestable() > 0) {
					currentToolTip.add("Items Requestable: " + NumberFormat.getNumberInstance(Locale.US).format(myStack.getCountRequestable()));
				}

				this.drawTooltip(x, y, 0, join(currentToolTip, "\n"));
			}
			else if (stack.stackSize > BigNumber) {
				final List var4 = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
				var4.add("Items Stored: " + NumberFormat.getNumberInstance(Locale.US).format(stack.stackSize));
				this.drawTooltip(x, y, 0, join(var4, "\n"));
				return;
			}
		}
		super.renderToolTip(stack, x, y);
		// super.drawItemStackTooltip( stack, x, y );
	}

	@Override
	public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
		if (this.SortByBox != null) {
			this.SortByBox.set(this.configSrc.getSetting(Settings.SORT_BY));
		}

		if (this.SortDirBox != null) {
			this.SortDirBox.set(this.configSrc.getSetting(Settings.SORT_DIRECTION));
		}

		if (this.ViewBox != null) {
			this.ViewBox.set(this.configSrc.getSetting(Settings.VIEW_MODE));
		}

		this.repo.updateView();
	}

	public boolean isCustomSortOrder() {
		return this.customSortOrder;
	}

	void setCustomSortOrder(final boolean customSortOrder) {
		this.customSortOrder = customSortOrder;
	}

	@Override
	public Enum getSortBy() {
		return this.configSrc.getSetting(Settings.SORT_BY);
	}

	@Override
	public Enum getSortDir() {
		return this.configSrc.getSetting(Settings.SORT_DIRECTION);
	}

	@Override
	public Enum getSortDisplay() {
		return this.configSrc.getSetting(Settings.VIEW_MODE);
	}

}
