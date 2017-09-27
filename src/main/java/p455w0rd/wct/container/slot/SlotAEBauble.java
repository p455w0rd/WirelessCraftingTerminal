package p455w0rd.wct.container.slot;

import baubles.api.IBauble;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.container.WCTBaseContainer;
import p455w0rd.wct.items.ItemWCT;

/**
 * @author p455w0rd
 *
 */
public class SlotAEBauble extends AppEngSlot {

	public SlotAEBauble(IInventory inv, int idx, int x, int y) {
		super(inv, idx, x, y);
	}

	@Override
	public boolean canTakeStack(final EntityPlayer player) {
		if (player == null || getStack() == null) {
			return false;
		}
		return (!(!(player.openContainer instanceof WCTBaseContainer)) && (!(getStack().getItem() instanceof ItemWCT)));
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
