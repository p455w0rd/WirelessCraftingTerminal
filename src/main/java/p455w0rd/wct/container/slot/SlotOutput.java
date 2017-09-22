package p455w0rd.wct.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotOutput extends AppEngSlot {

	public SlotOutput(final IItemHandler a, final int b, final int c, final int d, final int i) {
		super(a, b, c, d);
		setIIcon(i);
	}

	@Override
	public boolean isItemValid(final ItemStack i) {
		return false;
	}
}
