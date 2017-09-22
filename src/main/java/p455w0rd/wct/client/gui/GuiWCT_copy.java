package p455w0rd.wct.client.gui;

import net.minecraft.inventory.Container;

public class GuiWCT_copy /* extends GuiContainer implements ISortSource, IConfigManagerHost */ {
	/*
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
		private static final String bgTexturePath = "gui/crafting.png";
		private final ContainerWirelessCraftingTerminal containerWCT;
		private boolean isFullScreen, init = true, reInit, wasResized = false;
		private static boolean switchingGuis;
		private final List<InternalSlotME> meSlots = new LinkedList<InternalSlotME>();
		private final Set<Slot> drag_click = new HashSet<Slot>();
		private final WCTRenderItem aeRenderItem = new WCTRenderItem(Minecraft.getMinecraft().renderEngine, Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager(), Minecraft.getMinecraft().getItemColors(), false);
		private final StackSizeRenderer stackSizeRenderer = new StackSizeRenderer();
		private GuiScrollbar scrollBar = null;
		private boolean disableShiftClick = false;
		private Stopwatch dbl_clickTimer = Stopwatch.createStarted();
		private ItemStack dbl_whichItem;
		private Slot bl_clicked;
		private boolean subGui;
		public static int craftingGridOffsetX = 80;
		public static int craftingGridOffsetY;
		private static String memoryText = "";
		private final ItemRepo repo;
		private final int offsetX = 8;
		private final IConfigManager configSrc;
		private GuiTabButton craftingStatusBtn;
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
		private boolean isNEIEnabled;
		private boolean wasTextboxFocused = false;
		private int screenResTicks = 0;
		private int reservedSpace = 0;
		private int maxRows = Integer.MAX_VALUE;
		private int standardSize;
		private final int lowerTextureOffset = 0;
		private int rows = 0;
	*/
	public GuiWCT_copy(Container container) {
		/*
		super(container);
		xSize = GUI_WIDTH;
		ySize = GUI_HEIGHT;
		standardSize = xSize;
		setReservedSpace(73);
		containerWCT = (ContainerWirelessCraftingTerminal) container;
		scrollBar = new GuiScrollbar();
		subGui = switchingGuis;
		switchingGuis = false;
		setScrollBar(scrollBar);
		repo = new ItemRepo(scrollBar, this);
		configSrc = containerWCT.getConfigManager();
		devicePowered = containerWCT.isPowered();
		((ContainerWirelessCraftingTerminal) inventorySlots).setGui(this);
		*/
	}
	/*
		public void postUpdate(final List<IAEItemStack> list) {
			for (final IAEItemStack is : list) {
				repo.postUpdate(is);
			}
			repo.updateView();
			setScrollBar();
		}

		private void setScrollBar() {
			getScrollBar().setTop(18).setLeft(174).setHeight(rows * 18 - 2);
			getScrollBar().setRange(0, (repo.size() + perRow - 1) / perRow - rows, Math.max(1, rows / 6));
		}

		protected void setScrollBar(final GuiScrollbar myScrollBar) {
			scrollBar = myScrollBar;
		}

		@Override
		protected void actionPerformed(final GuiButton btn) {
			if (btn == craftingStatusBtn) {
				NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiHandler.GUI_CRAFTING_STATUS));
			}

			if (btn instanceof GuiImgButton) {
				final boolean backwards = Mouse.isButtonDown(1);

				final GuiImgButton iBtn = (GuiImgButton) btn;
				if (iBtn.getSetting() != Settings.ACTIONS) {
					final Enum<?> cv = iBtn.getCurrentValue();
					final Enum<?> next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());

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
							containerWCT.trashSlot.clearStack();
							NetworkHandler.instance().sendToServer(new PacketEmptyTrash());
						}
					}
				}
			}
		}

		private void reinitalize() {
			buttonList.clear();
			initGui();
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
			isNEIEnabled = Loader.isModLoaded("JEI");
			int top = isNEIEnabled ? 22 : 0;
			final int magicNumber = 114 + 1;
			final int extraSpace = height - magicNumber - 0 - top - reservedSpace;
			rows = (int) Math.floor(extraSpace / 18);
			if (rows > maxRows) {
				top += (rows - maxRows) * 18 / 2;
				rows = maxRows;
			}

			if (isNEIEnabled) {
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
			super.initGui();

			ySize = magicNumber + rows * 18 + reservedSpace;
			final int unusedSpace = height - ySize;
			guiTop = (int) Math.floor(unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f));
			int offset = guiTop + 8;

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

			buttonList.clear();
			buttonList.add(clearBtn = new GuiImgButton(guiLeft + 134, guiTop + ySize - 160, Settings.ACTIONS, ActionItems.STASH));
			buttonList.add(trashBtn = new GuiTrashButton(guiLeft + 98, guiTop + ySize - 104));
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
			craftingStatusBtn.setHideEdge(13);

			final Enum<?> setting = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);
			searchField.setFocused(SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.JEI_AUTOSEARCH == setting);

			if (isSubGui()) {
				if (isSubGui()) {
					searchField.setText(memoryText);
					repo.setSearchString(memoryText);
				}
				repo.updateView();
				setScrollBar();
			}

			craftingGridOffsetX = Integer.MAX_VALUE;
			craftingGridOffsetY = Integer.MAX_VALUE;

			for (final Object s : inventorySlots.inventorySlots) {
				if (s instanceof AppEngSlot) {
					if (((Slot) s).xDisplayPosition < 197) {
						repositionSlot((AppEngSlot) s);
					}
				}

				if (s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix) {
					final Slot g = (Slot) s;
					if (g.xDisplayPosition > 0 && g.yDisplayPosition > 0) {
						craftingGridOffsetX = Math.min(craftingGridOffsetX, g.xDisplayPosition);
						craftingGridOffsetY = Math.min(craftingGridOffsetY, g.yDisplayPosition);
					}
				}
			}
		}

		private List<Slot> getInventorySlots() {
			return inventorySlots.inventorySlots;
		}

		int getMaxRows() {
			return AEConfig.instance().getConfigManager().getSetting(Settings.TERMINAL_STYLE) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
		}

		protected void repositionSlot(final AppEngSlot s) {
			s.yDisplayPosition = s.getY() + ySize - 78 - 5;
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
			if (!mc.thePlayer.isEntityAlive() || mc.thePlayer.isDead) {
				mc.thePlayer.closeScreen();
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
			super.drawScreen(mouseX, mouseY, btn);
			xSize_lo = mouseX;
			ySize_lo = mouseY;

			final boolean hasClicked = Mouse.isButtonDown(0);
			if (hasClicked && scrollBar != null) {
				scrollBar.click(this, mouseX - guiLeft, mouseY - guiTop);
			}

			for (final Object c : buttonList) {
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
								drawTooltip(x + 11, y + 4, msg);
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

			this.drawHoveringText(lines, x, y, fontRendererObj);
		}

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

		public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
			this.bindTexture(bgTexturePath);
			final int x_width = 199;

			this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

			for (int x = 0; x < rows; x++) {
				this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
			}

			this.drawTexturedModalRect(offsetX, offsetY + 16 + rows * 18 + lowerTextureOffset, 0, 106 - 18 - 18, x_width, 99 + reservedSpace - lowerTextureOffset);

			if (ModConfig.WCT_BOOSTER_ENABLED) {
				this.drawTexturedModalRect(guiLeft + 132, (guiTop + rows * 18) + 83, 237, 237, 19, 19);
			}
			GuiInventory.drawEntityOnScreen(guiLeft + 51, (guiTop + rows * 18) + 94, 32, guiLeft + 51 - xSize_lo, (guiTop + rows * 18) + 50 - ySize_lo, mc.thePlayer);

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
			final ItemStack itemstack = mc.thePlayer.inventory.getItemStack();

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
			final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

			if (slot instanceof SlotFake) {
				final InventoryAction action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;

				if (drag_click.size() > 1) {
					return;
				}

				final PacketInventoryAction p = new PacketInventoryAction(action, slotIdx, 0);
				NetworkHandler.instance().sendToServer(p);

				return;
			}

			else if (slot instanceof SlotCraftingTerm) {
				if (mouseButton == 6) {
					return; // prevent weird double clicks..
				}

				InventoryAction action = null;
				if (mouseButton == 1) {
					action = InventoryAction.CRAFT_SHIFT;
				}
				else {
					action = clickType == ClickType.QUICK_CRAFT ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
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

					((ContainerWirelessCraftingTerminal) inventorySlots).setTargetStack(stack);
					final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, slotNum, 0);
					NetworkHandler.instance().sendToServer(p);
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
					NetworkHandler.instance().sendToServer(p);
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

					if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && stack.getStackSize() == 0 && player.inventory.getItemStack() == null) {
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
					((ContainerWirelessCraftingTerminal) inventorySlots).setTargetStack(stack);
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
						if (inventorySlot != null && inventorySlot.canTakeStack(mc.thePlayer) && inventorySlot.getHasStack() && inventorySlot.inventory == slot.inventory && Container.canAddItemToSlot(inventorySlot, dbl_whichItem, true)) {
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
			final Slot theSlot = getSlotUnderMouse();

			if (mc.thePlayer.inventory.getItemStack() == null && theSlot != null) {
				for (int j = 0; j < 9; ++j) {
					if (keyCode == mc.gameSettings.keyBindsHotbar[j].getKeyCode()) {
						final List<Slot> slots = getInventorySlots();
						for (final Slot s : slots) {
							if (s.getSlotIndex() == j && s.inventory == ((ContainerWirelessCraftingTerminal) inventorySlots).getPlayerInv()) {
								if (!s.canTakeStack(((ContainerWirelessCraftingTerminal) inventorySlots).getPlayerInv().player)) {
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
								if (s.getSlotIndex() == j && s.inventory == ((ContainerWirelessCraftingTerminal) inventorySlots).getPlayerInv()) {
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
			subGui = true; // in case the gui is reopened later ( i'm looking
							// at you NEI )
			Keyboard.enableRepeatEvents(false);
			if (searchField.getText() != null) {
				memoryText = searchField.getText();
			}
		}

		protected Slot getSlot(final int mouseX, final int mouseY) {
			final List<Slot> slots = getInventorySlots();
			for (final Slot slot : slots) {
				// isPointInRegion
				if (isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY)) {
					return slot;
				}
			}

			return null;
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
					((ContainerWirelessCraftingTerminal) inventorySlots).setTargetStack(item);
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

		protected void drawItem(final int x, final int y, final ItemStack is) {
			zLevel = 100.0F;
			itemRender.zLevel = 100.0F;

			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableGUIStandardItemLighting();
			itemRender.renderItemAndEffectIntoGUI(is, x, y);
			GL11.glPopAttrib();

			itemRender.zLevel = 0.0F;
			zLevel = 0.0F;
		}

		protected String getGuiDisplayName(final String in) {
			return hasCustomInventoryName() ? getInventoryName() : in;
		}

		private boolean hasCustomInventoryName() {
			if (inventorySlots instanceof ContainerWirelessCraftingTerminal) {
				return ((ContainerWirelessCraftingTerminal) inventorySlots).getCustomName() != null;
			}
			return false;
		}

		private String getInventoryName() {
			return ((ContainerWirelessCraftingTerminal) inventorySlots).getCustomName();
		}

		@Override
		public void drawSlot(Slot s) {
			if (s instanceof SlotME) {

				try {
					zLevel = 100.0F;
					itemRender.zLevel = 100.0F;

					if (!isPowered()) {
						drawRect(s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66111111);
					}

					zLevel = 0.0F;
					itemRender.zLevel = 0.0F;

					// Annoying but easier than trying to splice into render item
					super.drawSlot(new SlotSingleItem(s));

					stackSizeRenderer.renderStackSize(fontRendererObj, ((SlotME) s).getAEStack(), s.getStack(), s.xDisplayPosition, s.yDisplayPosition);

				}
				catch (final Exception err) {
					AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err.toString());
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

							try {
								final int uv_y = (int) Math.floor(aes.getIcon() / 16);
								final int uv_x = aes.getIcon() - uv_y * 16;

								GlStateManager.enableBlend();
								GlStateManager.disableLighting();
								GlStateManager.enableTexture2D();
								GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
								GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
								final float par1 = aes.xDisplayPosition;
								final float par2 = aes.yDisplayPosition;
								final float par3 = uv_x * 16;
								final float par4 = uv_y * 16;

								final Tessellator tessellator = Tessellator.getInstance();
								final VertexBuffer vb = tessellator.getBuffer();

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
						}
					}

					if (is != null && s instanceof AppEngSlot) {
						if (((AppEngSlot) s).getIsValid() == hasCalculatedValidness.NotAvailable) {
							boolean isValid = s.isItemValid(is) || s instanceof SlotOutput || s instanceof AppEngCraftingSlot || s instanceof SlotDisabled || s instanceof SlotInaccessible || s instanceof SlotFake || s instanceof SlotRestrictedInput || s instanceof SlotDisconnected;
							if (isValid && s instanceof SlotRestrictedInput) {
								try {
									isValid = ((SlotRestrictedInput) s).isValid(is, mc.theWorld);
								}
								catch (final Exception err) {
									AELog.debug(err);
								}
							}
							((AppEngSlot) s).setIsValid(isValid ? hasCalculatedValidness.Valid : hasCalculatedValidness.Invalid);
						}

						if (((AppEngSlot) s).getIsValid() == hasCalculatedValidness.Invalid) {
							zLevel = 100.0F;
							itemRender.zLevel = 100.0F;

							GlStateManager.disableLighting();
							drawRect(s.xDisplayPosition, s.yDisplayPosition, 16 + s.xDisplayPosition, 16 + s.yDisplayPosition, 0x66ff6666);
							GlStateManager.enableLighting();

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
					AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err.toString());
				}
			}
			// do the usual for non-ME Slots.
			super.drawSlot(s);
		}

		@Override
		protected void keyTyped(final char character, final int key) throws IOException {
			if (!checkHotbarKeys(key)) {
				if (character == ' ' && searchField.getText().isEmpty()) {
					return;
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

		protected boolean isPowered() {
			return repo.hasPower();
		}

		public void bindTexture(final String file) {
			final ResourceLocation loc = new ResourceLocation(Globals.MODID, "textures/" + file);
			mc.getTextureManager().bindTexture(loc);
		}

		protected GuiScrollbar getScrollBar() {
			return scrollBar;
		}

		protected List<InternalSlotME> getMeSlots() {
			return meSlots;
		}

		public static final synchronized boolean isSwitchingGuis() {
			return switchingGuis;
		}

		public static final synchronized void setSwitchingGuis(final boolean switchingGuis) {
			GuiWCT.switchingGuis = switchingGuis;
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
					final List<String> currentToolTip = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

					if (myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged())) {
						currentToolTip.add("Items Stored: " + NumberFormat.getNumberInstance(Locale.US).format(myStack.getStackSize()));
					}

					if (myStack.getCountRequestable() > 0) {
						currentToolTip.add("Items Requestable: " + NumberFormat.getNumberInstance(Locale.US).format(myStack.getCountRequestable()));
					}

					drawTooltip(x, y, join(currentToolTip, "\n"));
				}
				else if (stack.stackSize > BigNumber) {
					final List<String> var4 = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
					var4.add("Items Stored: " + NumberFormat.getNumberInstance(Locale.US).format(stack.stackSize));
					drawTooltip(x, y, join(var4, "\n"));
					return;
				}
			}
			super.renderToolTip(stack, x, y);
			// super.drawItemStackTooltip( stack, x, y );
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
	*/
}
