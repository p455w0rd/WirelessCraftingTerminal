package p455w0rd.wct.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotFake extends AppEngSlot {

	public SlotFake(final IItemHandler inv, final int idx, final int x, final int y) {
		super(inv, idx, x, y);
	}

	@Override
	public ItemStack onTake(final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack) {
		return par2ItemStack;
	}

	@Override
	public ItemStack decrStackSize(final int par1) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isItemValid(final ItemStack par1ItemStack) {
		return false;
	}

	@Override
	public void putStack(ItemStack is) {
		if (!is.isEmpty()) {
			is = is.copy();
		}

		super.putStack(is);
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}
}
