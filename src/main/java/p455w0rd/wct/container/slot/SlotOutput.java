package p455w0rd.wct.container.slot;

import appeng.container.slot.AppEngSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotOutput extends AppEngSlot {

	public SlotOutput(final IInventory a, final int b, final int c, final int d, final int i) {
		super(a, b, c, d);
		setIIcon(i);
	}

	@Override
	public boolean isItemValid(final ItemStack i) {
		return false;
	}
}
