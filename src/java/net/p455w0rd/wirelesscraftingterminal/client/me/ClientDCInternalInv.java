package net.p455w0rd.wirelesscraftingterminal.client.me;

import javax.annotation.Nonnull;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ItemSorters;
import net.minecraft.util.StatCollector;


public class ClientDCInternalInv implements Comparable<ClientDCInternalInv>
{

	private final String unlocalizedName;
	private final AppEngInternalInventory inventory;

	private final long id;
	private final long sortBy;

	public ClientDCInternalInv( final int size, final long id, final long sortBy, final String unlocalizedName )
	{
		this.inventory = new AppEngInternalInventory( null, size );
		this.unlocalizedName = unlocalizedName;
		this.id = id;
		this.sortBy = sortBy;
	}

	public String getName()
	{
		final String s = StatCollector.translateToLocal( this.unlocalizedName + ".name" );
		if( s.equals( this.unlocalizedName + ".name" ) )
		{
			return StatCollector.translateToLocal( this.unlocalizedName );
		}
		return s;
	}

	@Override
	public int compareTo( @Nonnull final ClientDCInternalInv o )
	{
		return ItemSorters.compareLong( this.sortBy, o.sortBy );
	}

	public AppEngInternalInventory getInventory()
	{
		return this.inventory;
	}

	public long getId()
	{
		return this.id;
	}
}