package p455w0rd.wct.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotInaccessible extends AppEngSlot {

	private ItemStack dspStack = null;

	public SlotInaccessible(final IInventory i, final int slotIdx, final int x, final int y) {
		super(i, slotIdx, x, y);
	}

	@Override
	public boolean isItemValid(final ItemStack i) {
		return false;
	}

	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		dspStack = null;
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}

	@Override
	public ItemStack getDisplayStack() {
		if (dspStack == null) {
			final ItemStack dsp = super.getDisplayStack();
			if (dsp != null) {
				dspStack = dsp.copy();
				dspStack.stackSize = 1;
			}
		}
		return dspStack;
	}
}
