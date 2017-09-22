package p455w0rd.wct.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.util.WCTUtils;

public class SlotTrash extends AppEngSlot {

	private final EntityPlayer entityPlayer;
	private ItemStack itemStack;
	private boolean shouldDeleteMouseStack;
	private String name = "TrashSlot";

	public SlotTrash(IItemHandler inv, int x, int y, EntityPlayer player) {
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
			if (itemStack.getCount() <= amount) {
				returnStack = itemStack;
				itemStack = null;
				shouldDeleteMouseStack = true;
				writeNBT();
				return returnStack;
			}
			else {
				returnStack = itemStack.splitStack(amount);
				shouldDeleteMouseStack = false;
				if (itemStack.getCount() == 0) {
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
			itemStack = new ItemStack(tagCompound);
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

	}

	@Override
	public void clearStack() {
		if (itemStack != null) {
			itemStack = null;
		}
		writeNBT();
	}
}