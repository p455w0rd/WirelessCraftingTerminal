package p455w0rd.wct.client.gui;

import static p455w0rd.wct.init.ModEvents.CLIENT_TICKS;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import appeng.api.config.ActionItems;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.ISortSource;
import appeng.container.slot.AppEngSlot;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;
import p455w0rd.wct.client.gui.widgets.GuiMagnetButton;
import p455w0rd.wct.client.gui.widgets.GuiScrollbar;
import p455w0rd.wct.client.gui.widgets.GuiTabButton;
import p455w0rd.wct.client.gui.widgets.GuiTrashButton;
import p455w0rd.wct.client.gui.widgets.MEGuiTextField;
import p455w0rd.wct.client.me.InternalSlotME;
import p455w0rd.wct.client.me.ItemRepo;
import p455w0rd.wct.client.me.SlotME;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.WCTBaseContainer;
import p455w0rd.wct.container.slot.OptionalSlotFake;
import p455w0rd.wct.container.slot.SlotAEBauble;
import p455w0rd.wct.container.slot.SlotBooster;
import p455w0rd.wct.container.slot.SlotCraftingMatrix;
import p455w0rd.wct.container.slot.SlotFakeCraftingMatrix;
import p455w0rd.wct.container.slot.SlotTrash;
import p455w0rd.wct.handlers.GuiHandler;
import p455w0rd.wct.init.ModGlobals;
import p455w0rd.wct.init.ModGlobals.Mods;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModKeybindings;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketEmptyTrash;
import p455w0rd.wct.sync.packets.PacketInventoryAction;
import p455w0rd.wct.sync.packets.PacketSwitchGuis;
import p455w0rd.wct.sync.packets.PacketValueConfig;
import p455w0rd.wct.util.WCTUtils;
import p455w0rdslib.util.RenderUtils;
import yalter.mousetweaks.api.IMTModGuiContainer2;

@Interface(iface = "yalter.mousetweaks.api.IMTModGuiContainer2", modid = "mousetweaks", striprefs = true)
public class GuiWCT extends WCTBaseGui implements ISortSource, IConfigManagerHost, IMTModGuiContainer2 {

	private float xSize_lo;
	private float ySize_lo;
	public static int tick = 0, GUI_HEIGHT = 240, GUI_WIDTH = 198, AE_ROW_HEIGHT = 18, AE_NUM_ROWS = 0,
			GUI_UPPER_HEIGHT = 35, GUI_SEARCH_ROW = 35, SEARCH_X = 81, SEARCH_Y = 5, SEARCH_WIDTH = 88,
			SEARCH_HEIGHT = 12, SEARCH_MAXCHARS = 15, GUI_LOWER_HEIGHT, AE_TOTAL_ROWS_HEIGHT,
			BUTTON_SEARCH_MODE_POS_X = -18, BUTTON_SEARCH_MODE_POS_Y = 68, BUTTON_SIZE = 16, NEI_EXTRA_SPACE = 30,
			slotYOffset;
	protected static final int BUTTON_SEARCH_MODE_ID = 5;
	protected static final long TOOLTIP_UPDATE_INTERVAL = 3000L;
	private int currScreenWidth, currScreenHeight;
	private static final String BG_TEXTURE = "gui/crafting.png";
	private static final String BG_TEXTURE_BAUBLES = "gui/crafting_baubles.png";
	private final ContainerWCT containerWCT;
	private boolean isFullScreen, init = true, reInit, wasResized = false;
	private GuiScrollbar scrollBar = null;
	public static int craftingGridOffsetX = 80;
	public static int craftingGridOffsetY;

	private final ItemRepo repo;
	private final int offsetX = 8;
	private final IConfigManager configSrc;
	private GuiTabButton craftingStatusBtn;
	private GuiMagnetButton magnetGUIButton;
	private MEGuiTextField searchField;
	private int perRow = 9;
	private boolean customSortOrder = true;
	private GuiImgButton ViewBox;
	private GuiImgButton SortByBox;
	private GuiImgButton SortDirBox;
	private GuiImgButton searchBoxSettings;
	private GuiImgButton clearBtn;
	private GuiTrashButton trashBtn;
	private GuiImgButton terminalStyleBox;
	public boolean devicePowered = false;
	private boolean isJEIEnabled;
	private boolean wasTextboxFocused = false;
	private int screenResTicks = 0;
	private int reservedSpace = 0;
	private int maxRows = Integer.MAX_VALUE;
	private int standardSize;
	private final int lowerTextureOffset = 0;
	private int rows = 0;

