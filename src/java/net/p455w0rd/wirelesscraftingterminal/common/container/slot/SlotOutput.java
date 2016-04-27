package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class SlotOutput extends AppEngSlot
{

	public SlotOutput( final IInventory a, final int b, final int c, final int d, final int i )
	{
		super( a, b, c, d );
		this.setIIcon( i );
	}

	@Override
	public boolean isItemValid( final ItemStack i )
	{
		return false;
	}
}
