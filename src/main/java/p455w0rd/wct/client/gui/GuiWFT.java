package p455w0rd.wct.client.gui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Mouse;

import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.FluidRepo;
import appeng.client.me.InternalFluidSlotME;
import appeng.client.me.SlotFluidME;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.fluids.container.slots.IMEFluidSlot;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import p455w0rd.wct.client.gui.widgets.GuiScrollbar;
import p455w0rd.wct.client.gui.widgets.MEGuiTextField;
import p455w0rd.wct.client.render.StackSizeRenderer.ReadableNumberConverter;
import p455w0rd.wct.container.ContainerWFT;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModGlobals;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketInventoryAction;
import p455w0rd.wct.sync.packets.PacketValueConfig;
import p455w0rd.wct.util.WCTUtils;
import yalter.mousetweaks.api.MouseTweaksIgnore;

@MouseTweaksIgnore
public class GuiWFT extends WCTBaseGui implements ISortSource, IConfigManagerHost {

	private final List<SlotFluidME> meFluidSlots = new LinkedList<>();
	private final FluidRepo repo;
	private final IConfigManager configSrc;
	private final ContainerWFT container;
	private final int offsetX = 9;
	private int rows = 5;
	private int perRow = 9;
	ItemStack wirelessTerm;

	protected ITerminalHost terminal;

	private MEGuiTextField searchField;
	private GuiImgButton sortByBox;
	private GuiImgButton sortDirBox;

	public GuiWFT(Container container) {
		super(container);
		xSize = 185;
		ySize = 222;
		terminal = (ITerminalHost) ((ContainerWFT) container).getObject();
		wirelessTerm = ((ContainerWFT) container).getFluidTerminal();
		final GuiScrollbar scrollbar = new GuiScrollbar();
		this.setScrollBar(scrollbar);
		repo = new FluidRepo(scrollbar, this);
		configSrc = ((IConfigurableObject) inventorySlots).getConfigManager();
		(this.container = (ContainerWFT) inventorySlots).setGui(this);
	}

	public void setFluidTerminal(ItemStack stack) {
		wirelessTerm = stack;
	}

