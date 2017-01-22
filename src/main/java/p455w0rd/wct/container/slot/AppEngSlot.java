package p455w0rd.wct.container.slot;

import appeng.tile.inventory.AppEngInternalInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.container.*;

public class AppEngSlot extends Slot {

	private final int defX;
	private final int defY;
	private boolean isDraggable = true;
	private boolean isPlayerSide = false;
	private Container myContainer = null;
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
				if (getContainer() instanceof ContainerWCT) {
					((ContainerWCT) getContainer()).onSlotChange(this);
				}
				if (getContainer() instanceof WCTBaseContainer) {
					((WCTBaseContainer) getContainer()).onSlotChange(this);
				}
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

	Container getContainer() {
		if (myContainer instanceof ContainerWCT) {
			return myContainer;
		}
		if (myContainer instanceof WCTBaseContainer) {
			return myContainer;
		}
		return myContainer;
	}

	public void setContainer(final Container myContainer) {
		if (this.myContainer instanceof ContainerWCT) {
			this.myContainer = myContainer;
		}
		else if (this.myContainer instanceof WCTBaseContainer) {
			this.myContainer = myContainer;
		}
		else {
			this.myContainer = myContainer;
		}
	}

	public enum hasCalculatedValidness {
		NotAvailable, Valid, Invalid
	}
}