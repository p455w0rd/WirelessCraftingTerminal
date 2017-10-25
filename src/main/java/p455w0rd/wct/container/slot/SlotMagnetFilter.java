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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotMagnetFilter extends Slot {

	private boolean isPhantom;
	private boolean canAdjustPhantom = true;
	private boolean canShift = true;
	private int stackLimit;

	public SlotMagnetFilter(IInventory inventory, int slotIndex, int xPos, int yPos) {
		super(inventory, slotIndex, xPos, yPos);
		stackLimit = -1;
	}

	public SlotMagnetFilter setPhantom() {
		isPhantom = true;
		return this;
	}

	public SlotMagnetFilter blockShift() {
		canShift = false;
		return this;
	}

	@Override
	public void putStack(@Nonnull ItemStack itemStack) {
		if (!isPhantom() || canAdjustPhantom()) {
			super.putStack(itemStack);
		}
	}

	public SlotMagnetFilter setCanAdjustPhantom(boolean canAdjust) {
		canAdjustPhantom = canAdjust;
		return this;
	}

	public SlotMagnetFilter setStackLimit(int limit) {
		stackLimit = limit;
		return this;
	}

	public boolean isPhantom() {
		return isPhantom;
	}

	public boolean canAdjustPhantom() {
		return canAdjustPhantom;
	}

	@Override
	public boolean canTakeStack(EntityPlayer stack) {
		return !isPhantom();
	}

	public boolean canShift() {
		return canShift;
	}

	@Override
	public int getSlotStackLimit() {
		if (stackLimit < 0) {
			return super.getSlotStackLimit();
		}
		else {
			return stackLimit;
		}
	}
}
