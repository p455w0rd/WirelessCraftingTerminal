package p455w0rd.wct.container.slot;

import appeng.util.inv.WrapperInvItemHandler;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotCraftingMatrix extends AppEngSlot {

	private final Container c;
	private final IInventory wrappedInventory;

	public SlotCraftingMatrix(final Container c, final IItemHandler par1iInventory, final int par2, final int par3, final int par4) {
		super(par1iInventory, par2, par3, par4);
		this.c = c;
		wrappedInventory = new WrapperInvItemHandler(par1iInventory);
	}

	@Override
	public void clearStack() {
		super.clearStack();
		c.onCraftMatrixChanged(wrappedInventory);
	}

	@Override
	public void putStack(final ItemStack par1ItemStack) {
		super.putStack(par1ItemStack);
		c.onCraftMatrixChanged(wrappedInventory);
	}

	@Override
	public boolean isPlayerSide() {
		return true;
	}

	@Override
	public ItemStack decrStackSize(final int par1) {
		final ItemStack is = super.decrStackSize(par1);
		c.onCraftMatrixChanged(wrappedInventory);
		return is;
	}
}
