package p455w0rd.wct.container.slot;

import net.minecraft.inventory.IInventory;

public class SlotTrash extends AppEngSlot {

	public SlotTrash(IInventory inv, int x, int y) {
		super(inv, 0, x, y);
	}

	/*
	public SlotTrash(IInventory inv, int x, int y, EntityPlayer player) {
		super(null, 0, x, y);
		entityPlayer = player;
		readNBT();
	}

	@Override
	public ItemStack getStack() {
		return itemStack;
	}

	@Override
	public boolean getHasStack() {
		return itemStack != null;
	}

	@Override
	public void putStack(ItemStack itemStack) {
		shouldDeleteMouseStack = (this.itemStack != null);
		this.itemStack = itemStack;
		writeNBT();
	}

	@Override
	public int getSlotStackLimit() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void onSlotChanged() {
		if (itemStack != null && shouldDeleteMouseStack) {
			entityPlayer.inventory.setItemStack(null);
		}
		writeNBT();
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return true;
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		if (itemStack != null) {
			ItemStack returnStack;
			if (itemStack.stackSize <= amount) {
				returnStack = itemStack;
				itemStack = null;
				shouldDeleteMouseStack = true;
				writeNBT();
				return returnStack;
			}
			else {
				returnStack = itemStack.splitStack(amount);
				shouldDeleteMouseStack = false;
				if (itemStack.stackSize == 0) {
					itemStack = null;
					shouldDeleteMouseStack = true;
				}
				writeNBT();
				return returnStack;
			}
		}
		return null;
	}

	public void readNBT() {
		NBTTagCompound nbtTagCompound = WCTUtils.getWirelessTerm(entityPlayer.inventory).getTagCompound();
		NBTTagList tagList = nbtTagCompound.getTagList(name, 10);
		NBTTagCompound tagCompound = tagList.getCompoundTagAt(0);
		if (tagCompound != null) {
			itemStack = ItemStack.loadItemStackFromNBT(tagCompound);
		}
	}

	public void writeNBT() {
		ItemStack terminal = WCTUtils.getWirelessTerm(entityPlayer.inventory);
		NBTTagCompound nbtTagCompound = terminal != null ? terminal.getTagCompound() : null;
		if (nbtTagCompound == null) {
			nbtTagCompound = new NBTTagCompound();
		}
		NBTTagList tagList = new NBTTagList();
		nbtTagCompound.setTag(name, tagList);
		if (itemStack != null) {
			NBTTagCompound tagCompound = new NBTTagCompound();
			itemStack.writeToNBT(tagCompound);
			tagList.appendTag(tagCompound);
		}
		if (getContainer() != null && (getContainer() instanceof ContainerWCT)) {
			if (Mods.BAUBLES.isLoaded()) {
				if (Platform.isClient()) {
					Baubles.doForcedSync(entityPlayer, terminal);
				}
			}
		}
	}

	@Override
	public void clearStack() {
		if (itemStack != null) {
			itemStack = null;
		}
		writeNBT();
	}
	*/
}