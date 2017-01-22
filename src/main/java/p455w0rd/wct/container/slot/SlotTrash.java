package p455w0rd.wct.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import p455w0rd.wct.util.WCTUtils;

public class SlotTrash extends AppEngSlot {

	private final EntityPlayer entityPlayer;
	private ItemStack itemStack;
	private boolean shouldDeleteMouseStack;
	private String name = "TrashSlot";

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
		NBTTagCompound nbtTagCompound = WCTUtils.getWirelessTerm(entityPlayer.inventory).getTagCompound();
		NBTTagList tagList = new NBTTagList();
		if (itemStack != null) {
			NBTTagCompound tagCompound = new NBTTagCompound();
			itemStack.writeToNBT(tagCompound);
			tagList.appendTag(tagCompound);
		}
		nbtTagCompound.setTag(name, tagList);
	}

	@Override
	public void clearStack() {
		if (itemStack != null) {
			itemStack = null;
		}
	}
}