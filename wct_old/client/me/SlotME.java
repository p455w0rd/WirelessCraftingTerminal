package p455w0rd.wct.client.me;

import appeng.api.storage.data.IAEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotME extends Slot {

	private final InternalSlotME mySlot;

	public SlotME(final InternalSlotME me) {
		super(null, 0, me.getxPosition(), me.getyPosition());
		mySlot = me;
	}

	public IAEItemStack getAEStack() {
		if (mySlot.hasPower()) {
			return mySlot.getAEStack();
		}
		return null;
	}

	@Override
	public boolean isItemValid(final ItemStack par1ItemStack) {
		return false;
	}

	@Override
	public ItemStack getStack() {
		if (mySlot.hasPower()) {
			return mySlot.getStack();
		}
		return null;
	}

	@Override
	public boolean getHasStack() {
		if (mySlot.hasPower()) {
			return getStack() != null;
		}
		return false;
	}

	@Override
	public void putStack(final ItemStack par1ItemStack) {

	}

	@Override
	public int getSlotStackLimit() {
		return 0;
	}

	@Override
	public ItemStack decrStackSize(final int par1) {
		return null;
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}

}