	EntityLivingBase entity;
	boolean isHalloween = false;

	public GuiWCT(Container container) {
		super(container);
		xSize = GUI_WIDTH;
		ySize = GUI_HEIGHT;
		standardSize = xSize;
		setReservedSpace(73);
		containerWCT = (ContainerWCT) container;
		scrollBar = new GuiScrollbar();
		subGui = switchingGuis;
		switchingGuis = false;
		setScrollBar(scrollBar);
		repo = new ItemRepo(scrollBar, this);
		configSrc = containerWCT.getConfigManager();
		devicePowered = containerWCT.isPowered();
		((ContainerWCT) inventorySlots).setGui(this);

		entity = WCTUtils.player();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		isHalloween = calendar.get(2) + 1 == 10 && calendar.get(5) == 31;
	}

	public void postUpdate(final List<IAEItemStack> list) {
		for (final IAEItemStack is : list) {
			repo.postUpdate(is);
		}
		repo.updateView();
		setScrollBar();
	}

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

	private void setScrollBar() {
		getScrollBar().setTop(18).setLeft(174).setHeight(rows * 18 - 2);
		getScrollBar().setRange(0, (repo.size() + perRow - 1) / perRow - rows, Math.max(1, rows / 6));
	}

	@Override
	protected void setScrollBar(final GuiScrollbar myScrollBar) {
		scrollBar = myScrollBar;
	}

