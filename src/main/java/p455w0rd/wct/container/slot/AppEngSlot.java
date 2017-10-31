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

import appeng.tile.inventory.AppEngInternalInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.container.WCTBaseContainer;

/**
 * @author p455w0rd
 *
 */
public class AppEngSlot extends Slot {

	private final int defX;
	private final int defY;
	private boolean isDraggable = true;
	private boolean isPlayerSide = false;
	private WCTBaseContainer myContainer = null;
	private int IIcon = -1;
	private hasCalculatedValidness isValid;
	private boolean isDisplay = false;

	public AppEngSlot(final IInventory inv, final int idx, final int x, final int y) {
		super(inv, idx, x, y);
		defX = x;
		defY = y;
		setIsValid(hasCalculatedValidness.NotAvailable);
	}

	public Slot setNotDraggable() {
		setDraggable(false);
		return this;
	}

	public Slot setPlayerSide() {
		isPlayerSide = true;
		return this;
	}

	public String getTooltip() {
		return null;
	}

	public void clearStack() {
		super.putStack(null);
	}

	@Override
	public boolean isItemValid(final ItemStack par1ItemStack) {
		if (isEnabled()) {
			return super.isItemValid(par1ItemStack);
		}
		return false;
	}

	@Override
	public ItemStack getStack() {
		if (!isEnabled()) {
			return null;
		}

		if (inventory.getSizeInventory() <= getSlotIndex()) {
			return null;
		}

		if (isDisplay()) {
			setDisplay(false);
			return getDisplayStack();
		}
		return super.getStack();
	}

	@Override
	public void putStack(final ItemStack par1ItemStack) {
		if (isEnabled()) {
			super.putStack(par1ItemStack);

			if (getContainer() != null) {
				getContainer().onSlotChange(this);
			}
		}
	}

	@Override
	public void onSlotChanged() {
		if (inventory instanceof AppEngInternalInventory) {
			((AppEngInternalInventory) inventory).markDirty(getSlotIndex());
		}
		else {
			super.onSlotChanged();
		}

		setIsValid(hasCalculatedValidness.NotAvailable);
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		if (isEnabled()) {
			return super.canTakeStack(par1EntityPlayer);
		}
		return false;
	}

	@Override
	public boolean canBeHovered() {
		return isEnabled();
	}

	public ItemStack getDisplayStack() {
		return super.getStack();
	}

	public boolean isEnabled() {
		return true;
	}

	public float getOpacityOfIcon() {
		return 0.4f;
	}

	public boolean renderIconWithItem() {
		return false;
	}

	public int getIcon() {
		return getIIcon();
	}

	public boolean isPlayerSide() {
		return isPlayerSide;
	}

	public boolean shouldDisplay() {
		return isEnabled();
	}

	public int getX() {
		return defX;
	}

	public int getY() {
		return defY;
	}

	private int getIIcon() {
		return IIcon;
	}

	public void setIIcon(final int iIcon) {
		IIcon = iIcon;
	}

	private boolean isDisplay() {
		return isDisplay;
	}

	public void setDisplay(final boolean isDisplay) {
		this.isDisplay = isDisplay;
	}

	public boolean isDraggable() {
		return isDraggable;
	}

	private void setDraggable(final boolean isDraggable) {
		this.isDraggable = isDraggable;
	}

	void setPlayerSide(final boolean isPlayerSide) {
		this.isPlayerSide = isPlayerSide;
	}

	public hasCalculatedValidness getIsValid() {
		return isValid;
	}

	public void setIsValid(final hasCalculatedValidness isValid) {
		this.isValid = isValid;
	}

	public WCTBaseContainer getContainer() {
		return myContainer;
	}

	public void setContainer(final WCTBaseContainer myContainer) {
		this.myContainer = myContainer;
	}

	public enum hasCalculatedValidness {
			NotAvailable, Valid, Invalid
	}
}