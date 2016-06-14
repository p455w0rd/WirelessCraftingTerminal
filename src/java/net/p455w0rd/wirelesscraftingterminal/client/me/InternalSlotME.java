package net.p455w0rd.wirelesscraftingterminal.client.me;

import appeng.api.storage.data.IAEItemStack;
import net.minecraft.item.ItemStack;


public class InternalSlotME
{

	private final int offset;
	private final int xPos;
	private final int yPos;
	private final ItemRepo repo;

	public InternalSlotME( final ItemRepo def, final int offset, final int displayX, final int displayY )
	{
		this.repo = def;
		this.offset = offset;
		this.xPos = displayX;
		this.yPos = displayY;
	}

	ItemStack getStack()
	{
		return this.repo.getItem( this.offset );
	}

	IAEItemStack getAEStack()
	{
		return this.repo.getReferenceItem( this.offset );
	}

	boolean hasPower()
	{
		return this.repo.hasPower();
	}

	int getxPosition()
	{
		return this.xPos;
	}

	int getyPosition()
	{
		return this.yPos;
	}
}
