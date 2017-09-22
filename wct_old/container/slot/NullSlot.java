package p455w0rd.wct.container.slot;

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
	public boolean isItemValid(final ItemStack par1ItemStack) {
		return false;
	}

	@Override
	public ItemStack getStack() {
		return null;
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
		return null;
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
