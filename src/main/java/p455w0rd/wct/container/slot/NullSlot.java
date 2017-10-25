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
package p455w0rd.wct.container.slot;

import javax.annotation.Nonnull;

import appeng.container.slot.AppEngSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class NullSlot extends AppEngSlot {

	public NullSlot() {
		super(null, 0, 0, 0);
	}

	@Override
	public void onSlotChange(final ItemStack par1ItemStack, final ItemStack par2ItemStack) {

	}

	@Override
	public ItemStack onTake(final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack) {
		return par2ItemStack;
	}

	@Override
	public boolean isItemValid(final ItemStack par1ItemStack) {
		return false;
	}

	@Override
	@Nonnull
	public ItemStack getStack() {
		return ItemStack.EMPTY;
	}

	@Override
	public void putStack(final ItemStack par1ItemStack) {

	}

	@Override
	public void onSlotChanged() {

	}

	@Override
	public int getSlotStackLimit() {
		return 0;
	}

	@Override
	public ItemStack decrStackSize(final int par1) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}

	@Override
	public int getSlotIndex() {
		return 0;
	}
}
