/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Joiner;

import appeng.api.AEApi;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.client.gui.widgets.GuiScrollbar;
import p455w0rd.wct.container.ContainerCraftingCPU;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketValueConfig;

public class GuiCraftingCPU extends WCTBaseGui implements ISortSource {
	private static final int GUI_HEIGHT = 184;
	private static final int GUI_WIDTH = 238;

	private static final int DISPLAYED_ROWS = 6;

	private static final int TEXT_COLOR = 0x404040;
	private static final int BACKGROUND_ALPHA = 0x5A000000;

	private static final int SECTION_LENGTH = 67;

	private static final int SCROLLBAR_TOP = 19;
	private static final int SCROLLBAR_LEFT = 218;
	private static final int SCROLLBAR_HEIGHT = 137;

	private static final int CANCEL_LEFT_OFFSET = 163;
	private static final int CANCEL_TOP_OFFSET = 25;
	private static final int CANCEL_HEIGHT = 20;
	private static final int CANCEL_WIDTH = 50;

	private static final int TITLE_TOP_OFFSET = 7;
	private static final int TITLE_LEFT_OFFSET = 8;

	private static final int ITEMSTACK_LEFT_OFFSET = 9;
	private static final int ITEMSTACK_TOP_OFFSET = 22;

	private final ContainerCraftingCPU craftingCpu;

	private IItemList<IAEItemStack> storage = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
	private IItemList<IAEItemStack> active = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
	private IItemList<IAEItemStack> pending = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();

	private List<IAEItemStack> visual = new ArrayList<IAEItemStack>();
	private GuiButton cancel;
	private int tooltip = -1;

	public GuiCraftingCPU(final InventoryPlayer inventoryPlayer, final Object te) {
		this(new ContainerCraftingCPU(inventoryPlayer, te));
	}

	protected GuiCraftingCPU(final ContainerCraftingCPU container) {
		super(container);
		craftingCpu = container;
		ySize = GUI_HEIGHT;
		xSize = GUI_WIDTH;

		final GuiScrollbar scrollbar = new GuiScrollbar();
		this.setScrollBar(scrollbar);
	}

	public void clearItems() {
		storage = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
		active = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
		pending = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
		visual = new ArrayList<IAEItemStack>();
	}

