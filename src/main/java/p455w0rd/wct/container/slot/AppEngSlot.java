package p455w0rd.wct.container.slot;

import javax.annotation.Nonnull;

import appeng.util.helpers.ItemHandlerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.WCTBaseContainer;

public class AppEngSlot extends Slot {

	private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
	private final IItemHandler itemHandler;
	private final int index;

	private final int defX;
	private final int defY;
	private boolean isDraggable = true;
	private boolean isPlayerSide = false;
	private Container myContainer = null;
	private int IIcon = -1;
	private hasCalculatedValidness isValid;
	private boolean isDisplay = false;

	public AppEngSlot(final IItemHandler inv, final int idx, final int x, final int y) {
		super(emptyInventory, idx, x, y);
		itemHandler = inv;
		index = idx;

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
		ItemHandlerUtil.setStackInSlot(itemHandler, index, ItemStack.EMPTY);
	}

	@Override
	public boolean isItemValid(final ItemStack par1ItemStack) {
		if (isSlotEnabled()) {
			return ItemHandlerUtil.isItemValidForSlot(itemHandler, index, par1ItemStack);
		}
		return false;
	}

	@Override
	@Nonnull
	public ItemStack getStack() {
		if (!isSlotEnabled()) {
			return ItemStack.EMPTY;
		}

		if (itemHandler.getSlots() <= getSlotIndex()) {
			return ItemStack.EMPTY;
		}

		if (isDisplay()) {
			setDisplay(false);
			return getDisplayStack();
		}

		return itemHandler.getStackInSlot(index);
	}

	@Override
	public void putStack(final ItemStack stack) {
		if (isSlotEnabled()) {
			ItemHandlerUtil.setStackInSlot(itemHandler, index, stack);
			onSlotChanged();

			if (getContainer() != null) {
				if (getContainer() instanceof WCTBaseContainer) {
					((WCTBaseContainer) getContainer()).onSlotChange(this);
				}
				else if (getContainer() instanceof ContainerWCT) {
					((ContainerWCT) getContainer()).onSlotChange(this);
				}
			}
		}
	}

	public IItemHandler getItemHandler() {
		return itemHandler;
	}

	@Override
	public void onSlotChanged() {
		ItemHandlerUtil.markDirty(itemHandler, index);
		setIsValid(hasCalculatedValidness.NotAvailable);
	}

	@Override
	public int getSlotStackLimit() {
		return itemHandler.getSlotLimit(index);
	}

	@Override
	public int getItemStackLimit(@Nonnull ItemStack stack) {
		return Math.min(getSlotStackLimit(), stack.getMaxStackSize());
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		if (isSlotEnabled()) {
			return !itemHandler.extractItem(index, 1, true).isEmpty();
		}
		return false;
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int amount) {
		return itemHandler.extractItem(index, amount, false);
	}

	@Override
	public boolean isSameInventory(Slot other) {
		return other instanceof AppEngSlot && ((AppEngSlot) other).itemHandler == itemHandler;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isEnabled() {
		return isSlotEnabled();
	}

	public boolean isSlotEnabled() {
		return true;
	}

	public ItemStack getDisplayStack() {
		return itemHandler.getStackInSlot(index);
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
		return isSlotEnabled();
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

	public AppEngSlot setContainer(final Container myContainer) {
		if (this.myContainer instanceof ContainerWCT) {
			this.myContainer = myContainer;
		}
		else if (this.myContainer instanceof WCTBaseContainer) {
			this.myContainer = myContainer;
		}
		else {
			this.myContainer = myContainer;
		}
		return this;
	}

	public enum hasCalculatedValidness {
			NotAvailable, Valid, Invalid
	}
}