package net.p455w0rd.wirelesscraftingterminal.common.container;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotMagnetFilter;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryMagnetFilter;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;

public class ContainerMagnet extends Container {

	public final InventoryPlayer inventoryPlayer;
	private EntityPlayer player;
	public ItemStack heldItem;
	public WCTInventoryMagnetFilter magnetInventory;
	private final int playerInventorySize = 9 * 4;
	private final int playerHotbarSize = 9;
	private final int PLAYER_INV_START = 0, PLAYER_INV_END = 26, HOTBAR_START = 27, HOTBAR_END = 36, FILTERS_START = 37,
			FILTERS_END = 56;

	public ContainerMagnet(EntityPlayer player, InventoryPlayer inventoryPlayer) {
		this.inventoryPlayer = inventoryPlayer;
		this.player = player;
		this.heldItem = RandomUtils.getMagnet(inventoryPlayer);
		this.magnetInventory = new WCTInventoryMagnetFilter(heldItem);

		// Add player inventory slots
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(this.inventoryPlayer, j + i * 9 + 9, j * 18 + 8, 86 + i * 18));
			}
		}

		// Add hotbar slots
		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(this.inventoryPlayer, i, i * 18 + 8, 144));
		}

		// Add filter slots
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlotToContainer(new SlotMagnetFilter(this.magnetInventory, j + i * 9, j * 18 + 8, 18 + i * 18));
			}
		}
	}

	private boolean isInHotbar(int slotNum) {
		return (slotNum >= HOTBAR_START) && (slotNum <= HOTBAR_END);
	}

	private boolean isInPlayerInventory(int slotNum) {
		return (slotNum >= PLAYER_INV_START) && (slotNum <= PLAYER_INV_END);
	}

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
	public ItemStack slotClick(int slotNum, int mouseButton, int modifier, EntityPlayer player) {
		if (slotNum < 0) {
			return null;
		}
		if (getSlot(slotNum) == null) {
			if (getSlot(slotNum) instanceof SlotMagnetFilter) {
				arrangeSlots();
			}
			return null;
		}
		//if (getSlot(slotNum).getStack() == RandomUtils.getMagnet(player.inventory)) {
		//	return null;
		//}
		if (getSlot(slotNum) instanceof SlotMagnetFilter) {

			SlotMagnetFilter slot = (SlotMagnetFilter) getSlot(slotNum);
			ItemStack stack = null;

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
			else if (mouseButton == 5) {
				InventoryPlayer playerInv = player.inventory;
				ItemStack stackHeld = playerInv.getItemStack();
				if (!slot.getHasStack()) {
					fillPhantomSlot(slot, stackHeld, mouseButton);
				}
			}
			return stack;
		}
		return super.slotClick(slotNum, mouseButton, modifier, player);
	}

	private void arrangeSlots() {
		int invSize = magnetInventory.getSizeInventory();
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
		phantomStack.stackSize = stackSize;
		ItemStack plainItem = new ItemStack(phantomStack.getItem(), 1);

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
		ItemStack itemStack = null;
		Slot slot = (Slot) inventorySlots.get(slotIndex);
		if (slot == null || !slot.getHasStack()) {
			return null;
		}
		ItemStack stack = slot.getStack();
		itemStack = stack.copy();
		if (isInHotbar(slotIndex)) {
			if (!mergeItemStack(stack, PLAYER_INV_START, PLAYER_INV_END + 1, false)) {
				return null;
			}
		}
		else if (isInPlayerInventory(slotIndex)) {
			if (!mergeItemStack(stack, HOTBAR_START, HOTBAR_END + 1, false)) {
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