	@Override
	public void initGui() {
		mc.player.openContainer = inventorySlots;
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;

		searchField = new MEGuiTextField(fontRenderer, guiLeft + Math.max(79, offsetX - 1), guiTop + 4, 90, 12);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setMaxStringLength(25);
		searchField.setTextColor(0xFFFFFF);
		searchField.setSelectionColor(0xFF99FF99);
		searchField.setVisible(true);

		int offset = guiTop + 5;

		buttonList.add(sortByBox = new GuiImgButton(guiLeft - 18, offset, Settings.SORT_BY, configSrc.getSetting(Settings.SORT_BY)));
		offset += 20;

		buttonList.add(sortDirBox = new GuiImgButton(guiLeft - 18, offset, Settings.SORT_DIRECTION, configSrc.getSetting(Settings.SORT_DIRECTION)));

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < perRow; x++) {
				SlotFluidME slot = new SlotFluidME(new InternalFluidSlotME(repo, x + y * perRow, offsetX - 1 + x * 18, 18 + y * 18));
				getMeFluidSlots().add(slot);
				inventorySlots.inventorySlots.add(slot);
			}
		}
		this.setScrollBar();
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
		fontRenderer.drawString(getGuiDisplayName("Fluid Terminal"), 8, 6, 4210752);
		fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 100, 4210752);

		String warning = "";
		if (ModConfig.WCT_BOOSTER_ENABLED && !ModConfig.USE_OLD_INFINTY_MECHANIC) {
			int infinityEnergyAmount = WCTUtils.getInfinityEnergy(container.getFluidTerminal());
			if (WCTUtils.hasInfiniteRange(wirelessTerm)) {
				if (!WCTUtils.isInRangeOfWAP(wirelessTerm, WCTUtils.player())) {
					if (infinityEnergyAmount < ModConfig.INFINTY_ENERGY_LOW_WARNING_AMOUNT) {
						warning = TextFormatting.RED + "" + I18n.format("tooltip.infinity_energy_low.desc");
					}
				}
			}
			if (!WCTUtils.isWFTCreative(wirelessTerm) && isPointInRegion(container.getBoosterSlot().xPos, container.getBoosterSlot().yPos, 16, 16, mouseX, mouseY) && mc.player.inventory.getItemStack().isEmpty()) {
				String amountColor = infinityEnergyAmount < ModConfig.INFINTY_ENERGY_LOW_WARNING_AMOUNT ? TextFormatting.RED.toString() : TextFormatting.GREEN.toString();
				String infinityEnergy = I18n.format("tooltip.infinity_energy.desc") + ": " + amountColor + "" + (isShiftKeyDown() ? infinityEnergyAmount : ReadableNumberConverter.INSTANCE.toSlimReadableForm(infinityEnergyAmount)) + "" + TextFormatting.GRAY + " " + I18n.format("tooltip.units.desc");
				drawTooltip(mouseX - offsetX, mouseY - offsetY, infinityEnergy);
			}
		}
		//if (!warning.isEmpty()) {
		//	GlStateManager.enableBlend();
		//	GlStateManager.color(1, 1, 1, 1);
		mc.fontRenderer.drawString(warning, 8, ySize - 111, 4210752);
		//}
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
		final ResourceLocation loc = new ResourceLocation(ModGlobals.MODID, "textures/" + getBackground());
		mc.getTextureManager().bindTexture(loc);

		final int x_width = 197;
		this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

		for (int x = 0; x < 5; x++) {
			this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
		}

		this.drawTexturedModalRect(offsetX, offsetY + 16 + 6 * 18 - 18, 0, 106 - 18 - 18, x_width, 8);
		this.drawTexturedModalRect(offsetX, offsetY + 16 + 6 * 18 - 10, 0, 106 - 18 - 18 + 8, x_width, 7);
		this.drawTexturedModalRect(offsetX, offsetY + 16 + 6 * 18 - 3, 0, 106 - 18 - 18 + 8, x_width, 7);
		//this.drawTexturedModalRect(offsetX, offsetY + 16 + 6 * 18 + 4, 0, 106 - 18 - 18 + 8, x_width, 3);

		this.drawTexturedModalRect(offsetX, offsetY + 16 + 6 * 18, 0, 106 - 18 - 18 + 8, x_width, 99 + 77);
		if (ModConfig.WCT_BOOSTER_ENABLED && !WCTUtils.isWFTCreative(WCTUtils.getFluidTerm(Minecraft.getMinecraft().player.inventory))) {
			drawTexturedModalRect(guiLeft + 150, (guiTop + rows * 18) + 18, 237, 237, 19, 19);
		}
		if (searchField != null) {
			searchField.drawTextBox();
		}
	}

	@Override
	public void updateScreen() {
		repo.setPower(container.isPowered());
		super.updateScreen();
	}

	@Override
	protected void renderHoveredToolTip(int mouseX, int mouseY) {
		final Slot slot = getSlot(mouseX, mouseY);

		if (slot != null && slot instanceof IMEFluidSlot && slot.isEnabled()) {
			final IMEFluidSlot fluidSlot = (IMEFluidSlot) slot;

			if (fluidSlot.getAEFluidStack() != null && fluidSlot.shouldRenderAsFluid()) {
				final IAEFluidStack fluidStack = fluidSlot.getAEFluidStack();
				final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(fluidStack.getStackSize() / 1000.0) + " B";

				final String modName = "" + TextFormatting.BLUE + TextFormatting.ITALIC + Loader.instance().getIndexedModList().get(Platform.getModId(fluidStack)).getName();

				final List<String> list = new ArrayList<>();

				list.add(fluidStack.getFluidStack().getLocalizedName());
				list.add(formattedAmount);
				list.add(modName);

				this.drawHoveringText(list, mouseX, mouseY);

				return;
			}
		}
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void actionPerformed(GuiButton btn) throws IOException {
		if (btn instanceof GuiImgButton) {
			final boolean backwards = Mouse.isButtonDown(1);
			final GuiImgButton iBtn = (GuiImgButton) btn;

			if (iBtn.getSetting() != Settings.ACTIONS) {
				final Enum cv = iBtn.getCurrentValue();
				final Enum next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());

				try {
					ModNetworking.instance().sendToServer(new PacketValueConfig(iBtn.getSetting().name(), next.name()));
				}
				catch (final IOException e) {
					AELog.debug(e);
				}

				iBtn.set(next);
			}
		}
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
		if (slot instanceof SlotFluidME) {
			final SlotFluidME meSlot = (SlotFluidME) slot;

			if (clickType == ClickType.PICKUP) {
				// TODO: Allow more options
				if (mouseButton == 0 && meSlot.getHasStack()) {
					container.setTargetStack(meSlot.getAEFluidStack());
					AELog.debug("mouse0 GUI STACK SIZE %s", meSlot.getAEFluidStack().getStackSize());
					ModNetworking.instance().sendToServer(new PacketInventoryAction(InventoryAction.FILL_ITEM, slot.slotNumber, 0));
				}
				else {
					container.setTargetStack(meSlot.getAEFluidStack());
					if (meSlot.getAEFluidStack() != null) {
						AELog.debug("mouse1 GUI STACK SIZE %s", meSlot.getAEFluidStack().getStackSize());
					}
					ModNetworking.instance().sendToServer(new PacketInventoryAction(InventoryAction.EMPTY_ITEM, slot.slotNumber, 0));
				}
			}
			return;
		}
		super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
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
				this.setScrollBar();
			}
			else {
				super.keyTyped(character, key);
			}
		}
	}

	@Override
	protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
		searchField.mouseClicked(xCoord, yCoord, btn);

		if (btn == 1 && searchField.isMouseIn(xCoord, yCoord)) {
			searchField.setText("");
			repo.setSearchString("");
			repo.updateView();
			this.setScrollBar();
		}

		super.mouseClicked(xCoord, yCoord, btn);
	}

	public void postUpdate(final List<IAEFluidStack> list) {
		for (final IAEFluidStack is : list) {
			repo.postUpdate(is);
		}

		repo.updateView();
		this.setScrollBar();
	}

	private void setScrollBar() {
		getScrollBar().setTop(18).setLeft(175).setHeight(rows * 18 - 2);
		getScrollBar().setRange(0, (repo.size() + perRow - 1) / perRow - rows, Math.max(1, rows / 6));
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

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
		if (sortByBox != null) {
			sortByBox.set(configSrc.getSetting(Settings.SORT_BY));
		}

		if (sortDirBox != null) {
			sortDirBox.set(configSrc.getSetting(Settings.SORT_DIRECTION));
		}

		repo.updateView();
	}

	protected List<SlotFluidME> getMeFluidSlots() {
		return meFluidSlots;
	}

	@Override
	protected boolean isPowered() {
		return repo.hasPower();
	}

	protected String getBackground() {
		return "gui/fluid.png";
	}

}
