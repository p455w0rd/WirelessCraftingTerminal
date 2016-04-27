package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class SlotCraftingMatrix extends AppEngSlot
{

	private final Container c;

	public SlotCraftingMatrix( final Container c, final IInventory par1iInventory, final int par2, final int par3, final int par4 )
	{
		super( par1iInventory, par2, par3, par4 );
		this.c = c;
	}

	@Override
	public void clearStack()
	{
		super.clearStack();
		this.c.onCraftMatrixChanged( this.inventory );
	}

	@Override
	public void putStack( final ItemStack par1ItemStack )
	{
		super.putStack( par1ItemStack );
		this.c.onCraftMatrixChanged( this.inventory );
	}

	@Override
	public boolean isPlayerSide()
	{
		return true;
	}

	@Override
	public ItemStack decrStackSize( final int par1 )
	{
		final ItemStack is = super.decrStackSize( par1 );
		this.c.onCraftMatrixChanged( this.inventory );
		return is;
	}
}
