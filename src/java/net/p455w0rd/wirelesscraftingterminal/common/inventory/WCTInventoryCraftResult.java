package net.p455w0rd.wirelesscraftingterminal.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class WCTInventoryCraftResult implements IInventory {

	private ItemStack[] stackResult = new ItemStack[1];

	public int getSizeInventory() {
		return 1;
	}

	public ItemStack getStackInSlot(int slot) {
		return this.stackResult[0];
	}

	public String getInventoryName() {
		return "CraftResult";
	}

	public boolean hasCustomInventoryName() {
		return true;
	}

	public ItemStack decrStackSize(int slot, int amount) {
		if (this.stackResult[0] != null) {
			ItemStack itemstack = this.stackResult[0];
			this.stackResult[0] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	public ItemStack getStackInSlotOnClosing(int slot) {
		if (this.stackResult[0] != null) {
			ItemStack itemstack = this.stackResult[0];
			this.stackResult[0] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	public void setInventorySlotContents(int slot, ItemStack is) {
		this.stackResult[0] = is;
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	public void markDirty() {
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	public void openInventory() {
	}

	public void closeInventory() {
	}

	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return true;
	}

}
