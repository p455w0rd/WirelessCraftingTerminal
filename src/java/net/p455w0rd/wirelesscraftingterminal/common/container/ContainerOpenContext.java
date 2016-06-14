package net.p455w0rd.wirelesscraftingterminal.common.container;

import appeng.api.parts.IPart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;


public class ContainerOpenContext
{

	private final boolean isItem;
	private World w;
	private int x;
	private int y;
	private int z;
	private ForgeDirection side;

	public ContainerOpenContext( final Object myItem )
	{
		final boolean isWorld = myItem instanceof IPart || myItem instanceof TileEntity;
		this.isItem = !isWorld;
	}

	public TileEntity getTile()
	{
		if( this.isItem )
		{
			return null;
		}
		return this.getWorld().getTileEntity( this.getX(), this.getY(), this.getZ() );
	}

	public ForgeDirection getSide()
	{
		return this.side;
	}

	public void setSide( final ForgeDirection side )
	{
		this.side = side;
	}

	private int getZ()
	{
		return this.z;
	}

	public void setZ( final int z )
	{
		this.z = z;
	}

	private int getY()
	{
		return this.y;
	}

	public void setY( final int y )
	{
		this.y = y;
	}

	private int getX()
	{
		return this.x;
	}

	public void setX( final int x )
	{
		this.x = x;
	}

	private World getWorld()
	{
		return this.w;
	}

	public void setWorld( final World w )
	{
		this.w = w;
	}
}
