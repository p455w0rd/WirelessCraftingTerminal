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
package p455w0rd.wct.container;

import java.util.*;

import javax.annotation.Nonnull;

import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wct.api.IMagnetContainer;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.client.gui.GuiWCT;
import p455w0rd.wct.container.slot.SlotMagnetFilter;
import p455w0rd.wct.inventory.WCTInventoryMagnetFilter;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.util.WCTUtils;
import p455w0rdslib.util.MathUtils;

public class ContainerMagnet extends Container implements IMagnetContainer {

	public final InventoryPlayer inventoryPlayer;
	public final ItemStack magnetItem;
	public WCTInventoryMagnetFilter magnetInventory;
	private int distributeState = 0;
	private int pressedKeyInRange = -1;
	private final Set<Slot> distributeSlotSet = new HashSet<>();
	private final int PLAYER_INV_START = 0, PLAYER_INV_END = 26, HOTBAR_START = 27, HOTBAR_END = 35, FILTERS_START = 36,
			FILTERS_END = 62;
	private final boolean isHeld;
	private final boolean isWCTBauble;
	private final int wctSlot;
	private final ItemStack wirelessTerminal;

	public ContainerMagnet(final EntityPlayer player, final boolean isHeld, final boolean isWCTBauble, final int wctSlot) {
		inventoryPlayer = player.inventory;
		this.isHeld = isHeld;
		this.isWCTBauble = isWCTBauble;
		this.wctSlot = wctSlot;
		if (isHeld) {
			magnetItem = ItemMagnet.getHeldMagnet(player);
			wirelessTerminal = ItemStack.EMPTY;
		}
		else {
			wirelessTerminal = isWCTBauble ? WTApi.instance().getBaublesUtility().getWTBySlot(player, wctSlot, IWirelessCraftingTerminalItem.class) : WCTUtils.getWCTBySlot(player, wctSlot, false);
			magnetItem = ItemMagnet.getMagnetFromWCT(wirelessTerminal);
		}
		magnetInventory = new WCTInventoryMagnetFilter(magnetItem);
		magnetInventory.setContainer(this);
		// Add player inventory slots
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, j * 18 + 8, 126 + i * 18));
			}
		}

		// Add hotbar slots
		for (int i = 0; i < 9; ++i) {
			addSlotToContainer(new Slot(inventoryPlayer, i, i * 18 + 8, 184));
		}

		// Add filter slots
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new SlotMagnetFilter(magnetInventory, j + i * 9, j * 18 + 8, 58 + i * 18));
			}
		}
	}

	@Override
	public void onContainerClosed(final EntityPlayer playerIn) {
		if (Platform.isClient()) {
			GuiWCT.setSwitchingGuis(false);
		}
	}

	@Override
	public EntityPlayer getPlayer() {
		return inventoryPlayer.player;
	}

	@Override
	public boolean isMagnetHeld() {
		return isHeld;
	}

	@Override
	public boolean isWTBauble() {
		return isWCTBauble;
	}

	@Override
	public int getWTSlot() {
		return wctSlot;
	}

	@Override
	public ItemStack getWirelessTerminal() {
		return wirelessTerminal;
	}

	@Override
	public ItemStack getMagnet() {
		return magnetItem;
	}

	private boolean isInHotbar(final int slotNum) {
		return slotNum >= HOTBAR_START && slotNum <= HOTBAR_END;
	}

	private boolean isInPlayerInventory(final int slotNum) {
		return slotNum >= PLAYER_INV_START && slotNum <= PLAYER_INV_END;
	}

	@SuppressWarnings("unused")
	private boolean isInFilters(final int slotNum) {
		return slotNum >= FILTERS_START && slotNum <= FILTERS_END;
	}

	@Override
	public boolean canInteractWith(final EntityPlayer p) {
		return true;
	}

	private boolean alreadyFiltered(final ItemStack item) {
		for (int i = 0; i < magnetInventory.getSizeInventory(); i++) {
			if (isIdenticalItem(item, magnetInventory.getStackInSlot(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack slotClick(final int slotNum, final int dragType, final ClickType clickTypeIn, final EntityPlayer player) {
		if (slotNum >= inventorySlots.size()) {
			return null;
		}
		final InventoryPlayer inventoryplayer = player.inventory;
		ItemStack itemstack3 = ItemStack.EMPTY;
		ItemStack stack = ItemStack.EMPTY;
		int sizeOrID;
		if (slotNum == -999 || clickTypeIn == ClickType.THROW) {

			if (!inventoryplayer.getItemStack().isEmpty()) {
				if (dragType == 0) {
					player.dropItem(inventoryplayer.getItemStack(), true);
					inventoryplayer.setItemStack(ItemStack.EMPTY);
				}

				if (dragType == 1) {
					player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);

					if (inventoryplayer.getItemStack().getCount() == 0) {
						inventoryplayer.setItemStack(ItemStack.EMPTY);
					}
				}
			}
			return ItemStack.EMPTY;
		}
		if (slotNum < 0) {
			return ItemStack.EMPTY;
		}

		//Click+drag stack
		else if (clickTypeIn == ClickType.QUICK_CRAFT) {
			//if (isInFilters(slotNum)) {
			//	return null;
			//}
			final int currentDistributeState = distributeState;
			distributeState = checkForPressedButton(dragType);

			if ((currentDistributeState != 1 || distributeState != 2) && currentDistributeState != distributeState) {
				resetDistributionVariables();
			}
			else if (inventoryplayer.getItemStack().isEmpty()) {
				resetDistributionVariables();
			}
			else if (distributeState == 0) {
				pressedKeyInRange = checkForPressedButton2(dragType);

				if (checkValue(pressedKeyInRange)) {
					distributeState = 1;
					distributeSlotSet.clear();
				}
				else {
					resetDistributionVariables();
				}
			}
			else if (distributeState == 1) {
				final Slot slot = inventorySlots.get(slotNum);

				if (slot != null && stackFitsInSlot(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().getCount() > distributeSlotSet.size() && canDragIntoSlot(slot)) {
					distributeSlotSet.add(slot);
				}
			}
			else if (distributeState == 2) {
				if (!distributeSlotSet.isEmpty()) {
					itemstack3 = inventoryplayer.getItemStack().copy();
					sizeOrID = inventoryplayer.getItemStack().getCount();
					final Iterator<Slot> iterator = distributeSlotSet.iterator();

					while (iterator.hasNext()) {
						final Slot slot1 = iterator.next();

						if (slot1 != null && stackFitsInSlot(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().getCount() >= distributeSlotSet.size() && canDragIntoSlot(slot1)) {
							final ItemStack itemstack1 = itemstack3.copy();
							final int j1 = slot1.getHasStack() ? slot1.getStack().getCount() : 0;
							setSlotStack(distributeSlotSet, pressedKeyInRange, itemstack1, j1);

							if (itemstack1.getCount() > itemstack1.getMaxStackSize()) {
								itemstack1.setCount(itemstack1.getMaxStackSize());
							}

							if (itemstack1.getCount() > slot1.getSlotStackLimit()) {
								itemstack1.setCount(slot1.getSlotStackLimit());
							}

							sizeOrID -= itemstack1.getCount() - j1;
							slot1.putStack(itemstack1);
						}
					}

					itemstack3.setCount(sizeOrID);

					if (itemstack3.getCount() <= 0) {
						itemstack3 = ItemStack.EMPTY;
					}

					inventoryplayer.setItemStack(itemstack3);
				}

				resetDistributionVariables();
			}
			else {
				resetDistributionVariables();
			}
		}

		else if (distributeState != 0) {
			resetDistributionVariables();
		}

		else if (getSlot(slotNum) != null && getSlot(slotNum) instanceof SlotMagnetFilter) {

			final SlotMagnetFilter slot = (SlotMagnetFilter) getSlot(slotNum);

			final ItemStack stackSlot = slot.getStack();
			if (alreadyFiltered(player.inventory.getItemStack()) && !player.inventory.getItemStack().isEmpty()) {
				return ItemStack.EMPTY;
			}
			if (!stackSlot.isEmpty()) {
				stack = stackSlot.copy();
			}

			if (dragType == 2) {
				fillPhantomSlot(slot, ItemStack.EMPTY, clickTypeIn);
			}
			else if (dragType == 0 || dragType == 1) {
				final InventoryPlayer playerInv = player.inventory;

				final ItemStack stackHeld = playerInv.getItemStack();

				if (stackSlot.isEmpty()) {
					if (!stackHeld.isEmpty() && slot.isItemValid(stackHeld)) {
						fillPhantomSlot(slot, stackHeld, clickTypeIn);
					}
				}
				else if (stackHeld.isEmpty()) {
					adjustPhantomSlot(slot, dragType, clickTypeIn);
				}
				else if (slot.isItemValid(stackHeld)) {
					if (isIdenticalItem(stackSlot, stackHeld)) {
						adjustPhantomSlot(slot, dragType, clickTypeIn);
					}
					else {
						fillPhantomSlot(slot, stackHeld, clickTypeIn);
					}
				}
			}
			return stack;
		}
		else {
			return super.slotClick(slotNum, dragType, clickTypeIn, player);
		}
		return stack;
	}

	public static void setSlotStack(final Set<Slot> slotSet, final int stackSizeSelector, @Nonnull final ItemStack stackToResize, final int currentSlotStackSize) {
		switch (stackSizeSelector) {
		case 0:
			stackToResize.setCount(MathUtils.floor((float) stackToResize.getCount() / (float) slotSet.size()));
			break;
		case 1:
			stackToResize.setCount(1);
		}

		stackToResize.grow(currentSlotStackSize);
	}

	public static boolean stackFitsInSlot(final Slot slot, @Nonnull final ItemStack itemStack, final boolean sizeMatters) {
		boolean flag1 = slot == null || !slot.getHasStack();

		if (slot != null && slot.getHasStack() && !itemStack.isEmpty() && itemStack.isItemEqual(slot.getStack()) && ItemStack.areItemStackTagsEqual(slot.getStack(), itemStack)) {
			final int i = sizeMatters ? 0 : itemStack.getCount();
			flag1 |= slot.getStack().getCount() + i <= itemStack.getMaxStackSize();
		}

		return flag1;
	}

	public static int checkForPressedButton(final int btn) {
		return btn & 3;
	}

	public static int checkForPressedButton2(final int mouseButtonPressed) {
		return mouseButtonPressed >> 2 & 3;
	}

	public static boolean checkValue(final int value) {
		return value == 0 || value == 1;
	}

	protected void resetDistributionVariables() {
		distributeState = 0;
		distributeSlotSet.clear();
	}

	private void arrangeSlots() {
		final int invSize = magnetInventory.getSizeInventory();
		if (invSize <= 0) {
			return;
		}
		for (int i = 0; i < invSize; i++) {
			if (magnetInventory.getStackInSlot(i).isEmpty()) {
				for (int j = 0; j < invSize; j++) {
					if (j <= i) {
						continue;
					}
					if (!magnetInventory.getStackInSlot(j).isEmpty()) {
						magnetInventory.setInventorySlotContents(i, magnetInventory.getStackInSlot(j));
						magnetInventory.decrStackSize(j, 1);
						break;
					}
				}
			}
		}
	}

	private void fillPhantomSlot(final SlotMagnetFilter slot, @Nonnull final ItemStack stackHeld, final ClickType clickType) {
		if (!slot.canAdjustPhantom()) {
			return;
		}
		if (stackHeld.isEmpty()) {
			slot.putStack(ItemStack.EMPTY);
			return;
		}

		int stackSize = clickType == ClickType.QUICK_MOVE ? stackHeld.getCount() : 1;
		if (stackSize > slot.getSlotStackLimit()) {
			stackSize = slot.getSlotStackLimit();
		}
		final ItemStack phantomStack = stackHeld.copy();
		ItemMagnet.removeTimerTags(phantomStack);
		phantomStack.setCount(stackSize);

		slot.putStack(phantomStack);
		arrangeSlots();
	}

	private void adjustPhantomSlot(final SlotMagnetFilter slot, final int mouseButton, final ClickType clickType) {
		if (!slot.canAdjustPhantom()) {
			return;
		}
		ItemStack stackSlot = slot.getStack();
		int stackSize;
		if (clickType == ClickType.QUICK_CRAFT) {
			stackSize = mouseButton == 0 ? (stackSlot.getCount() + 1) / 2 : stackSlot.getCount() * 2;
		}
		else {
			stackSize = mouseButton == 0 ? stackSlot.getCount() - 1 : stackSlot.getCount() + 1;
		}

		if (stackSize > slot.getSlotStackLimit()) {
			stackSize = slot.getSlotStackLimit();
		}

		stackSlot.setCount(stackSize);

		if (stackSlot.getCount() <= 0) {
			stackSlot = ItemStack.EMPTY;
		}

		slot.putStack(stackSlot);
		arrangeSlots();
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer p, final int slotIndex) {
		final Slot slot = inventorySlots.get(slotIndex);
		if (slot == null || slot.getStack().isEmpty()) {
			return ItemStack.EMPTY;
		}
		final ItemStack stack = slot.getStack();
		if (isInHotbar(slotIndex)) {
			if (!mergePhantomStack(stack)) {
				if (!mergeItemStack(stack, PLAYER_INV_START, PLAYER_INV_END + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			else {
				return ItemStack.EMPTY;
			}
		}
		else if (isInPlayerInventory(slotIndex)) {
			if (!mergePhantomStack(stack)) {
				if (!mergeItemStack(stack, HOTBAR_START, HOTBAR_END + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			else {
				return ItemStack.EMPTY;
			}
		}
		else {
			return ItemStack.EMPTY;
		}
		if (stack.getCount() == 0) {
			slot.putStack(ItemStack.EMPTY);
		}
		else {
			slot.onSlotChanged();
		}
		slot.onTake(p, stack);
		return stack;
	}

	protected boolean mergePhantomStack(@Nonnull final ItemStack stack) {
		if (!alreadyFiltered(stack)) {
			for (int i = FILTERS_START; i <= FILTERS_END; i++) {
				if (!getSlot(i).getStack().isEmpty()) {
					continue;
				}
				else {
					fillPhantomSlot((SlotMagnetFilter) getSlot(i), stack, ClickType.PICKUP);
					return true;
				}
			}
		}
		return false;
	}

	public boolean isIdenticalItem(@Nonnull final ItemStack lhs, @Nonnull final ItemStack rhs) {
		if (lhs == rhs) {
			return true;
		}

		if (lhs.isEmpty() || rhs.isEmpty()) {
			return false;
		}

		if (lhs.getItem() != rhs.getItem()) {
			return false;
		}

		if (lhs.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
			if (lhs.getItemDamage() != rhs.getItemDamage()) {
				return false;
			}
		}

		return ItemStack.areItemStackTagsEqual(lhs, rhs);
	}

	@Override
	protected boolean mergeItemStack(@Nonnull final ItemStack stack, final int start, final int end, final boolean backwards) {
		boolean flag1 = false;
		int k = backwards ? end - 1 : start;
		Slot slot;
		ItemStack itemstack1 = ItemStack.EMPTY;

		if (stack.isStackable()) {
			while (stack.getCount() > 0 && (!backwards && k < end || backwards && k >= start)) {
				slot = inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (!slot.isItemValid(stack)) {
					k += backwards ? -1 : 1;
					continue;
				}

				if (!itemstack1.isEmpty() && itemstack1.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, itemstack1)) {
					final int l = itemstack1.getCount() + stack.getCount();

					if (l <= stack.getMaxStackSize() && l <= slot.getSlotStackLimit()) {
						stack.setCount(0);
						itemstack1.setCount(l);
						magnetInventory.markDirty();
						flag1 = true;
					}
					else if (itemstack1.getCount() < stack.getMaxStackSize() && l < slot.getSlotStackLimit()) {
						stack.shrink(stack.getMaxStackSize() - itemstack1.getCount());
						itemstack1.setCount(stack.getMaxStackSize());
						magnetInventory.markDirty();
						flag1 = true;
					}
				}

				k += backwards ? -1 : 1;
			}
		}
		if (stack.getCount() > 0) {
			k = backwards ? end - 1 : start;
			while (!backwards && k < end || backwards && k >= start) {
				slot = inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (!slot.isItemValid(stack)) {
					k += backwards ? -1 : 1;
					continue;
				}

				if (itemstack1.isEmpty()) {
					final int l = stack.getCount();
					if (l <= slot.getSlotStackLimit()) {
						slot.putStack(stack.copy());
						stack.setCount(0);
						magnetInventory.markDirty();
						flag1 = true;
						break;
					}
					else {
						putStackInSlot(k, new ItemStack(stack.getItem(), slot.getSlotStackLimit(), stack.getItemDamage()));
						stack.shrink(slot.getSlotStackLimit());
						magnetInventory.markDirty();
						flag1 = true;
					}
				}

				k += backwards ? -1 : 1;
			}
		}

		return flag1;
	}

}
