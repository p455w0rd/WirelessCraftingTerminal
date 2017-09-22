package p455w0rd.wct.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotInaccessible extends AppEngSlot {

	private ItemStack dspStack = ItemStack.EMPTY;

	public SlotInaccessible(final IItemHandler i, final int slotIdx, final int x, final int y) {
		super(i, slotIdx, x, y);
	}

	@Override
	public boolean isItemValid(final ItemStack i) {
		return false;
	}

	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		dspStack = ItemStack.EMPTY;
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}

	@Override
	public ItemStack getDisplayStack() {
		if (dspStack.isEmpty()) {
			final ItemStack dsp = super.getDisplayStack();
			if (!dsp.isEmpty()) {
				dspStack = dsp.copy();
			}
		}
		return dspStack;
	}
}
