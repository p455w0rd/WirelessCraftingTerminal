package p455w0rd.wct.inventory;

import appeng.tile.inventory.*;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;

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
		stackList = new ItemStack[size];
		eventHandler = container;
		inventoryWidth = rows;
		invItem = is;
		setTileEntity((IAEAppEngInventory) container);
	}

	@Override
	public int getSizeInventory() {
		return stackList.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot >= getSizeInventory() ? null : stackList[slot];
	}

	public ItemStack getStackInRowAndColumn(int row, int col) {
		if (row >= 0 && row < inventoryWidth) {
			int k = row + col * inventoryWidth;
			return getStackInSlot(k);
		}
		else {
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
		if (stackList[slot] != null) {
			ItemStack itemstack = stackList[slot];
			stackList[slot] = null;
			return itemstack;
		}
		else {
			return null;
		}
	}

	@Override
	public ItemStack decrStackSize(final int slot, final int qty) {
		if (stackList[slot] != null) {
			final ItemStack split = getStackInSlot(slot);
			ItemStack ns = null;

			if (qty >= split.stackSize) {
				ns = stackList[slot];
				stackList[slot] = null;
			}
			else {
				ns = split.splitStack(qty);
			}

			if (getTileEntity() != null && eventsEnabled()) {
				getTileEntity().onChangeInventory(this, slot, InvOperation.decreaseStackSize, ns, null);
			}

			this.markDirty();
			return ns;
		}

		return null;
	}

	@Override
	protected boolean eventsEnabled() {
		return Platform.isServer() || isEnableClientEvents();
	}

	private boolean isEnableClientEvents() {
		return enableClientEvents;
	}

	@Override
	public void setEnableClientEvents(final boolean enableClientEvents) {
		this.enableClientEvents = enableClientEvents;
	}

	@Override
	public boolean isEmpty() {
		for (int x = 0; x < size; x++) {
			if (getStackInSlot(x) != null) {
				return false;
			}
		}
		return true;
	}

	private IAEAppEngInventory getTileEntity() {
		return te;
	}

	@Override
	public void setTileEntity(final IAEAppEngInventory te) {
		this.te = te;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		stackList[slot] = stack;
		eventHandler.onCraftMatrixChanged(this);
		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
		markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty(final int slotIndex) {
		if (getTileEntity() != null && eventsEnabled()) {
			getTileEntity().onChangeInventory(this, slotIndex, InvOperation.markDirty, null, null);
		}
	}

	@Override
	public void markDirty() {
		if (getTileEntity() != null && eventsEnabled()) {
			getTileEntity().onChangeInventory(this, -1, InvOperation.markDirty, null, null);
		}
		for (int i = 0; i < getSizeInventory(); ++i) {
			if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
				stackList[i] = null;
			}
		}
		writeNBT(invItem.getTagCompound());
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	public void openInventory() {
	}

	public void closeInventory() {
	}

	@Override
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
