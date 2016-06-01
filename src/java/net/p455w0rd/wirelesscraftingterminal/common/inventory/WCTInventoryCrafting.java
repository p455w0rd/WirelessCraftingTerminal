package net.p455w0rd.wirelesscraftingterminal.common.inventory;

import appeng.api.storage.IMEInventory;
import appeng.me.storage.MEIInventoryWrapper;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;

public class WCTInventoryCrafting extends AppEngInternalInventory {

	private ItemStack[] stackList;
	private int inventoryWidth;
	private Container eventHandler;
	private final ItemStack invItem;
	private IAEAppEngInventory te;
	private final int size;
	private boolean enableClientEvents = false;

	public WCTInventoryCrafting(Container container, int rows, int cols, ItemStack is) {
		super((IAEAppEngInventory) container, rows * cols);
		size = rows * cols;
		this.stackList = new ItemStack[size];
		this.eventHandler = container;
		this.inventoryWidth = rows;
		this.invItem = is;
		this.setTileEntity((IAEAppEngInventory) container);
	}

	@Override
	public int getSizeInventory() {
		return this.stackList.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot >= this.getSizeInventory() ? null : this.stackList[slot];
	}

	public ItemStack getStackInRowAndColumn(int row, int col) {
		if (row >= 0 && row < this.inventoryWidth) {
			int k = row + col * this.inventoryWidth;
			return this.getStackInSlot(k);
		} else {
			return null;
		}
	}

	public String getInventoryName() {
		return "crafting";
	}

	public boolean hasCustomInventoryName() {
		return true;
	}

	public ItemStack getStackInSlotOnClosing(int slot) {
		if (this.stackList[slot] != null) {
			ItemStack itemstack = this.stackList[slot];
			this.stackList[slot] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	@Override
	public ItemStack decrStackSize(final int slot, final int qty) {
		if (this.stackList[slot] != null) {
			final ItemStack split = this.getStackInSlot(slot);
			ItemStack ns = null;

			if (qty >= split.stackSize) {
				ns = this.stackList[slot];
				this.stackList[slot] = null;
			} else {
				ns = split.splitStack(qty);
			}

			if (this.getTileEntity() != null && this.eventsEnabled()) {
				this.getTileEntity().onChangeInventory(this, slot, InvOperation.decreaseStackSize, ns, null);
			}

			this.markDirty();
			return ns;
		}

		return null;
	}

	@Override
	protected boolean eventsEnabled() {
		return Platform.isServer() || this.isEnableClientEvents();
	}

	private boolean isEnableClientEvents() {
		return this.enableClientEvents;
	}

	public void setEnableClientEvents(final boolean enableClientEvents) {
		this.enableClientEvents = enableClientEvents;
	}

	@Override
	public boolean isEmpty() {
		for (int x = 0; x < this.size; x++) {
			if (this.getStackInSlot(x) != null) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public IMEInventory getMEInventory() {
		return new MEIInventoryWrapper(this, null);
	}

	private IAEAppEngInventory getTileEntity() {
		return this.te;
	}

	@Override
	public void setTileEntity(final IAEAppEngInventory te) {
		this.te = te;
	}

	/*
	 * @Override public ItemStack decrStackSize(int slot, int amount) { if
	 * (this.stackList[slot] != null) { ItemStack itemstack; if
	 * (this.stackList[slot].stackSize <= amount) { itemstack =
	 * this.stackList[slot]; this.stackList[slot] = null;
	 * this.eventHandler.onCraftMatrixChanged(this); return itemstack; } else {
	 * itemstack = this.stackList[slot].splitStack(amount);
	 * 
	 * if (this.stackList[slot].stackSize == 0) { this.stackList[slot] = null; }
	 * 
	 * this.eventHandler.onCraftMatrixChanged(this); return itemstack; } } else
	 * { return getStackInSlot(slot); } }
	 */
	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.stackList[slot] = stack;
		this.eventHandler.onCraftMatrixChanged(this);
		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
		markDirty();
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty(final int slotIndex) {
		if (this.getTileEntity() != null && this.eventsEnabled()) {
			this.getTileEntity().onChangeInventory(this, slotIndex, InvOperation.markDirty, null, null);
		}
	}

	public void markDirty() {
		if (this.getTileEntity() != null && this.eventsEnabled()) {
			this.getTileEntity().onChangeInventory(this, -1, InvOperation.markDirty, null, null);
		}
		for (int i = 0; i < getSizeInventory(); ++i) {
			if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
				stackList[i] = null;
			}
		}
		writeNBT(invItem.getTagCompound());
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	public void openInventory() {
	}

	public void closeInventory() {
	}

	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return !(stack.getItem() instanceof IWirelessCraftingTerminalItem);
	}

	public void writeNBT(NBTTagCompound nbtTagCompound) {
		// Write the ItemStacks in the inventory to NBT
		NBTTagList tagList = new NBTTagList();
		for (int currentIndex = 0; currentIndex < stackList.length; ++currentIndex) {
			if (stackList[currentIndex] != null) {
				NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setByte("Slot", (byte) currentIndex);
				stackList[currentIndex].writeToNBT(tagCompound);
				tagList.appendTag(tagCompound);
			}
		}
		nbtTagCompound.setTag("CraftingMatrix", tagList);
	}

}
