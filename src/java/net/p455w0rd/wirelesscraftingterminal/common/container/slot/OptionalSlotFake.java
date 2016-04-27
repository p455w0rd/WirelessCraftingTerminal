package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class OptionalSlotFake extends SlotFake
{

	private final int srcX;
	private final int srcY;
	private final int groupNum;
	private final IOptionalSlotHost host;
	private boolean renderDisabled = true;

	public OptionalSlotFake( final IInventory inv, final IOptionalSlotHost containerBus, final int idx, final int x, final int y, final int offX, final int offY, final int groupNum )
	{
		super( inv, idx, x + offX * 18, y + offY * 18 );
		this.srcX = x;
		this.srcY = y;
		this.groupNum = groupNum;
		this.host = containerBus;
	}

	@Override
	public ItemStack getStack()
	{
		if( !this.isEnabled() )
		{
			if( this.getDisplayStack() != null )
			{
				this.clearStack();
			}
		}

		return super.getStack();
	}

	@Override
	public boolean isEnabled()
	{
		if( this.host == null )
		{
			return false;
		}

		return this.host.isSlotEnabled( this.groupNum );
	}

	public boolean renderDisabled()
	{
		return this.isRenderDisabled();
	}

	private boolean isRenderDisabled()
	{
		return this.renderDisabled;
	}

	public void setRenderDisabled( final boolean renderDisabled )
	{
		this.renderDisabled = renderDisabled;
	}

	public int getSourceX()
	{
		return this.srcX;
	}

	public int getSourceY()
	{
		return this.srcY;
	}
}
