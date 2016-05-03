package net.p455w0rd.wirelesscraftingterminal.common.container;

import javax.annotation.Nonnull;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.util.ItemSorters;


public class CraftingCPURecord implements Comparable<CraftingCPURecord>
{

	private final String myName;
	private final ICraftingCPU cpu;
	private final long size;
	private final int processors;

	public CraftingCPURecord( final long size, final int coProcessors, final ICraftingCPU server )
	{
		this.size = size;
		this.processors = coProcessors;
		this.cpu = server;
		this.myName = server.getName();
	}

	@Override
	public int compareTo( @Nonnull final CraftingCPURecord o )
	{
		final int a = ItemSorters.compareLong( o.getProcessors(), this.getProcessors() );
		if( a != 0 )
		{
			return a;
		}
		return ItemSorters.compareLong( o.getSize(), this.getSize() );
	}

	public ICraftingCPU getCpu()
	{
		return this.cpu;
	}

	String getName()
	{
		return this.myName;
	}

	int getProcessors()
	{
		return this.processors;
	}

	long getSize()
	{
		return this.size;
	}
}