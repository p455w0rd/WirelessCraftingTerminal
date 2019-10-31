package p455w0rd.wct.client.gui;

import static p455w0rd.wct.init.ModEvents.CLIENT_TICKS;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.base.Stopwatch;

import appeng.api.config.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.*;
import appeng.container.slot.*;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.helpers.InventoryAction;
import appeng.integration.Integrations;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.api.client.ItemStackSizeRenderer;
import p455w0rd.ae2wtlib.api.client.gui.GuiWT;
import p455w0rd.ae2wtlib.api.client.gui.widgets.*;
import p455w0rd.ae2wtlib.api.container.ContainerWT;
import p455w0rd.ae2wtlib.api.container.slot.SlotTrash;
import p455w0rd.wct.client.gui.widgets.*;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.slot.SlotCraftingOutput;
import p455w0rd.wct.init.*;
import p455w0rd.wct.sync.packets.*;
import p455w0rd.wct.util.WCTUtils;
import p455w0rdslib.LibGlobals.Mods;
import p455w0rdslib.util.RenderUtils;
import yalter.mousetweaks.api.MouseTweaksIgnore;

@MouseTweaksIgnore
public class GuiWCT extends GuiWT implements ISortSource, IConfigManagerHost {

	public static String memoryText = "";
	private float xSize_lo;
	private float ySize_lo;
	private static int tick = 0, GUI_HEIGHT = 240, GUI_WIDTH = 198;
	protected static final int BUTTON_SEARCH_MODE_ID = 5;
	protected static final long TOOLTIP_UPDATE_INTERVAL = 3000L;
	private int currScreenWidth, currScreenHeight;
	private static final String BG_TEXTURE = "gui/crafting.png";
	private static final String BG_TEXTURE_BAUBLES = "gui/crafting_baubles.png";
	private final ContainerWCT containerWCT;
	private boolean isFullScreen, init = true, reInit, wasResized = false;
	public static int craftingGridOffsetX = 80;
	public static int craftingGridOffsetY;

	private final ItemRepo repo;
	private final int offsetX = 8;
	private final IConfigManager configSrc;
	private GuiItemIconButton craftingStatusBtn;
	private GuiMagnetButton magnetGUIButton;
	private GuiMETextField searchField;
	private int perRow = 9;
	private boolean customSortOrder = true;
	private GuiImgButton ViewBox;
	private GuiImgButton SortByBox;
	private GuiImgButton SortDirBox;
	private GuiImgButton searchBoxSettings;
	private GuiImgButton clearBtn;
	private GuiTrashButton trashBtn;
	private GuiImgButton terminalStyleBox;
	private GuiImgButtonBooster autoConsumeBoostersBox;
	private GuiImgButtonMagnetMode magnetModeBox;
	private GuiImgButtonShiftCraft shiftCraftButton;
	private boolean isJEIEnabled;
	private boolean wasTextboxFocused = false;
	private int screenResTicks = 0;
	private int reservedSpace = 0;
	private int maxRows = Integer.MAX_VALUE;
	private int standardSize;
	private final int lowerTextureOffset = 0;
	private int rows = 0;
	EntityLivingBase halloween;
	EntityLivingBase entity;
	boolean isHalloween = false;
	private final ItemStack[] myCurrentViewCells = new ItemStack[4];

	public GuiWCT(final Container container) {
		super(container);
		xSize = GUI_WIDTH;
		ySize = GUI_HEIGHT;
		standardSize = xSize;
		setReservedSpace(73);
		containerWCT = (ContainerWCT) container;
		setScrollBar(WTApi.instance().createScrollbar());
		subGui = switchingGuis;
		switchingGuis = false;
		repo = new ItemRepo(getScrollBar(), this);
		configSrc = containerWCT.getConfigManager();
		((ContainerWCT) inventorySlots).setGui(this);
		entity = Minecraft.getMinecraft().player;
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		isHalloween = calendar.get(2) + 1 == 10 && calendar.get(5) == 31;
	}

