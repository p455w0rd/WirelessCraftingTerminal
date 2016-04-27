package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class SlotInaccessible extends AppEngSlot
{

	private ItemStack dspStack = null;

	public SlotInaccessible( final IInventory i, final int slotIdx, final int x, final int y )
	{
		super( i, slotIdx, x, y );
	}

	@Override
	public boolean isItemValid( final ItemStack i )
	{
		return false;
	}

	@Override
	public void onSlotChanged()
	{
		super.onSlotChanged();
		this.dspStack = null;
	}

	@Override
	public boolean canTakeStack( final EntityPlayer par1EntityPlayer )
	{
		return false;
	}

	@Override
	public ItemStack getDisplayStack()
	{
		if( this.dspStack == null )
		{
			final ItemStack dsp = super.getDisplayStack();
			if( dsp != null )
			{
				this.dspStack = dsp.copy();
			}
		}
		return this.dspStack;
	}
}
