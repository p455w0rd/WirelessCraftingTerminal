package net.p455w0rd.wirelesscraftingterminal.client.me;

import appeng.api.storage.data.IAEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;


public class SlotME extends Slot
{

	private final InternalSlotME mySlot;

	public SlotME( final InternalSlotME me )
	{
		super( null, 0, me.getxPosition(), me.getyPosition() );
		this.mySlot = me;
	}

	public IAEItemStack getAEStack()
	{
		if( this.mySlot.hasPower() )
		{
			return this.mySlot.getAEStack();
		}
		return null;
	}

	@Override
	public void onPickupFromSlot( final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack )
	{
	}

	@Override
	public boolean isItemValid( final ItemStack par1ItemStack )
	{
		return false;
	}

	@Override
	public ItemStack getStack()
	{
		if( this.mySlot.hasPower() )
		{
			return this.mySlot.getStack();
		}
		return null;
	}

	@Override
	public boolean getHasStack()
	{
		if( this.mySlot.hasPower() )
		{
			return this.getStack() != null;
		}
		return false;
	}

	@Override
	public void putStack( final ItemStack par1ItemStack )
	{

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

	@Override
	public boolean canTakeStack( final EntityPlayer par1EntityPlayer )
	{
		return false;
	}
	
}
