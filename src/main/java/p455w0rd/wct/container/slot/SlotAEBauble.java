package p455w0rd.wct.container.slot;

import baubles.api.IBauble;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * @author p455w0rd
 *
 */
public class SlotAEBauble extends AppEngSlot {

	public SlotAEBauble(IInventory inv, int idx, int x, int y) {
		super(new InvWrapper(inv), idx, x, y);
	}

	@Override
	public boolean isItemValid(final ItemStack stack) {
		if (stack != null && stack.getItem() instanceof IBauble) {
			IBauble bauble = (IBauble) stack.getItem();
			return bauble.getBaubleType(stack).hasSlot(getSlotIndex());
		}
		return false;
	}

}
