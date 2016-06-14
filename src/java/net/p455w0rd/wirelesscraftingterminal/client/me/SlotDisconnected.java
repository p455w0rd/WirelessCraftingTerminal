package net.p455w0rd.wirelesscraftingterminal.client.me;

import appeng.items.misc.ItemEncodedPattern;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.AppEngSlot;


public class SlotDisconnected extends AppEngSlot
{

	private final ClientDCInternalInv mySlot;

	public SlotDisconnected( final ClientDCInternalInv me, final int which, final int x, final int y )
	{
		super( me.getInventory(), which, x, y );
		this.mySlot = me;
	}

	@Override
	public boolean isItemValid( final ItemStack par1ItemStack )
	{
		return false;
	}

	@Override
	public void putStack( final ItemStack par1ItemStack )
	{

	}

	@Override
	public boolean canTakeStack( final EntityPlayer par1EntityPlayer )
	{
		return false;
	}

	@Override
	public ItemStack getDisplayStack()
	{
		if( Platform.isClient() )
		{
			final ItemStack is = super.getStack();
			if( is != null && is.getItem() instanceof ItemEncodedPattern )
			{
				final ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
				final ItemStack out = iep.getOutput( is );
				if( out != null )
				{
					return out;
				}
			}
		}
		return super.getStack();
	}

	@Override
	public void onPickupFromSlot( final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack )
	{
	}

	@Override
	public boolean getHasStack()
	{
		return this.getStack() != null;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 0;
	}

	@Override
	public ItemStack decrStackSize( final int par1 )
	{
		return null;
	}

	@Override
	public boolean isSlotInInventory( final IInventory par1iInventory, final int par2 )
	{
		return false;
	}

	public ClientDCInternalInv getSlot()
	{
		return this.mySlot;
	}
}
