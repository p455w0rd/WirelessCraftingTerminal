package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class SlotFake extends AppEngSlot
{

	public SlotFake( final IInventory inv, final int idx, final int x, final int y )
	{
		super( inv, idx, x, y );
	}

	@Override
	public void onPickupFromSlot( final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack )
	{
	}

	@Override
	public ItemStack decrStackSize( final int par1 )
	{
		return null;
	}

	@Override
	public boolean isItemValid( final ItemStack par1ItemStack )
	{
		return false;
	}

	@Override
	public void putStack( ItemStack is )
	{
		if( is != null )
		{
			is = is.copy();
		}

		super.putStack( is );
	}

	@Override
	public boolean canTakeStack( final EntityPlayer par1EntityPlayer )
	{
		return false;
	}
}