	@Override
	public void drawSlot(final Slot s) {
		if (s instanceof SlotME) {
			try {
				zLevel = 100.0F;
				itemRender.zLevel = 100.0F;
				if (!isPowered()) {
					drawRect(s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111);
				}
				zLevel = 0.0F;
				itemRender.zLevel = 0.0F;
				super.drawSlot(new Size1Slot((SlotME) s));
				ItemStackSizeRenderer.getInstance().renderStackSize(fontRenderer, ((SlotME) s).getAEStack(), s.xPos, s.yPos);
			}
			catch (final Exception err) {
			}
			return;
		}
		else {
			super.drawSlot(s);
		}
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
		else if (i != 0 && getScrollBar() != null) {
			getScrollBar().wheel(i);
		}
	}

	public ItemRepo getRepo() {
		return repo;
	}

	private void mouseWheelEvent(final int x, final int y, final int wheel) {
		final Slot slot = getSlot(x, y);
		if (slot instanceof SlotME) {
			final IAEItemStack item = ((SlotME) slot).getAEStack();
			if (item != null) {
				if (inventorySlots instanceof ContainerWCT) {
					((ContainerWCT) inventorySlots).setTargetStack(item);
				}
				final InventoryAction direction = wheel > 0 ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
				final int times = Math.abs(wheel);
				final int inventorySize = getInventorySlots().size();
				for (int h = 0; h < times; h++) {
					final PacketInventoryAction p = new PacketInventoryAction(direction, inventorySize, 0);
					ModNetworking.instance().sendToServer(p);
				}
			}
		}
	}

	private void setScrollBar() {
		getScrollBar().setTop(18).setLeft(174).setHeight(rows * 18 - 2);
		getScrollBar().setRange(0, (repo.size() + perRow - 1) / perRow - rows, Math.max(1, rows / 6));
	}

