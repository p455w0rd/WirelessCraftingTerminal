package p455w0rd.wct.container;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import p455w0rd.wct.client.gui.GuiWCT;
import p455w0rd.wct.container.slot.SlotMagnetFilter;
import p455w0rd.wct.inventory.WCTInventoryMagnetFilter;
import p455w0rd.wct.util.WCTUtils;
import p455w0rdslib.util.MathUtils;

public class ContainerMagnet extends Container {

	public final InventoryPlayer inventoryPlayer;
	public ItemStack magnetItem;
	public WCTInventoryMagnetFilter magnetInventory;
	private int distributeState = 0;
	private int pressedKeyInRange = -1;
	private final Set<Slot> distributeSlotSet = new HashSet<Slot>();
	private final int PLAYER_INV_START = 0, PLAYER_INV_END = 26, HOTBAR_START = 27, HOTBAR_END = 35, FILTERS_START = 36,
			FILTERS_END = 62;

	public ContainerMagnet(EntityPlayer player, InventoryPlayer inventoryPlayer) {
		this.inventoryPlayer = inventoryPlayer;
		magnetItem = WCTUtils.getMagnet(inventoryPlayer);
		magnetInventory = new WCTInventoryMagnetFilter(magnetItem);
		magnetInventory.setContainer(this);
		// Add player inventory slots
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(this.inventoryPlayer, j + i * 9 + 9, j * 18 + 8, 126 + i * 18));
			}
		}

		// Add hotbar slots
		for (int i = 0; i < 9; ++i) {
			addSlotToContainer(new Slot(this.inventoryPlayer, i, i * 18 + 8, 184));
		}

		// Add filter slots
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new SlotMagnetFilter(magnetInventory, j + i * 9, j * 18 + 8, 58 + i * 18));
			}
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		if (Platform.isClient()) {
			GuiWCT.setSwitchingGuis(false);
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

	@Override
	public ItemStack slotClick(int slotNum, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (slotNum >= inventorySlots.size()) {
			return null;
		}
		InventoryPlayer inventoryplayer = player.inventory;
		ItemStack itemstack3;
		ItemStack stack = null;
		int sizeOrID;
		if (slotNum == -999 || clickTypeIn == ClickType.THROW) {

			if (inventoryplayer.getItemStack() != null) {
				if (dragType == 0) {
					player.dropItem(inventoryplayer.getItemStack(), true);
					inventoryplayer.setItemStack((ItemStack) null);
				}

				if (dragType == 1) {
					player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);

					if (inventoryplayer.getItemStack().stackSize == 0) {
						inventoryplayer.setItemStack((ItemStack) null);
					}
				}
			}
			return null;
		}

		//Click+drag stack
		else if (clickTypeIn == ClickType.QUICK_CRAFT) {
			//if (isInFilters(slotNum)) {
			//	return null;
			//}
			int currentDistributeState = distributeState;
			distributeState = checkForPressedButton(dragType);

			if ((currentDistributeState != 1 || distributeState != 2) && currentDistributeState != distributeState) {
				resetDistributionVariables();
			}
			else if (inventoryplayer.getItemStack() == null) {
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
				Slot slot = inventorySlots.get(slotNum);

				if (slot != null && stackFitsInSlot(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > distributeSlotSet.size() && canDragIntoSlot(slot)) {
					distributeSlotSet.add(slot);
				}
			}
			else if (distributeState == 2) {
				if (!distributeSlotSet.isEmpty()) {
					itemstack3 = inventoryplayer.getItemStack().copy();
					sizeOrID = inventoryplayer.getItemStack().stackSize;
					Iterator<Slot> iterator = distributeSlotSet.iterator();

					while (iterator.hasNext()) {
						Slot slot1 = iterator.next();

						if (slot1 != null && stackFitsInSlot(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= distributeSlotSet.size() && canDragIntoSlot(slot1)) {
							ItemStack itemstack1 = itemstack3.copy();
							int j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
							setSlotStack(distributeSlotSet, pressedKeyInRange, itemstack1, j1);

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

			SlotMagnetFilter slot = (SlotMagnetFilter) getSlot(slotNum);

			ItemStack stackSlot = slot.getStack();
			if (alreadyFiltered(player.inventory.getItemStack()) && player.inventory.getItemStack() != null) {
				return null;
			}
			if (stackSlot != null) {
				stack = stackSlot.copy();
			}

			if (dragType == 2) {
				fillPhantomSlot(slot, null, clickTypeIn);
			}
			else if (dragType == 0 || dragType == 1) {
				InventoryPlayer playerInv = player.inventory;

				ItemStack stackHeld = playerInv.getItemStack();

				if (stackSlot == null) {
					if (stackHeld != null && slot.isItemValid(stackHeld)) {
						fillPhantomSlot(slot, stackHeld, clickTypeIn);
					}
				}
				else if (stackHeld == null) {
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

	public boolean alwaysTrue(Slot slot) {
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static void setSlotStack(Set slotSet, int stackSizeSelector, ItemStack stackToResize, int currentSlotStackSize) {
		switch (stackSizeSelector) {
		case 0:
			stackToResize.stackSize = MathUtils.floor((float) stackToResize.stackSize / (float) slotSet.size());
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
		distributeState = 0;
		distributeSlotSet.clear();
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

	private void fillPhantomSlot(SlotMagnetFilter slot, ItemStack stackHeld, ClickType clickType) {
		if (!slot.canAdjustPhantom()) {
			return;
		}
		if (stackHeld == null) {
			slot.putStack(null);
			return;
		}

		int stackSize = clickType == ClickType.QUICK_MOVE ? stackHeld.stackSize : 1;
		if (stackSize > slot.getSlotStackLimit()) {
			stackSize = slot.getSlotStackLimit();
		}
		ItemStack phantomStack = stackHeld.copy();
		WCTUtils.removeTimerTags(phantomStack);
		phantomStack.stackSize = stackSize;

		slot.putStack(phantomStack);
		arrangeSlots();
	}

	private void adjustPhantomSlot(SlotMagnetFilter slot, int mouseButton, ClickType clickType) {
		if (!slot.canAdjustPhantom()) {
			return;
		}
		ItemStack stackSlot = slot.getStack();
		int stackSize;
		if (clickType == ClickType.QUICK_CRAFT) {
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
		Slot slot = inventorySlots.get(slotIndex);
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
				if (getSlot(i).getStack() != null) {
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
				slot = inventorySlots.get(k);
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
				slot = inventorySlots.get(k);
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