	@Override
	protected void actionPerformed(final GuiButton btn) throws IOException {
		super.actionPerformed(btn);

		if (cancel == btn) {
			try {
				ModNetworking.instance().sendToServer(new PacketValueConfig("TileCrafting.Cancel", "Cancel"));
			}
			catch (final IOException e) {
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.setScrollBar();
		cancel = new GuiButton(0, guiLeft + CANCEL_LEFT_OFFSET, guiTop + ySize - CANCEL_TOP_OFFSET, CANCEL_WIDTH, CANCEL_HEIGHT, GuiText.Cancel.getLocal());
		buttonList.add(cancel);
	}

	private void setScrollBar() {
		final int size = visual.size();

		getScrollBar().setTop(SCROLLBAR_TOP).setLeft(SCROLLBAR_LEFT).setHeight(SCROLLBAR_HEIGHT);
		getScrollBar().setRange(0, (size + 2) / 3 - DISPLAYED_ROWS, 1);
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float btn) {
		cancel.enabled = !visual.isEmpty();

		final int gx = (width - xSize) / 2;
		final int gy = (height - ySize) / 2;

		tooltip = -1;

		final int offY = 23;
		int y = 0;
		int x = 0;
		for (int z = 0; z <= 4 * 5; z++) {
			final int minX = gx + 9 + x * 67;
			final int minY = gy + 22 + y * offY;

			if (minX < mouseX && minX + 67 > mouseX) {
				if (minY < mouseY && minY + offY - 2 > mouseY) {
					tooltip = z;
					break;
				}
			}

			x++;

			if (x > 2) {
				y++;
				x = 0;
			}
		}

		super.drawScreen(mouseX, mouseY, btn);
	}

	@Override
	public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		String title = getGuiDisplayName(GuiText.CraftingStatus.getLocal());

		if (craftingCpu.getEstimatedTime() > 0 && !visual.isEmpty()) {
			final long etaInMilliseconds = TimeUnit.MILLISECONDS.convert(craftingCpu.getEstimatedTime(), TimeUnit.NANOSECONDS);
			final String etaTimeText = DurationFormatUtils.formatDuration(etaInMilliseconds, GuiText.ETAFormat.getLocal());
			title += " - " + etaTimeText;
		}

		fontRenderer.drawString(title, TITLE_LEFT_OFFSET, TITLE_TOP_OFFSET, TEXT_COLOR);

		int x = 0;
		int y = 0;
		final int viewStart = getScrollBar().getCurrentScroll() * 3;
		final int viewEnd = viewStart + 3 * 6;

		String dspToolTip = "";
		final List<String> lineList = new LinkedList<String>();
		int toolPosX = 0;
		int toolPosY = 0;

		final int offY = 23;

		final ReadableNumberConverter converter = ReadableNumberConverter.INSTANCE;
		for (int z = viewStart; z < Math.min(viewEnd, visual.size()); z++) {
			final IAEItemStack refStack = visual.get(z);// repo.getReferenceItem( z );
			if (refStack != null) {
				GL11.glPushMatrix();
				GL11.glScaled(0.5, 0.5, 0.5);

				final IAEItemStack stored = storage.findPrecise(refStack);
				final IAEItemStack activeStack = active.findPrecise(refStack);
				final IAEItemStack pendingStack = pending.findPrecise(refStack);

				int lines = 0;

				if (stored != null && stored.getStackSize() > 0) {
					lines++;
				}
				boolean active = false;
				if (activeStack != null && activeStack.getStackSize() > 0) {
					lines++;
					active = true;
				}
				boolean scheduled = false;
				if (pendingStack != null && pendingStack.getStackSize() > 0) {
					lines++;
					scheduled = true;
				}

				if (AEConfig.instance().isUseColoredCraftingStatus() && (active || scheduled)) {
					final int bgColor = (active ? AEColor.GREEN.blackVariant : AEColor.YELLOW.blackVariant) | BACKGROUND_ALPHA;
					final int startX = (x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET) * 2;
					final int startY = ((y * offY + ITEMSTACK_TOP_OFFSET) - 3) * 2;
					drawRect(startX, startY, startX + (SECTION_LENGTH * 2), startY + (offY * 2) - 2, bgColor);
				}

				final int negY = ((lines - 1) * 5) / 2;
				int downY = 0;

				if (stored != null && stored.getStackSize() > 0) {
					final String str = GuiText.Stored.getLocal() + ": " + converter.toWideReadableForm(stored.getStackSize());
					final int w = 4 + fontRenderer.getStringWidth(str);
					fontRenderer.drawString(str, (int) ((x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19 - (w * 0.5)) * 2), (y * offY + ITEMSTACK_TOP_OFFSET + 6 - negY + downY) * 2, TEXT_COLOR);

					if (tooltip == z - viewStart) {
						lineList.add(GuiText.Stored.getLocal() + ": " + Long.toString(stored.getStackSize()));
					}

					downY += 5;
				}

				if (activeStack != null && activeStack.getStackSize() > 0) {
					final String str = GuiText.Crafting.getLocal() + ": " + converter.toWideReadableForm(activeStack.getStackSize());
					final int w = 4 + fontRenderer.getStringWidth(str);

					fontRenderer.drawString(str, (int) ((x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19 - (w * 0.5)) * 2), (y * offY + ITEMSTACK_TOP_OFFSET + 6 - negY + downY) * 2, TEXT_COLOR);

					if (tooltip == z - viewStart) {
						lineList.add(GuiText.Crafting.getLocal() + ": " + Long.toString(activeStack.getStackSize()));
					}

					downY += 5;
				}

				if (pendingStack != null && pendingStack.getStackSize() > 0) {
					final String str = GuiText.Scheduled.getLocal() + ": " + converter.toWideReadableForm(pendingStack.getStackSize());
					final int w = 4 + fontRenderer.getStringWidth(str);

					fontRenderer.drawString(str, (int) ((x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19 - (w * 0.5)) * 2), (y * offY + ITEMSTACK_TOP_OFFSET + 6 - negY + downY) * 2, TEXT_COLOR);

					if (tooltip == z - viewStart) {
						lineList.add(GuiText.Scheduled.getLocal() + ": " + Long.toString(pendingStack.getStackSize()));
					}
				}

				GL11.glPopMatrix();
				final int posX = x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19;
				final int posY = y * offY + ITEMSTACK_TOP_OFFSET;

				final ItemStack is = refStack.copy().createItemStack();

				if (tooltip == z - viewStart) {
					dspToolTip = Platform.getItemDisplayName(is);

					if (lineList.size() > 0) {
						dspToolTip = dspToolTip + '\n' + Joiner.on("\n").join(lineList);
					}

					toolPosX = x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 8;
					toolPosY = y * offY + ITEMSTACK_TOP_OFFSET;
				}

				drawItem(posX, posY, is);

				x++;

				if (x > 2) {
					y++;
					x = 0;
				}
			}
		}

		if (tooltip >= 0 && dspToolTip.length() > 0) {
			drawTooltip(toolPosX, toolPosY + 10, dspToolTip);
		}
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		this.bindTexture("appliedenergistics2", "guis/craftingcpu.png");
		this.drawTexturedModalRect(offsetX, offsetY, 0, 0, xSize, ySize);
	}

	public void postUpdate(final List<IAEItemStack> list, final byte ref) {
		switch (ref) {
		case 0:
			for (final IAEItemStack l : list) {
				this.handleInput(storage, l);
			}
			break;

		case 1:
			for (final IAEItemStack l : list) {
				this.handleInput(active, l);
			}
			break;

		case 2:
			for (final IAEItemStack l : list) {
				this.handleInput(pending, l);
			}
			break;
		}

		for (final IAEItemStack l : list) {
			final long amt = getTotal(l);

			if (amt <= 0) {
				deleteVisualStack(l);
			}
			else {
				final IAEItemStack is = findVisualStack(l);
				is.setStackSize(amt);
			}
		}

		this.setScrollBar();
	}

	private void handleInput(final IItemList<IAEItemStack> s, final IAEItemStack l) {
		IAEItemStack a = s.findPrecise(l);

		if (l.getStackSize() <= 0) {
			if (a != null) {
				a.reset();
			}
		}
		else {
			if (a == null) {
				s.add(l.copy());
				a = s.findPrecise(l);
			}

			if (a != null) {
				a.setStackSize(l.getStackSize());
			}
		}
	}

	private long getTotal(final IAEItemStack is) {
		final IAEItemStack a = storage.findPrecise(is);
		final IAEItemStack b = active.findPrecise(is);
		final IAEItemStack c = pending.findPrecise(is);

		long total = 0;

		if (a != null) {
			total += a.getStackSize();
		}

		if (b != null) {
			total += b.getStackSize();
		}

		if (c != null) {
			total += c.getStackSize();
		}

		return total;
	}

	private void deleteVisualStack(final IAEItemStack l) {
		final Iterator<IAEItemStack> i = visual.iterator();

		while (i.hasNext()) {
			final IAEItemStack o = i.next();
			if (o.equals(l)) {
				i.remove();
				return;
			}
		}
	}

	private IAEItemStack findVisualStack(final IAEItemStack l) {
		for (final IAEItemStack o : visual) {
			if (o.equals(l)) {
				return o;
			}
		}

		final IAEItemStack stack = l.copy();
		visual.add(stack);

		return stack;
	}

	@Override
	public Enum getSortBy() {
		return SortOrder.NAME;
	}

	@Override
	public Enum getSortDir() {
		return SortDir.ASCENDING;
	}

	@Override
	public Enum getSortDisplay() {
		return ViewItems.ALL;
	}
}