	@Override
	protected void actionPerformed(final GuiButton btn) {
		if (btn == craftingStatusBtn) {
			NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiHandler.GUI_CRAFTING_STATUS));
			return;
		}

		if (btn instanceof GuiImgButton) {
			final boolean backwards = Mouse.isButtonDown(1);

			final GuiImgButton iBtn = (GuiImgButton) btn;
			if (iBtn.getSetting() != Settings.ACTIONS) {
				final Enum<?> cv = iBtn.getCurrentValue();
				Enum<?> next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());

				if (btn == terminalStyleBox) {
					AEConfig.instance().getConfigManager().putSetting(iBtn.getSetting(), next);
				}
				if (btn == searchBoxSettings) {
					AEConfig.instance().getConfigManager().putSetting(iBtn.getSetting(), next);
				}
				else {
					try {
						NetworkHandler.instance().sendToServer(new PacketValueConfig(iBtn.getSetting().name(), next.name()));
					}
					catch (final IOException e) {
					}
				}

				iBtn.set(next);

				if (next.getClass() == SearchBoxMode.class || next.getClass() == TerminalStyle.class) {
					reinitalize();
				}
			}
			if (clearBtn == btn) {
				Slot s = null;
				final Container c = inventorySlots;
				for (final Object j : c.inventorySlots) {
					if (j instanceof SlotCraftingMatrix) {
						s = (Slot) j;
					}
				}

				if (s != null) {
					final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, s.slotNumber, 0);
					NetworkHandler.instance().sendToServer(p);
				}
			}
		}
		if (btn instanceof GuiTrashButton) {
			if (trashBtn == btn) {
				Slot s = null;
				final Container c = inventorySlots;
				for (final Object j : c.inventorySlots) {
					if (j instanceof SlotTrash) {
						s = (Slot) j;
					}
				}

				if (s != null) {
					if (s.getHasStack()) {
						containerWCT.getTrashSlot().clearStack();
						NetworkHandler.instance().sendToServer(new PacketEmptyTrash());
					}
				}
			}
			return;
		}
		if (btn == magnetGUIButton) {
			NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiHandler.GUI_MAGNET));
		}

	}

	private void reinitalize() {
		//buttonList.clear();
		initGui();
	}

	int getReservedSpace() {
		return reservedSpace;
	}

	void setReservedSpace(final int reservedSpace) {
		this.reservedSpace = reservedSpace;
	}

	public int getStandardSize() {
		return standardSize;
	}

	void setStandardSize(final int standardSize) {
		this.standardSize = standardSize;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		maxRows = getMaxRows();
		perRow = AEConfig.instance().getConfigManager().getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL ? 9 : 9 + ((width - standardSize) / 18);
		isJEIEnabled = Loader.isModLoaded("JEI");
		int top = isJEIEnabled ? 22 : 0;
		final int magicNumber = 114 + 1;
		final int extraSpace = height - magicNumber - 0 - top - reservedSpace;
		rows = (int) Math.floor(extraSpace / 18);
		if (rows > maxRows) {
			top += (rows - maxRows) * 18 / 2;
			rows = maxRows;
		}

		if (isJEIEnabled) {
			rows--;
		}

		if (rows < 3) {
			rows = 3;
		}

		getMeSlots().clear();
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < perRow; x++) {
				getMeSlots().add(new InternalSlotME(repo, x + y * perRow, offsetX + x * 18, 18 + y * 18));
			}
		}
		if (AEConfig.instance().getConfigManager().getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL) {
			xSize = standardSize + ((perRow - 9) * 18);
		}
		else {
			xSize = standardSize;
		}

		ySize = magicNumber + rows * 18 + reservedSpace;
		final int unusedSpace = height - ySize;
		guiTop = (int) Math.floor(unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f));
		int offset = guiTop + 8;

		super.initGui();

		buttonList.clear();
		buttonList.add(clearBtn = new GuiImgButton(guiLeft + 134, guiTop + ySize - 160, Settings.ACTIONS, ActionItems.STASH));
		buttonList.add(trashBtn = new GuiTrashButton(guiLeft + 116, guiTop + ySize - 104));
		clearBtn.setHalfSize(true);
		if (customSortOrder) {
			buttonList.add(SortByBox = new GuiImgButton(guiLeft - 18, offset, Settings.SORT_BY, configSrc.getSetting(Settings.SORT_BY)));
			offset += 20;
		}

		buttonList.add(ViewBox = new GuiImgButton(guiLeft - 18, offset, Settings.VIEW_MODE, configSrc.getSetting(Settings.VIEW_MODE)));
		offset += 20;

		buttonList.add(SortDirBox = new GuiImgButton(guiLeft - 18, offset, Settings.SORT_DIRECTION, configSrc.getSetting(Settings.SORT_DIRECTION)));
		offset += 20;

		buttonList.add(searchBoxSettings = new GuiImgButton(guiLeft - 18, offset, Settings.SEARCH_MODE, AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE)));
		offset += 20;

		buttonList.add(terminalStyleBox = new GuiImgButton(guiLeft - 18, offset, Settings.TERMINAL_STYLE, AEConfig.instance().getConfigManager().getSetting(Settings.TERMINAL_STYLE)));

		searchField = new MEGuiTextField(fontRendererObj, SEARCH_X, SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT);

		searchField.setEnableBackgroundDrawing(false);
		searchField.setMaxStringLength(25);
		searchField.setTextColor(0xFFFFFF);
		searchField.setVisible(true);
		searchField.setEnabled(true);

		buttonList.add(craftingStatusBtn = new GuiTabButton(guiLeft + 169, guiTop - 4, 2 + 11 * 16, GuiText.CraftingStatus.getLocal(), itemRender));
		buttonList.add(magnetGUIButton = new GuiMagnetButton(guiLeft + 157, guiTop + ySize - 115));
		craftingStatusBtn.setHideEdge(13);

		final Enum<?> setting = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);
		searchField.setFocused(SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.JEI_AUTOSEARCH == setting);

		if (memoryText != null) {
			searchField.setText(memoryText);
			repo.setSearchString(memoryText);
			repo.updateView();
			setScrollBar();
		}

		craftingGridOffsetX = Integer.MAX_VALUE;
		craftingGridOffsetY = Integer.MAX_VALUE;

		for (final Object s : inventorySlots.inventorySlots) {
			if (s instanceof AppEngSlot) {
				if (((Slot) s).xPos < 197) {
					repositionSlot((AppEngSlot) s);
				}
			}

			if (s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix) {
				final Slot g = (Slot) s;
				if (g.xPos > 0 && g.yPos > 0) {
					craftingGridOffsetX = Math.min(craftingGridOffsetX, g.xPos);
					craftingGridOffsetY = Math.min(craftingGridOffsetY, g.yPos);
				}
			}
		}

		if (isHalloween) {
			entity = new EntitySkeleton(WCTUtils.world());
			entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Blocks.LIT_PUMPKIN));
		}
	}

	private List<Slot> getInventorySlots() {
		return inventorySlots.inventorySlots;
	}

	int getMaxRows() {
		return AEConfig.instance().getConfigManager().getSetting(Settings.TERMINAL_STYLE) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
	}

	protected void repositionSlot(final AppEngSlot s) {
		s.yPos = s.getY() + ySize - 78 - 5;
	}

	@Override
	public void updateScreen() {
		devicePowered = containerWCT.isPowered();
		repo.setPower(devicePowered);

		super.updateScreen();
		if (init) {
			currScreenWidth = mc.displayWidth;
			currScreenHeight = mc.displayHeight;
			isFullScreen = mc.isFullScreen();
			reInit = true;
			init = false;
		}
		++screenResTicks;
		if (screenResTicks == 20) {
			wasTextboxFocused = searchField.isFocused();
			screenResTicks = 0;
		}
		if (reInit) {
			if (tick != 1) {
				tick++;
			}
			else {
				initGui();
				setScrollBar();
				if (wasResized == true) {
					searchField.setFocused(wasTextboxFocused);
					searchField.setText(repo.getSearchString());
					wasTextboxFocused = false;
					wasResized = false;
				}
				reInit = false;
				tick = 0;
			}
		}
		if (hasScreenResChanged()) {
			reInit = true;
			wasResized = true;
		}
		magnetGUIButton.visible = WCTUtils.isMagnetInstalled(mc.player.inventory);
		if (!mc.player.isEntityAlive() || mc.player.isDead) {
			mc.player.closeScreen();
		}
	}

	private boolean hasScreenResChanged() {
		if ((currScreenWidth != mc.displayWidth) || (currScreenHeight != mc.displayHeight) || (isFullScreen != mc.isFullScreen())) {
			currScreenWidth = mc.displayWidth;
			currScreenHeight = mc.displayHeight;
			isFullScreen = mc.isFullScreen();
			return true;
		}
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float btn) {
		//super.drawScreen(mouseX, mouseY, btn);
		xSize_lo = mouseX;
		ySize_lo = mouseY;

		final boolean hasClicked = Mouse.isButtonDown(0);
		if (hasClicked && scrollBar != null) {
			scrollBar.click(this, mouseX - guiLeft, mouseY - guiTop);
		}
		super.drawScreen(mouseX, mouseY, btn);
	}

	@Override
	public void bindTexture(final String base, final String file) {
		final ResourceLocation loc = new ResourceLocation(base, "textures/" + file);
		mc.getTextureManager().bindTexture(loc);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		final int ox = guiLeft; // (width - xSize) / 2;
		final int oy = guiTop; // (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		if (scrollBar != null) {
			scrollBar.draw(this);
		}
		drawFG(ox, oy, mouseX, mouseY);
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
		String s = "Terminal";
		mc.fontRendererObj.drawString(s, 7, 5, 4210752);
		mc.fontRendererObj.drawString(I18n.format("container.inventory"), 7, ySize - 172 + 3, 4210752);
		if (searchField != null) {
			searchField.drawTextBox();
		}
	}

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
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		this.bindTexture(Mods.BAUBLES.isLoaded() ? BG_TEXTURE_BAUBLES : BG_TEXTURE);
		final int x_width = 199;
		/*
				this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

				for (int x = 0; x < rows; x++) {
					this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
				}

				this.drawTexturedModalRect(offsetX, offsetY + 16 + rows * 18 + lowerTextureOffset, 0, 106 - 18 - 18, x_width, 99 + reservedSpace - lowerTextureOffset);

				if (ModConfig.WCT_BOOSTER_ENABLED) {
					this.drawTexturedModalRect(guiLeft + 132, (guiTop + rows * 18) + 83, 237, 237, 19, 19);
				}
				*/
		GuiInventory.drawEntityOnScreen(guiLeft + 51, (guiTop + rows * 18) + (isHalloween && !isAltKeyDown() ? 98 : 94), 32, guiLeft + 51 - xSize_lo, (guiTop + rows * 18) + 50 - ySize_lo, (!isAltKeyDown() ? entity : WCTUtils.player()));

		//draw "over inventory area"
		drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

		//draw "top line" of ME inv
		drawTexturedModalRect(offsetX, offsetY + 18, 0, 18, x_width, 18);

		for (int i = 1; i < rows - 1; i++) {
			drawTexturedModalRect(offsetX, offsetY + 18 + i * 18, 0, 18 + 18, x_width, 18);
		}

		//draw "bottom line" of ME inv
		drawTexturedModalRect(offsetX, offsetY + 18 + (rows - 1) * 18, 0, 18 + 2 * 18, x_width, 18);

		//draw player inv
		drawTexturedModalRect(offsetX, offsetY + 16 + rows * 18 + lowerTextureOffset, 0, 106 - 18 - 18, x_width, 99 + reservedSpace - lowerTextureOffset);
		if (isHalloween && !isAltKeyDown()) {

			String name = "Happy Halloween!            ";
			int idx = (int) ((CLIENT_TICKS / 4) % name.length());
			name = (name + " " + name).substring(idx, idx + 10);

			FontRenderer fr = RenderUtils.getFontRenderer();
			GlStateManager.disableDepth();
			fr.drawStringWithShadow(name, (guiLeft + 52) - fr.getStringWidth(name) / 2, (guiTop + rows * 18) + 90, 0xFFFFA00F);
			GlStateManager.enableDepth();
		}

	}

	@Override
	protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {

		final Enum<?> searchMode = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);

		if (searchMode != SearchBoxMode.AUTOSEARCH && searchMode != SearchBoxMode.JEI_AUTOSEARCH) {
			searchField.mouseClicked(xCoord - guiLeft, yCoord - guiTop, btn);
		}

		if (btn == 1 && searchField.isMouseIn(xCoord - guiLeft, yCoord - guiTop)) {
			searchField.setText("");
			repo.setSearchString("");
			repo.updateView();
			setScrollBar();
		}
		/*
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
		*/
		super.mouseClicked(xCoord, yCoord, btn);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
		if (searchField.getText() != null) {
			memoryText = searchField.getText();
		}
	}

	@Override
	protected String getGuiDisplayName(final String in) {
		return hasCustomInventoryName() ? getInventoryName() : in;
	}

	private boolean hasCustomInventoryName() {
		if (inventorySlots instanceof ContainerWCT) {
			return ((ContainerWCT) inventorySlots).getCustomName() != null;
		}
		return false;
	}

	private String getInventoryName() {
		return ((ContainerWCT) inventorySlots).getCustomName();
	}

	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		//if (slotIn != null && slotIn.getStack() != null && !(slotIn.getStack().getItem() instanceof IWirelessCraftingTerminalItem) && type != ClickType.SWAP) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		//}
	}

	@Override
	protected void keyTyped(final char character, final int key) throws IOException {
		Slot slot = null;
		try {
			slot = ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, this, "theSlot", "field_147006_u", "f");
		}
		catch (final Throwable t) {
		}
		if (slot != null && ((slot instanceof SlotBooster && slot.getHasStack() && slot.getStack().getItem() == ModItems.BOOSTER_CARD) || (slot instanceof SlotAEBauble && slot.getHasStack() && slot.getStack().getItem() == ModItems.WCT))) {
			//System.out.println("");
			return;
		}
		if (!checkHotbarKeys(key)) {
			if (character == ' ' && searchField.getText().isEmpty()) {
				return;
			}
			if (ModKeybindings.openTerminal.getKeyCode() == key) {
				if (AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE) == SearchBoxMode.MANUAL_SEARCH && !searchField.isFocused()) {
					WCTUtils.player().closeScreen();
				}
				else {
					if (isCtrlKeyDown()) {
						WCTUtils.player().closeScreen();
					}
				}
			}
			if (searchField.textboxKeyTyped(character, key)) {
				repo.setSearchString(searchField.getText());
				repo.updateView();
				setScrollBar();
			}
			else {
				super.keyTyped(character, key);
			}
		}
	}

	@Override
	protected boolean isPowered() {
		return repo.hasPower();
	}

	@Override
	public void bindTexture(final String file) {
		final ResourceLocation loc = new ResourceLocation(ModGlobals.MODID, "textures/" + file);
		mc.getTextureManager().bindTexture(loc);
	}

	@Override
	protected GuiScrollbar getScrollBar() {
		return scrollBar;
	}

	@Override
	protected List<InternalSlotME> getMeSlots() {
		return meSlots;
	}

	public List<String> handleItemTooltip(final ItemStack stack, final int mouseX, final int mouseY, final List<String> currentToolTip) {
		if (stack != null) {
			final Slot s = getSlot(mouseX, mouseY);
			if (s instanceof SlotME) {
				final int BigNumber = AEConfig.instance().useTerminalUseLargeFont() ? 999 : 9999;

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
		final Slot s = getSlot(x, y);
		if (s instanceof SlotME && stack != null) {
			final int BigNumber = AEConfig.instance().useTerminalUseLargeFont() ? 999 : 9999;

			IAEItemStack myStack = null;

			try {
				final SlotME theSlotField = (SlotME) s;
				myStack = theSlotField.getAEStack();
			}
			catch (final Throwable ignore) {
			}

			if (myStack != null) {
				final List<String> currentToolTip = stack.getTooltip(WCTUtils.player(), mc.gameSettings.advancedItemTooltips);

				if (myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged())) {
					currentToolTip.add("Items Stored: " + NumberFormat.getNumberInstance(Locale.US).format(myStack.getStackSize()));
				}

				if (myStack.getCountRequestable() > 0) {
					currentToolTip.add("Items Requestable: " + NumberFormat.getNumberInstance(Locale.US).format(myStack.getCountRequestable()));
				}

				drawTooltip(x, y, join(currentToolTip, "\n"));
			}
			else if (stack.stackSize > BigNumber) {
				final List<String> var4 = stack.getTooltip(WCTUtils.player(), mc.gameSettings.advancedItemTooltips);
				var4.add("Items Stored: " + NumberFormat.getNumberInstance(Locale.US).format(stack.stackSize));
				drawTooltip(x, y, join(var4, "\n"));
				return;
			}
		}
		super.renderToolTip(stack, x, y);
	}

	@Override
	public void updateSetting(final IConfigManager manager, Enum settingName, Enum newValue) {
		if (SortByBox != null) {
			SortByBox.set(configSrc.getSetting(Settings.SORT_BY));
		}

		if (SortDirBox != null) {
			SortDirBox.set(configSrc.getSetting(Settings.SORT_DIRECTION));
		}

		if (ViewBox != null) {
			ViewBox.set(configSrc.getSetting(Settings.VIEW_MODE));
		}

		repo.updateView();
	}

	public boolean isCustomSortOrder() {
		return customSortOrder;
	}

	void setCustomSortOrder(final boolean customSortOrder) {
		this.customSortOrder = customSortOrder;
	}

	@Override
	public Enum getSortBy() {
		return configSrc.getSetting(Settings.SORT_BY);
	}

	@Override
	public Enum getSortDir() {
		return configSrc.getSetting(Settings.SORT_DIRECTION);
	}

	@Override
	public Enum getSortDisplay() {
		return configSrc.getSetting(Settings.VIEW_MODE);
	}

	@Method(modid = "mousetweaks")
	@Override
	public boolean MT_isMouseTweaksDisabled() {
		return true;
	}

	@Method(modid = "mousetweaks")
	@Override
	public boolean MT_isWheelTweakDisabled() {
		return false;
	}

	@Method(modid = "mousetweaks")
	@Override
	public Container MT_getContainer() {
		return null;
	}

	@Method(modid = "mousetweaks")
	@Override
	public Slot MT_getSlotUnderMouse() {
		return null;
	}

	@Method(modid = "mousetweaks")
	@Override
	public boolean MT_isCraftingOutput(Slot slot) {
		return false;
	}

	@Method(modid = "mousetweaks")
	@Override
	public boolean MT_isIgnored(Slot slot) {
		return false;
	}

	@Method(modid = "mousetweaks")
	@Override
	public boolean MT_disableRMBDraggingFunctionality() {
		return false;
	}

}
