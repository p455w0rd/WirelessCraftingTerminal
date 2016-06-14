package net.p455w0rd.wirelesscraftingterminal.common.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.ITerminalHost;
import net.minecraft.entity.player.InventoryPlayer;
import net.p455w0rd.wirelesscraftingterminal.common.container.guisync.GuiSync;


public class ContainerCraftingStatus extends ContainerCraftingCPU
{

	private final List<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();
	@GuiSync( 5 )
	public int selectedCpu = -1;
	@GuiSync( 6 )
	public boolean noCPU = true;
	@GuiSync( 7 )
	public String myName = "";

	public ContainerCraftingStatus( final InventoryPlayer ip, final ITerminalHost te )
	{
		super( ip, te );
	}

	@Override
	public void detectAndSendChanges()
	{
		final ICraftingGrid cc = this.getNetwork().getCache( ICraftingGrid.class );
		final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

		int matches = 0;
		boolean changed = false;
		for( final ICraftingCPU c : cpuSet )
		{
			boolean found = false;
			for( final CraftingCPURecord ccr : this.cpus )
			{
				if( ccr.getCpu() == c )
				{
					found = true;
				}
			}

			final boolean matched = this.cpuMatches( c );

			if( matched )
			{
				matches++;
			}

			if( found == !matched )
			{
				changed = true;
			}
		}

		if( changed || this.cpus.size() != matches )
		{
			this.cpus.clear();
			for( final ICraftingCPU c : cpuSet )
			{
				if( this.cpuMatches( c ) )
				{
					this.cpus.add( new CraftingCPURecord( c.getAvailableStorage(), c.getCoProcessors(), c ) );
				}
			}

			this.sendCPUs();
		}

		this.noCPU = this.cpus.isEmpty();

		super.detectAndSendChanges();
	}

	private boolean cpuMatches( final ICraftingCPU c )
	{
		return c.isBusy();
	}

	private void sendCPUs()
	{
		Collections.sort( this.cpus );

		if( this.selectedCpu >= this.cpus.size() )
		{
			this.selectedCpu = -1;
			this.myName = "";
		}
		else if( this.selectedCpu != -1 )
		{
			this.myName = this.cpus.get( this.selectedCpu ).getName();
		}

		if( this.selectedCpu == -1 && this.cpus.size() > 0 )
		{
			this.selectedCpu = 0;
		}

		if( this.selectedCpu != -1 )
		{
			if( this.cpus.get( this.selectedCpu ).getCpu() != this.getMonitor() )
			{
				this.setCPU( this.cpus.get( this.selectedCpu ).getCpu() );
			}
		}
		else
		{
			this.setCPU( null );
		}
	}

	public void cycleCpu( final boolean next )
	{
		if( next )
		{
			this.selectedCpu++;
		}
		else
		{
			this.selectedCpu--;
		}

		if( this.selectedCpu < -1 )
		{
			this.selectedCpu = this.cpus.size() - 1;
		}
		else if( this.selectedCpu >= this.cpus.size() )
		{
			this.selectedCpu = -1;
		}

		if( this.selectedCpu == -1 && this.cpus.size() > 0 )
		{
			this.selectedCpu = 0;
		}

		if( this.selectedCpu == -1 )
		{
			this.myName = "";
			this.setCPU( null );
		}
		else
		{
			this.myName = this.cpus.get( this.selectedCpu ).getName();
			this.setCPU( this.cpus.get( this.selectedCpu ).getCpu() );
		}
	}
}
