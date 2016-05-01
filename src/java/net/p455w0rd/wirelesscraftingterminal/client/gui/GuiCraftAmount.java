package net.p455w0rd.wirelesscraftingterminal.client.gui;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.helpers.Reflected;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.GuiTabButton;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftAmount;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketCraftRequest;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class GuiCraftAmount extends WCTBaseGui {
	private GuiNumberBox amountToCraft;
	private GuiTabButton originalGuiBtn;

	private GuiButton next;

	private GuiButton plus1;
	private GuiButton plus10;
	private GuiButton plus100;
	private GuiButton plus1000;
	private GuiButton minus1;
	private GuiButton minus10;
	private GuiButton minus100;
	private GuiButton minus1000;

	private ItemStack myIcon;

	private int originalGui;

	@Reflected
	public GuiCraftAmount(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
		super(new ContainerCraftAmount(inventoryPlayer, te));
		ItemStack is = new ItemStack(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem());
		((ItemWirelessCraftingTerminal) is.getItem()).injectAEPower(is, 6400001);
		myIcon = is;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public void initGui() {
		super.initGui();

		final int a = AEConfig.instance.craftItemsByStackAmounts(0);
		final int b = AEConfig.instance.craftItemsByStackAmounts(1);
		final int c = AEConfig.instance.craftItemsByStackAmounts(2);
		final int d = AEConfig.instance.craftItemsByStackAmounts(3);

		this.buttonList.add(this.plus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 26, 22, 20, "+" + a));
		this.buttonList.add(this.plus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 26, 28, 20, "+" + b));
		this.buttonList.add(this.plus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 26, 32, 20, "+" + c));
		this.buttonList.add(this.plus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 26, 38, 20, "+" + d));

		this.buttonList.add(this.minus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 75, 22, 20, "-" + a));
		this.buttonList.add(this.minus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 75, 28, 20, "-" + b));
		this.buttonList.add(this.minus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 75, 32, 20, "-" + c));
		this.buttonList.add(this.minus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 75, 38, 20, "-" + d));

		this.buttonList.add(this.next = new GuiButton(0, this.guiLeft + 128, this.guiTop + 51, 38, 20, GuiText.Next.getLocal()));
		final Object target = ((ContainerCraftAmount) this.inventorySlots).getTarget();
		final IDefinitions definitions = AEApi.instance().definitions();
		final IParts parts = definitions.parts();

		if (target instanceof WirelessTerminalGuiObject) {
			this.originalGui = Reference.GUI_WCT;
		}

		if (this.originalGui > 0 && myIcon != null) {
			this.buttonList.add(this.originalGuiBtn = new GuiTabButton(this.guiLeft + 154, this.guiTop - 4, myIcon, myIcon.getDisplayName(), itemRender));
		}

		this.amountToCraft = new GuiNumberBox(this.fontRendererObj, this.guiLeft + 62, this.guiTop + 57, 59, this.fontRendererObj.FONT_HEIGHT, Integer.class);
		this.amountToCraft.setEnableBackgroundDrawing(false);
		this.amountToCraft.setMaxStringLength(16);
		this.amountToCraft.setTextColor(0xFFFFFF);
		this.amountToCraft.setVisible(true);
		this.amountToCraft.setFocused(true);
		this.amountToCraft.setText("1");
	}

	@Override
	public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		this.fontRendererObj.drawString(GuiText.SelectAmount.getLocal(), 8, 6, 4210752);
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		this.next.displayString = isShiftKeyDown() ? GuiText.Start.getLocal() : GuiText.Next.getLocal();

		this.bindTexture("appliedenergistics2", "guis/craftAmt.png");
		this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);

		try {
			Long.parseLong(this.amountToCraft.getText());
			this.next.enabled = this.amountToCraft.getText().length() > 0;
		}
		catch (final NumberFormatException e) {
			this.next.enabled = false;
		}

		this.amountToCraft.drawTextBox();
	}

	@Override
	protected void keyTyped(final char character, final int key) {
		if (!this.checkHotbarKeys(key)) {
			if (key == 28) {
				this.actionPerformed(this.next);
			}
			if ((key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit(character)) && this.amountToCraft.textboxKeyTyped(character, key)) {
				try {
					String out = this.amountToCraft.getText();

					boolean fixed = false;
					while (out.startsWith("0") && out.length() > 1) {
						out = out.substring(1);
						fixed = true;
					}

					if (fixed) {
						this.amountToCraft.setText(out);
					}

					if (out.isEmpty()) {
						out = "0";
					}

					final long result = Long.parseLong(out);
					if (result < 0) {
						this.amountToCraft.setText("1");
					}
				}
				catch (final NumberFormatException e) {
					// :P
				}
			}
			else {
				super.keyTyped(character, key);
			}
		}
	}

	@Override
	protected void actionPerformed(final GuiButton btn) {
		super.actionPerformed(btn);

		try {

			if (btn == this.originalGuiBtn) {
				NetworkHandler.instance.sendToServer(new PacketSwitchGuis(this.originalGui));
			}

			if (btn == this.next) {
				NetworkHandler.instance.sendToServer(new PacketCraftRequest(Integer.parseInt(this.amountToCraft.getText()), isShiftKeyDown()));
			}
		}
		catch (final NumberFormatException e) {
			// nope..
			this.amountToCraft.setText("1");
		}

		final boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
		final boolean isMinus = btn == this.minus1 || btn == this.minus10 || btn == this.minus100 || btn == this.minus1000;

		if (isPlus || isMinus) {
			this.addQty(this.getQty(btn));
		}
	}

	private void addQty(final int i) {
		try {
			String out = this.amountToCraft.getText();

			boolean fixed = false;
			while (out.startsWith("0") && out.length() > 1) {
				out = out.substring(1);
				fixed = true;
			}

			if (fixed) {
				this.amountToCraft.setText(out);
			}

			if (out.isEmpty()) {
				out = "0";
			}

			long result = Integer.parseInt(out);

			if (result == 1 && i > 1) {
				result = 0;
			}

			result += i;
			if (result < 1) {
				result = 1;
			}

			out = Long.toString(result);
			Integer.parseInt(out);
			this.amountToCraft.setText(out);
		}
		catch (final NumberFormatException e) {
			// :P
		}
	}

	protected String getBackground() {
		return "guis/craftAmt.png";
	}
}
