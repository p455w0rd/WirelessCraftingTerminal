package p455w0rd.wct.inventory;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class WCTInventoryTrash extends AppEngInternalInventory {

	private String name = "TrashSlot";
	private final ItemStack invItem;
	private ItemStack[] inventory;
	private IAEAppEngInventory aeInv;
	private boolean enableClientEvents = true;

	public WCTInventoryTrash(Container container, ItemStack stack) {
		super((IAEAppEngInventory) container, 1);
		inventory = new ItemStack[1];
		invItem = stack;
		setTileEntity((IAEAppEngInventory) container);
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
		for (int x = 0; x < getSizeInventory(); x++) {
			if (getStackInSlot(x) != null) {
				return false;
			}
		}
		return true;
	}

	private IAEAppEngInventory getTileEntity() {
		return aeInv;
	}

	@Override
	public void setTileEntity(final IAEAppEngInventory aeInv) {
		this.aeInv = aeInv;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot >= getSizeInventory() ? null : inventory[slot];
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean hasCustomName() {
		return name.length() > 0;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public ItemStack decrStackSize(final int slot, final int qty) {
		if (inventory[slot] != null) {
			final ItemStack split = getStackInSlot(slot);
			ItemStack ns = null;
			if (qty >= split.stackSize) {
				ns = inventory[slot];
				inventory[slot] = null;
			}
			else {
				ns = split.splitStack(qty);
			}
			this.markDirty();
			return ns;
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		//eventHandler.onCraftMatrixChanged(this);
		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
		markDirty();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void markDirty() {
		if (getTileEntity() != null && eventsEnabled()) {
			getTileEntity().onChangeInventory(this, -1, InvOperation.markDirty, null, null);
		}
		for (int i = 0; i < getSizeInventory(); ++i) {
			if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
				inventory[i] = null;
			}
		}
		writeNBT(invItem.getTagCompound());
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return true;
	}

	public NBTTagCompound writeNBT(NBTTagCompound nbtTagCompound) {
		// Write the ItemStacks in the inventory to NBT
		NBTTagList tagList = new NBTTagList();
		for (int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
			if (inventory[currentIndex] != null) {
				NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setByte("Slot", (byte) currentIndex);
				inventory[currentIndex].writeToNBT(tagCompound);
				tagList.appendTag(tagCompound);
			}
		}
		nbtTagCompound.setTag("CraftingMatrix", tagList);
		return nbtTagCompound;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(name);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return null;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	}
}
