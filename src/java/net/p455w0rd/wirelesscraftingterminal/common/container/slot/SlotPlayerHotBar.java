package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import net.minecraft.inventory.IInventory;

public class SlotPlayerHotBar extends AppEngSlot
{

	public SlotPlayerHotBar( final IInventory par1iInventory, final int par2, final int par3, final int par4 )
	{
		super( par1iInventory, par2, par3, par4 );
		this.setPlayerSide( true );
	}
}
