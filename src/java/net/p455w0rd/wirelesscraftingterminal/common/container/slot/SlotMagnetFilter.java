package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

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
		if (inventory == null) {
			throw new IllegalArgumentException("Inventory must not be null");
		}
		this.stackLimit = -1;
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
	public void putStack(ItemStack itemStack) {
		if (!isPhantom() || canAdjustPhantom()) {
			super.putStack(itemStack);
		}
	}

	public SlotMagnetFilter setCanAdjustPhantom(boolean canAdjust) {
		this.canAdjustPhantom = canAdjust;
		return this;
	}

	public SlotMagnetFilter setStackLimit(int limit) {
		this.stackLimit = limit;
		return this;
	}

	public boolean isPhantom() {
		return this.isPhantom;
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
		} else {
			return stackLimit;
		}
	}
}
