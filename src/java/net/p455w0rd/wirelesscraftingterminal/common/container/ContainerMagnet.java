package net.p455w0rd.wirelesscraftingterminal.common.container;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotMagnetFilter;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryMagnetFilter;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;

public class ContainerMagnet extends Container {

	public final InventoryPlayer inventoryPlayer;
	public ItemStack magnetItem;
	public WCTInventoryMagnetFilter magnetInventory;
	private int distributeState = 0;
	private int pressedKeyInRange = -1;
	@SuppressWarnings("rawtypes")
	private final Set distributeSlotSet = new HashSet();
	private final int PLAYER_INV_START = 0, PLAYER_INV_END = 26, HOTBAR_START = 27, HOTBAR_END = 35, FILTERS_START = 36,
			FILTERS_END = 62;

	public ContainerMagnet(EntityPlayer player, InventoryPlayer inventoryPlayer) {
		this.inventoryPlayer = inventoryPlayer;
		this.magnetItem = RandomUtils.getMagnet(inventoryPlayer);
		this.magnetInventory = new WCTInventoryMagnetFilter(magnetItem);
		this.magnetInventory.setContainer(this);
		// Add player inventory slots
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(this.inventoryPlayer, j + i * 9 + 9, j * 18 + 8, 126 + i * 18));
			}
		}

		// Add hotbar slots
		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(this.inventoryPlayer, i, i * 18 + 8, 184));
		}

		// Add filter slots
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlotToContainer(new SlotMagnetFilter(this.magnetInventory, j + i * 9, j * 18 + 8, 58 + i * 18));
			}
		}
	}

	private boolean isInHotbar(int slotNum) {
		return (slotNum >= HOTBAR_START) && (slotNum <= HOTBAR_END);
	}

	private boolean isInPlayerInventory(int slotNum) {
		return (slotNum >= PLAYER_INV_START) && (slotNum <= PLAYER_INV_END);
	}

	@SuppressWarnings("unused")
	private boolean isInFilters(int slotNum) {
		return (slotNum >= FILTERS_START) && (slotNum <= FILTERS_END);
	}

	@Override
	public boolean canInteractWith(EntityPlayer p) {
		return true;
	}

	private boolean alreadyFiltered(ItemStack item) {
		for (int i = 0; i < magnetInventory.getSizeInventory(); i++) {
			if (isIdenticalItem(item, magnetInventory.getStackInSlot(i))) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ItemStack slotClick(int slotNum, int mouseButton, int modifier, EntityPlayer player) {
		if (slotNum >= this.inventorySlots.size()) {
			return null;
		}
		InventoryPlayer inventoryplayer = player.inventory;
		ItemStack itemstack3;
		ItemStack stack = null;
		int sizeOrID;
		if (slotNum == -999) {

			if (inventoryplayer.getItemStack() != null && slotNum == -999) {
				if (mouseButton == 0) {
					player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
					inventoryplayer.setItemStack((ItemStack) null);
				}

				if (mouseButton == 1) {
					player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), true);

					if (inventoryplayer.getItemStack().stackSize == 0) {
						inventoryplayer.setItemStack((ItemStack) null);
					}
				}
			}
			return null;
		}

		//Click+drag stack
		else if (modifier == 5) {
			//if (isInFilters(slotNum)) {
			//	return null;
			//}
			int currentDistributeState = this.distributeState;
			this.distributeState = checkForPressedButton(mouseButton);

			if ((currentDistributeState != 1 || this.distributeState != 2) && currentDistributeState != this.distributeState) {
				this.resetDistributionVariables();
			}
			else if (inventoryplayer.getItemStack() == null) {
				this.resetDistributionVariables();
			}
			else if (this.distributeState == 0) {
				this.pressedKeyInRange = checkForPressedButton2(mouseButton);

				if (checkValue(this.pressedKeyInRange)) {
					this.distributeState = 1;
					this.distributeSlotSet.clear();
				}
				else {
					this.resetDistributionVariables();
				}
			}
			else if (this.distributeState == 1) {
				Slot slot = (Slot) this.inventorySlots.get(slotNum);

				if (slot != null && stackFitsInSlot(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > this.distributeSlotSet.size() && this.canDragIntoSlot(slot)) {
					this.distributeSlotSet.add(slot);
				}
			}
			else if (this.distributeState == 2) {
				if (!this.distributeSlotSet.isEmpty()) {
					itemstack3 = inventoryplayer.getItemStack().copy();
					sizeOrID = inventoryplayer.getItemStack().stackSize;
					Iterator iterator = this.distributeSlotSet.iterator();

					while (iterator.hasNext()) {
						Slot slot1 = (Slot) iterator.next();

						if (slot1 != null && stackFitsInSlot(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= this.distributeSlotSet.size() && this.canDragIntoSlot(slot1)) {
							ItemStack itemstack1 = itemstack3.copy();
							int j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
							setSlotStack(this.distributeSlotSet, this.pressedKeyInRange, itemstack1, j1);

							if (itemstack1.stackSize > itemstack1.getMaxStackSize()) {
								itemstack1.stackSize = itemstack1.getMaxStackSize();
							}

							if (itemstack1.stackSize > slot1.getSlotStackLimit()) {
								itemstack1.stackSize = slot1.getSlotStackLimit();
							}

							sizeOrID -= itemstack1.stackSize - j1;
							slot1.putStack(itemstack1);
						}
					}

					itemstack3.stackSize = sizeOrID;

					if (itemstack3.stackSize <= 0) {
						itemstack3 = null;
					}

					inventoryplayer.setItemStack(itemstack3);
				}

				this.resetDistributionVariables();
			}
			else {
				this.resetDistributionVariables();
			}
		}

		else if (this.distributeState != 0) {
			this.resetDistributionVariables();
		}

		else if (getSlot(slotNum) != null && getSlot(slotNum) instanceof SlotMagnetFilter) {

			SlotMagnetFilter slot = (SlotMagnetFilter) getSlot(slotNum);

			ItemStack stackSlot = slot.getStack();
			if (alreadyFiltered(player.inventory.getItemStack()) && player.inventory.getItemStack() != null) {
				return null;
			}
			if (stackSlot != null) {
				stack = stackSlot.copy();
			}

			if (mouseButton == 2) {
				fillPhantomSlot(slot, null, mouseButton);
			}
			else if (mouseButton == 0 || mouseButton == 1) {
				InventoryPlayer playerInv = player.inventory;

				ItemStack stackHeld = playerInv.getItemStack();

				if (stackSlot == null) {
					if (stackHeld != null && slot.isItemValid(stackHeld)) {
						fillPhantomSlot(slot, stackHeld, mouseButton);
					}
				}
				else if (stackHeld == null) {
					adjustPhantomSlot(slot, mouseButton, modifier);
				}
				else if (slot.isItemValid(stackHeld)) {
					if (isIdenticalItem(stackSlot, stackHeld)) {
						adjustPhantomSlot(slot, mouseButton, modifier);
					}
					else {
						fillPhantomSlot(slot, stackHeld, mouseButton);
					}
				}
			}
			return stack;
		}
		else {
			return super.slotClick(slotNum, mouseButton, modifier, player);
		}
		return stack;
	}

	public boolean alwaysTrue(Slot slot) {
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static void setSlotStack(Set slotSet, int stackSizeSelector, ItemStack stackToResize, int currentSlotStackSize) {
		switch (stackSizeSelector) {
		case 0:
			stackToResize.stackSize = MathHelper.floor_float((float) stackToResize.stackSize / (float) slotSet.size());
			break;
		case 1:
			stackToResize.stackSize = 1;
		}

		stackToResize.stackSize += currentSlotStackSize;
	}

	public static boolean stackFitsInSlot(Slot slot, ItemStack itemStack, boolean sizeMatters) {
		boolean flag1 = slot == null || !slot.getHasStack();

		if (slot != null && slot.getHasStack() && itemStack != null && itemStack.isItemEqual(slot.getStack()) && ItemStack.areItemStackTagsEqual(slot.getStack(), itemStack)) {
			int i = sizeMatters ? 0 : itemStack.stackSize;
			flag1 |= slot.getStack().stackSize + i <= itemStack.getMaxStackSize();
		}

		return flag1;
	}

	public static int checkForPressedButton(int btn) {
		return btn & 3;
	}

	public static int checkForPressedButton2(int mouseButtonPressed) {
		return mouseButtonPressed >> 2 & 3;
	}

	public static boolean checkValue(int value) {
		return value == 0 || value == 1;
	}

	protected void resetDistributionVariables() {
		this.distributeState = 0;
		this.distributeSlotSet.clear();
	}

	private void arrangeSlots() {
		int invSize = magnetInventory.getSizeInventory();
		if (invSize <= 0) {
			return;
		}
		for (int i = 0; i < invSize; i++) {
			if (magnetInventory.getStackInSlot(i) == null) {
				for (int j = 0; j < invSize; j++) {
					if (j <= i) {
						continue;
					}
					if (magnetInventory.getStackInSlot(j) != null) {
						magnetInventory.setInventorySlotContents(i, magnetInventory.getStackInSlot(j));
						magnetInventory.decrStackSize(j, 1);
						break;
					}
				}
			}
		}
	}

	private void fillPhantomSlot(SlotMagnetFilter slot, ItemStack stackHeld, int mouseButton) {
		if (!slot.canAdjustPhantom()) {
			return;
		}
		if (stackHeld == null) {
			slot.putStack(null);
			return;
		}

		int stackSize = mouseButton == 0 ? stackHeld.stackSize : 1;
		if (stackSize > slot.getSlotStackLimit()) {
			stackSize = slot.getSlotStackLimit();
		}
		ItemStack phantomStack = stackHeld.copy();
		RandomUtils.removeTimerTags(phantomStack);
		phantomStack.stackSize = stackSize;

		slot.putStack(phantomStack);
		arrangeSlots();
	}

	private void adjustPhantomSlot(SlotMagnetFilter slot, int mouseButton, int modifier) {
		if (!slot.canAdjustPhantom()) {
			return;
		}
		ItemStack stackSlot = slot.getStack();
		int stackSize;
		if (modifier == 1) {
			stackSize = mouseButton == 0 ? (stackSlot.stackSize + 1) / 2 : stackSlot.stackSize * 2;
		}
		else {
			stackSize = mouseButton == 0 ? stackSlot.stackSize - 1 : stackSlot.stackSize + 1;
		}

		if (stackSize > slot.getSlotStackLimit()) {
			stackSize = slot.getSlotStackLimit();
		}

		stackSlot.stackSize = stackSize;

		if (stackSlot.stackSize <= 0) {
			stackSlot = null;
		}

		slot.putStack(stackSlot);
		arrangeSlots();
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer p, final int slotIndex) {
		Slot slot = (Slot) inventorySlots.get(slotIndex);
		if (slot == null || slot.getStack() == null) {
			return null;
		}
		ItemStack stack = slot.getStack();
		if (isInHotbar(slotIndex)) {
			if (!mergePhantomStack(stack)) {
				if (!mergeItemStack(stack, PLAYER_INV_START, PLAYER_INV_END + 1, false)) {
					return null;
				}
			}
			else {
				return null;
			}
		}
		else if (isInPlayerInventory(slotIndex)) {
			if (!mergePhantomStack(stack)) {
				if (!mergeItemStack(stack, HOTBAR_START, HOTBAR_END + 1, false)) {
					return null;
				}
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
		if (stack.stackSize == 0) {
			slot.putStack((ItemStack) null);
		}
		else {
			slot.onSlotChanged();
		}
		slot.onPickupFromSlot(p, stack);
		return stack;
	}

	protected boolean mergePhantomStack(ItemStack stack) {
		if (!alreadyFiltered(stack)) {
			for (int i = FILTERS_START; i <= FILTERS_END; i++) {
				if (this.getSlot(i).getStack() != null) {
					continue;
				}
				else {
					fillPhantomSlot((SlotMagnetFilter) this.getSlot(i), stack, 0);
					return true;
				}
			}
		}
		return false;
	}

	public boolean isIdenticalItem(ItemStack lhs, ItemStack rhs) {
		if (lhs == rhs) {
			return true;
		}

		if (lhs == null || rhs == null) {
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
	protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean backwards) {
		boolean flag1 = false;
		int k = (backwards ? end - 1 : start);
		Slot slot;
		ItemStack itemstack1;

		if (stack.isStackable()) {
			while (stack.stackSize > 0 && (!backwards && k < end || backwards && k >= start)) {
				slot = (Slot) inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (!slot.isItemValid(stack)) {
					k += (backwards ? -1 : 1);
					continue;
				}

				if (itemstack1 != null && itemstack1.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, itemstack1)) {
					int l = itemstack1.stackSize + stack.stackSize;

					if (l <= stack.getMaxStackSize() && l <= slot.getSlotStackLimit()) {
						stack.stackSize = 0;
						itemstack1.stackSize = l;
						magnetInventory.markDirty();
						flag1 = true;
					}
					else if (itemstack1.stackSize < stack.getMaxStackSize() && l < slot.getSlotStackLimit()) {
						stack.stackSize -= stack.getMaxStackSize() - itemstack1.stackSize;
						itemstack1.stackSize = stack.getMaxStackSize();
						magnetInventory.markDirty();
						flag1 = true;
					}
				}

				k += (backwards ? -1 : 1);
			}
		}
		if (stack.stackSize > 0) {
			k = (backwards ? end - 1 : start);
			while (!backwards && k < end || backwards && k >= start) {
				slot = (Slot) inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (!slot.isItemValid(stack)) {
					k += (backwards ? -1 : 1);
					continue;
				}

				if (itemstack1 == null) {
					int l = stack.stackSize;
					if (l <= slot.getSlotStackLimit()) {
						slot.putStack(stack.copy());
						stack.stackSize = 0;
						magnetInventory.markDirty();
						flag1 = true;
						break;
					}
					else {
						putStackInSlot(k, new ItemStack(stack.getItem(), slot.getSlotStackLimit(), stack.getItemDamage()));
						stack.stackSize -= slot.getSlotStackLimit();
						magnetInventory.markDirty();
						flag1 = true;
					}
				}

				k += (backwards ? -1 : 1);
			}
		}

		return flag1;
	}

}
