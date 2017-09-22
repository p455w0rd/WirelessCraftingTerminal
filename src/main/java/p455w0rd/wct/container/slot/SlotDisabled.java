package p455w0rd.wct.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotDisabled extends AppEngSlot {

	public SlotDisabled(final IItemHandler par1iInventory, final int slotIndex, final int x, final int y) {
		super(par1iInventory, slotIndex, x, y);
	}

	@Override
	public boolean isItemValid(final ItemStack par1ItemStack) {
		return false;
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}
}
