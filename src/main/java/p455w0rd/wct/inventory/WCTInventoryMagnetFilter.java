/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import p455w0rd.wct.container.ContainerMagnet;

public class WCTInventoryMagnetFilter implements IInventory {

	private String name = "MagnetFilter";
	private final ItemStack magnet;
	private NonNullList<ItemStack> inventory;
	private ContainerMagnet container;

	public WCTInventoryMagnetFilter(ItemStack stack) {

		inventory = NonNullList.withSize(27, ItemStack.EMPTY);
		magnet = stack;
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		readFromNBT(stack.getTagCompound());
	}

	public void setContainer(ContainerMagnet container) {
		this.container = container;
	}

	@Override
	public int getSizeInventory() {
		return inventory.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory.get(slot);
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
		return 1;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.getCount() > amount) {
				stack = stack.splitStack(amount);
				markDirty();
			}
			else {
				setInventorySlotContents(slot, ItemStack.EMPTY);
			}
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory.set(slot, stack);

		if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
		}
		markDirty();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void markDirty() {
		writeNBT(magnet.getTagCompound());
		container.detectAndSendChanges();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return true;
	}

	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		NBTTagList tagList = nbtTagCompound.getTagList(name, 10);
		inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
			int slot = tagCompound.getInteger("Slot");
			if (slot >= 0 && slot < inventory.size()) {
				inventory.set(slot, new ItemStack(tagCompound));
			}
		}
	}

	public void writeNBT(NBTTagCompound nbtTagCompound) {
		if (nbtTagCompound == null) {
			nbtTagCompound = new NBTTagCompound();
		}
		NBTTagList tagList = new NBTTagList();
		for (int currentIndex = 0; currentIndex < inventory.size(); ++currentIndex) {
			if (!inventory.get(currentIndex).isEmpty()) {
				NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setInteger("Slot", currentIndex);
				inventory.get(currentIndex).writeToNBT(tagCompound);
				tagList.appendTag(tagCompound);
			}
		}
		nbtTagCompound.setTag(name, tagList);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(name);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStack.EMPTY;
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
		for (int i = 0; i < inventory.size(); i++) {
			setInventorySlotContents(i, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < inventory.size(); i++) {
			if (!getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