	@Override
	protected void actionPerformed(final GuiButton btn) throws IOException {
		if (btn == craftingStatusBtn) {
			ModNetworking.instance().sendToServer(new PacketSwitchGuis(ModGuiHandler.GUI_CRAFTING_STATUS));
			return;
		}

		if (btn instanceof GuiImgButton) {
			final boolean backwards = Mouse.isButtonDown(1);
			final GuiImgButton iBtn = (GuiImgButton) btn;
			if (iBtn.getSetting() != Settings.ACTIONS) {
				final Enum<?> cv = iBtn.getCurrentValue();
				final Enum<?> next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());
				if (btn == terminalStyleBox || btn == searchBoxSettings) {
					AEConfig.instance().getConfigManager().putSetting(iBtn.getSetting(), next);
				}
				else {
					ModNetworking.instance().sendToServer(new PacketValueConfig(iBtn.getSetting().name(), next.name()));
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
					ModNetworking.instance().sendToServer(p);
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
						WTApi.instance().getNetHandler().sendToServer(WTApi.instance().getNetHandler().createEmptyTrashPacket());
					}
				}
			}
			return;
		}
		if (btn == magnetGUIButton) {
			ModNetworking.instance().sendToServer(new PacketSwitchGuis(ModGuiHandler.GUI_MAGNET));
		}
		if (btn == autoConsumeBoostersBox) {
			autoConsumeBoostersBox.cycleValue();
		}
		if (btn == magnetModeBox) {
			magnetModeBox.cycleValue();
		}
		if (btn == shiftCraftButton) {
			shiftCraftButton.cycleValue();
		}
		else {
			super.actionPerformed(btn);
		}
	}

	@Override
	protected void mouseClickMove(final int x, final int y, final int c, final long d) {
		final Slot slot = getSlot(x, y);
		final ItemStack itemstack = ((EntityPlayer) entity).inventory.getItemStack();
		if (slot instanceof SlotFake && itemstack != null) {
			drag_click.add(slot);
			if (drag_click.size() > 1) {
				for (final Slot dr : drag_click) {
					final PacketInventoryAction p = new PacketInventoryAction(c == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE, dr.slotNumber, 0);
					ModNetworking.instance().sendToServer(p);
				}
			}
		}
		else {
			super.mouseClickMove(x, y, c, d);
		}
	}

	@Override
	protected void handleMouseClick(final Slot slot, final int slotIdx, final int mouseButton, final ClickType clickType) {
		final EntityPlayer player = Minecraft.getMinecraft().player;
		if (slot instanceof SlotFake) {
			final InventoryAction action = clickType == ClickType.QUICK_CRAFT ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
			if (drag_click.size() > 1) {
				return;
			}
			final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
			ModNetworking.instance().sendToServer(p);
			return;
		}
		else if (slot instanceof SlotCraftingOutput) {
			if (mouseButton == 6) {
				return; // prevent weird double clicks..
			}
			InventoryAction action = null;
			if (isShiftKeyDown()) {
				action = InventoryAction.CRAFT_SHIFT;
			}
			else {
				// Craft stack on right-click, craft single on left-click
				action = mouseButton == 1 ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
			}
			final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
			ModNetworking.instance().sendToServer(p);

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

				((ContainerWT) inventorySlots).setTargetStack(stack);

				final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, slotNum, 0);
				ModNetworking.instance().sendToServer(p);
				return;
			}
		}

		if (slot instanceof SlotDisconnected) {
			InventoryAction action = null;
			switch (clickType) {
			case PICKUP: // pickup / set-down.
				action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				break;
			case QUICK_MOVE:
				action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
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
				ModNetworking.instance().sendToServer(p);
			}
			return;
		}

		if (slot instanceof SlotME) {
			InventoryAction action = null;
			IAEItemStack stack = null;

			switch (clickType) {
			case PICKUP: // pickup / set-down.
				action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
				stack = ((SlotME) slot).getAEStack();

				if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && player.inventory.getItemStack().isEmpty() && (stack.getStackSize() == 0 || stack.getStackSize() > 0 && GuiScreen.isAltKeyDown())) {
					action = InventoryAction.AUTO_CRAFT;
				}

				break;
			case QUICK_MOVE:
				action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
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
					final PacketInventoryAction p = new PacketInventoryAction(action, getInventorySlots().size(), 0);
					ModNetworking.instance().sendToServer(p);
				}
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
					dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
				}
				else {
					dbl_whichItem = ItemStack.EMPTY;
				}
			}
			else if (!dbl_whichItem.isEmpty()) {
				// a replica of the weird broken vanilla feature.

				final List<Slot> slots = getInventorySlots();
				for (final Slot inventorySlot : slots) {
					if (inventorySlot != null && inventorySlot.canTakeStack(Minecraft.getMinecraft().player) && inventorySlot.getHasStack() && inventorySlot.inventory == slot.inventory && Container.canAddItemToSlot(inventorySlot, dbl_whichItem, true)) {
						handleMouseClick(inventorySlot, inventorySlot.slotNumber, 1, clickType);
					}
				}
			}

			disableShiftClick = false;
		}
		super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
	}

	private void reinitalize() {
		buttonList.clear();
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
		perRow = AEConfig.instance().getConfigManager().getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL ? 9 : 9 + (width - standardSize) / 18;
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
			xSize = standardSize + (perRow - 9) * 18;
		}
		else {
			xSize = standardSize;
		}

		ySize = magicNumber + rows * 18 + reservedSpace;
		final int unusedSpace = height - ySize;
		guiTop = (int) Math.floor(unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f));

		super.initGui();

		buttonList.add(clearBtn = new GuiImgButton(guiLeft + 134, guiTop + ySize - 160, Settings.ACTIONS, ActionItems.STASH));
		buttonList.add(trashBtn = new GuiTrashButton(guiLeft + 116, guiTop + ySize - 104));
		clearBtn.setHalfSize(true);
		if (customSortOrder) {
			getButtonPanel().addButton(SortByBox = new GuiImgButton(getButtonPanelXOffset(), getButtonPanelYOffset(), Settings.SORT_BY, configSrc.getSetting(Settings.SORT_BY)));
		}
		getButtonPanel().addButton(ViewBox = new GuiImgButton(getButtonPanelXOffset(), getButtonPanelYOffset(), Settings.VIEW_MODE, configSrc.getSetting(Settings.VIEW_MODE)));
		getButtonPanel().addButton(SortDirBox = new GuiImgButton(getButtonPanelXOffset(), getButtonPanelYOffset(), Settings.SORT_DIRECTION, configSrc.getSetting(Settings.SORT_DIRECTION)));
		getButtonPanel().addButton(searchBoxSettings = new GuiImgButton(getButtonPanelXOffset(), getButtonPanelYOffset(), Settings.SEARCH_MODE, AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE)));
		getButtonPanel().addButton(terminalStyleBox = new GuiImgButton(getButtonPanelXOffset(), getButtonPanelYOffset(), Settings.TERMINAL_STYLE, AEConfig.instance().getConfigManager().getSetting(Settings.TERMINAL_STYLE)));
		getButtonPanel().addButton(shiftCraftButton = new GuiImgButtonShiftCraft(getButtonPanelXOffset(), getButtonPanelYOffset(), containerWCT.getWirelessTerminal()));
		if (!WTApi.instance().getConfig().isOldInfinityMechanicEnabled() && !WTApi.instance().isWTCreative(getWirelessTerminal())) {
			getButtonPanel().addButton(autoConsumeBoostersBox = new GuiImgButtonBooster(getButtonPanelXOffset(), getButtonPanelYOffset(), containerWCT.getWirelessTerminal()));
		}
		if (containerWCT.getWirelessTerminal() != null && WCTUtils.isAnyWCT(containerWCT.getWirelessTerminal())) {
			getButtonPanel().addButton(magnetModeBox = new GuiImgButtonMagnetMode(getButtonPanelXOffset(), getButtonPanelYOffset(), containerWCT));
		}
		getButtonPanel().init(this);
		searchField = new GuiMETextField(fontRenderer, guiLeft + Math.max(80, offsetX), guiTop + 4, 90, 12);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setMaxStringLength(25);
		searchField.setTextColor(0xFFFFFF);
		searchField.setSelectionColor(0xFF99FF99);
		searchField.setVisible(true);

		buttonList.add(craftingStatusBtn = new GuiItemIconButton(guiLeft + 169, guiTop - 4, 2 + 11 * 16, GuiText.CraftingStatus.getLocal(), itemRender));
		buttonList.add(magnetGUIButton = new GuiMagnetButton(guiLeft + 157, guiTop + ySize - 115));
		craftingStatusBtn.setHideEdge(13);

		final Enum<?> searchModeSetting = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);
		final boolean isAutoFocus = SearchBoxMode.AUTOSEARCH == searchModeSetting || SearchBoxMode.JEI_AUTOSEARCH == searchModeSetting || SearchBoxMode.AUTOSEARCH_KEEP == searchModeSetting || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchModeSetting;
		final boolean isManualFocus = SearchBoxMode.MANUAL_SEARCH == searchModeSetting || SearchBoxMode.JEI_MANUAL_SEARCH == searchModeSetting || SearchBoxMode.MANUAL_SEARCH_KEEP == searchModeSetting || SearchBoxMode.JEI_MANUAL_SEARCH_KEEP == searchModeSetting;
		final boolean isKeepFilter = SearchBoxMode.AUTOSEARCH_KEEP == searchModeSetting || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchModeSetting || SearchBoxMode.MANUAL_SEARCH_KEEP == searchModeSetting || SearchBoxMode.JEI_MANUAL_SEARCH_KEEP == searchModeSetting;
		final boolean isJEIEnabled = SearchBoxMode.JEI_AUTOSEARCH == searchModeSetting || SearchBoxMode.JEI_MANUAL_SEARCH == searchModeSetting;

		searchField.setFocused(isAutoFocus);
		searchField.setCanLoseFocus(isManualFocus);

		if (isJEIEnabled) {
			memoryText = Integrations.jei().getSearchText();
		}

		if (isKeepFilter && memoryText != null && !memoryText.isEmpty()) {
			searchField.setText(memoryText);
			searchField.selectAll();
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
			halloween = new EntityWitherSkeleton(Minecraft.getMinecraft().world);
			halloween.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Blocks.LIT_PUMPKIN));
		}
	}

	int getMaxRows() {
		return AEConfig.instance().getConfigManager().getSetting(Settings.TERMINAL_STYLE) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
	}

	protected void repositionSlot(final AppEngSlot s) {
		s.yPos = s.getY() + ySize - 78 - 5;
	}

	@Override
	public void updateScreen() {
		repo.setPower(containerWCT.isPowered());
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

		if (!mc.player.isEntityAlive() || mc.player.isDead) {
			mc.player.closeScreen();
		}
		if (isHalloween && halloween != null) {
			if (!halloween.getHeldItemMainhand().isItemEqual(Minecraft.getMinecraft().player.getHeldItemMainhand())) {
				halloween.setHeldItem(EnumHand.MAIN_HAND, Minecraft.getMinecraft().player.getHeldItemMainhand());
			}
			if (!halloween.getHeldItemOffhand().isItemEqual(Minecraft.getMinecraft().player.getHeldItemOffhand())) {
				halloween.setHeldItem(EnumHand.OFF_HAND, Minecraft.getMinecraft().player.getHeldItemOffhand());
			}
		}
	}

	private boolean hasScreenResChanged() {
		if (currScreenWidth != mc.displayWidth || currScreenHeight != mc.displayHeight || isFullScreen != mc.isFullScreen()) {
			currScreenWidth = mc.displayWidth;
			currScreenHeight = mc.displayHeight;
			isFullScreen = mc.isFullScreen();
			return true;
		}
		return false;
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		xSize_lo = mouseX;
		ySize_lo = mouseY;

		final boolean hasClicked = Mouse.isButtonDown(0);
		if (hasClicked && getScrollBar() != null) {
			getScrollBar().click(this, mouseX - guiLeft, mouseY - guiTop);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);

	}

	@Override
	public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		final String s = "Terminal";
		mc.fontRenderer.drawString(s, 7, 5, 4210752);
		String warning = "";
		if (WTApi.instance().getConfig().isInfinityBoosterCardEnabled() && !WTApi.instance().getConfig().isOldInfinityMechanicEnabled()) {
			final int infinityEnergyAmount = WTApi.instance().getInfinityEnergy(getWirelessTerminal());
			if (WTApi.instance().hasInfiniteRange(getWirelessTerminal())) {
				if (!WTApi.instance().isInRangeOfWAP(getWirelessTerminal(), Minecraft.getMinecraft().player)) {
					if (infinityEnergyAmount < WTApi.instance().getConfig().getLowInfinityEnergyWarningAmount()) {
						warning = TextFormatting.RED + "" + I18n.format("tooltip.infinity_energy_low.desc");
					}
				}
			}
			//if (!WTApi.instance().isWTCreative(getWirelessTerminal()) && isPointInRegion(containerWCT.getBoosterSlot().xPos, containerWCT.getBoosterSlot().yPos, 16, 16, mouseX, mouseY) && EasyMappings.player().inventory.getItemStack().isEmpty()) {
			//final String amountColor = infinityEnergyAmount < WTApi.instance().getConfig().getLowInfinityEnergyWarningAmount() ? TextFormatting.RED.toString() : TextFormatting.GREEN.toString();
			//final String infinityEnergy = I18n.format("tooltip.infinity_energy.desc") + ": " + amountColor + "" + (isShiftKeyDown() ? infinityEnergyAmount : ItemStackSizeRenderer.getInstance().getConverter().toSlimReadableForm(infinityEnergyAmount)) + "" + TextFormatting.GRAY + " " + I18n.format("tooltip.units.desc");
			//drawTooltip(mouseX - offsetX, mouseY - offsetY, infinityEnergy);
			//}
		}
		mc.fontRenderer.drawString(I18n.format("container.inventory") + " " + warning, 7, ySize - 172 + 3, 4210752);
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		mc.getTextureManager().bindTexture(new ResourceLocation(ModGlobals.MODID, "textures/" + (Mods.BAUBLES.isLoaded() ? BG_TEXTURE_BAUBLES : BG_TEXTURE)));
		final int x_width = 199;
		boolean update = false;

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

		//draw view cells
		drawTexturedModalRect(offsetX - 40, offsetY + 16 + rows * 18 + lowerTextureOffset + 119, 213, 0, 43, 52);

		if (WTApi.instance().getConfig().isInfinityBoosterCardEnabled() && !WTApi.instance().isWTCreative(getWirelessTerminal())) {
			drawTexturedModalRect(guiLeft + 132, guiTop + rows * 18 + 83, 237, 237, 19, 19);
		}
		if(isHalloween)
		GuiInventory.drawEntityOnScreen(guiLeft + 51, guiTop + rows * 18 + (!isAltKeyDown() ? 98 : 94), 32, guiLeft + 51 - xSize_lo, guiTop + rows * 18 + 50 - ySize_lo, !isAltKeyDown() ? halloween : Minecraft.getMinecraft().player);
		else
		GuiInventory.drawEntityOnScreen(guiLeft + 51, guiTop + rows * 18 + 94, 32, guiLeft + 51 - xSize_lo, guiTop + rows * 18 + 50 - ySize_lo, Minecraft.getMinecraft().player);
		if (isHalloween && !isAltKeyDown()) {

			String name = "Happy Halloween!            ";
			final int idx = (int) (CLIENT_TICKS / 4 % name.length());
			name = (name + " " + name).substring(idx, idx + 10);

			final FontRenderer fr = RenderUtils.getFontRenderer();
			GlStateManager.disableDepth();
			fr.drawStringWithShadow(name, guiLeft + 52 - fr.getStringWidth(name) / 2, guiTop + rows * 18 + 90, 0xFFFFA00F);
			GlStateManager.enableDepth();
		}

		for (int i = 0; i < 4; i++) {
			if (myCurrentViewCells[i] != containerWCT.getCellViewSlot(i).getStack()) {
				update = true;
				myCurrentViewCells[i] = containerWCT.getCellViewSlot(i).getStack();
			}
		}

		if (magnetGUIButton != null) {
			magnetGUIButton.visible = containerWCT.getMagnetSlot().getHasStack();
		}
		if (magnetModeBox != null) {
			magnetModeBox.setVisibility(containerWCT.getMagnetSlot().getHasStack());
		}

		if (update) {
			repo.setViewCell(myCurrentViewCells);
		}

		if (searchField != null) {
			searchField.drawTextBox();
		}
	}

	@Override
	protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
		final Enum<?> searchMode = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);

		if (searchMode != SearchBoxMode.AUTOSEARCH && searchMode != SearchBoxMode.JEI_AUTOSEARCH) {
			searchField.mouseClicked(xCoord, yCoord, btn);
		}

		if (btn == 1 && searchField.isMouseIn(xCoord, yCoord)) {
			searchField.setText("");
			repo.setSearchString("");
			repo.updateView();
			setScrollBar();
		}

		super.mouseClicked(xCoord, yCoord, btn);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
		memoryText = searchField.getText();
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

	public GuiMETextField getSearchField() {
		return searchField;
	}

	@Override
	protected void keyTyped(final char character, final int key) throws IOException {
		if (!checkHotbarKeys(key)) {
			if (character == ' ' && searchField.getText().isEmpty()) {
				return;
			}
			if (ModKeybindings.openTerminal.getKeyCode() == key) {
				final Enum<?> searchMode = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);
				if ((searchMode == SearchBoxMode.MANUAL_SEARCH || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH) && !searchField.isFocused()) {
					Minecraft.getMinecraft().player.closeScreen();
				}
				else {
					if (isCtrlKeyDown()) {
						Minecraft.getMinecraft().player.closeScreen();
					}
				}
			}

			if (isTabKeyDown()) {
				if (!searchField.isFocused()) {
					searchField.setFocused(true);
				}
				if (!searchField.getText().isEmpty()) {
					searchField.selectAll();
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
	protected List<InternalSlotME> getMeSlots() {
		return meSlots;
	}

	@Override
	public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
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

}
