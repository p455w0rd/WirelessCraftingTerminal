package net.p455w0rd.wirelesscraftingterminal.common.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.WCTIActionHost;
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.WCTPlayerSource;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.common.container.guisync.GuiSync;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketMEInventoryUpdate;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSetJobBytes;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketUpdateCPUInfo;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;


public class ContainerCraftConfirm extends WCTBaseContainer
{

	public final ArrayList<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();
	private Future<ICraftingJob> job;
	private ICraftingJob result;
	@GuiSync( 0 )
	public long bytesUsed;
	@GuiSync( 1 )
	public long cpuBytesAvail;
	@GuiSync( 2 )
	public int cpuCoProcessors;
	@GuiSync( 3 )
	public boolean autoStart = false;
	@GuiSync( 4 )
	public boolean simulation = true;
	@GuiSync( 5 )
	public int selectedCpu = -1;
	@GuiSync( 6 )
	public boolean noCPU = true;
	@GuiSync( 7 )
	public String myName = "";
	InventoryPlayer inventoryPlayer;

	public ContainerCraftConfirm( final InventoryPlayer ip, final ITerminalHost te )
	{
		super( ip, te );
		inventoryPlayer = ip;
	}

	public void cycleCpu( final boolean next )
	{
		if( next )
		{
			this.setSelectedCpu( this.getSelectedCpu() + 1 );
		}
		else
		{
			this.setSelectedCpu( this.getSelectedCpu() - 1 );
		}

		if( this.getSelectedCpu() < -1 )
		{
			this.setSelectedCpu( this.cpus.size() - 1 );
		}
		else if( this.getSelectedCpu() >= this.cpus.size() )
		{
			this.setSelectedCpu( -1 );
		}

		if( this.getSelectedCpu() == -1 )
		{
			this.setCpuAvailableBytes( 0 );
			this.setCpuCoProcessors( 0 );
			this.setName( "" );
			try {
				NetworkHandler.instance.sendTo(new PacketUpdateCPUInfo(0, 0), (EntityPlayerMP) this.getPlayerInv().player);
			}
			catch (IOException e) {
				// 
			}
		}
		else {
			this.setName( this.cpus.get( this.getSelectedCpu() ).getName() );
			this.setCpuAvailableBytes( this.cpus.get( this.getSelectedCpu() ).getSize() );
			this.setCpuCoProcessors( this.cpus.get( this.getSelectedCpu() ).getProcessors() );
			
			try {
				NetworkHandler.instance.sendTo(new PacketUpdateCPUInfo((int)this.getCpuAvailableBytes(), (int)this.getCpuCoProcessors()), (EntityPlayerMP) this.getPlayerInv().player);
			}
			catch (IOException e) {
				// 
			}
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		if( Platform.isClient() )
		{
			return;
		}

		final ICraftingGrid cc = this.getGrid().getCache( ICraftingGrid.class );
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

		this.setNoCPU( this.cpus.isEmpty() );

		super.detectAndSendChanges();

		if( this.getJob() != null && this.getJob().isDone() )
		{
			try
			{
				this.result = this.getJob().get();

				if( !this.result.isSimulation() )
				{
					this.setSimulation( false );
					if( this.isAutoStart() )
					{
						this.startJob();
						return;
					}
				}
				else
				{
					this.setSimulation( true );
				}

				try
				{
					final PacketMEInventoryUpdate a = new PacketMEInventoryUpdate( (byte) 0 );
					final PacketMEInventoryUpdate b = new PacketMEInventoryUpdate( (byte) 1 );
					final PacketMEInventoryUpdate c = this.result.isSimulation() ? new PacketMEInventoryUpdate( (byte) 2 ) : null;

					final IItemList<IAEItemStack> plan = AEApi.instance().storage().createItemList();
					this.result.populatePlan( plan );
					
					try {
						NetworkHandler.instance.sendTo(new PacketSetJobBytes((int)this.result.getByteTotal()), (EntityPlayerMP) this.getPlayerInv().player);
					}
					catch (IOException e) {
						// 
					}

					this.setUsedBytes( this.result.getByteTotal() );

					for( final IAEItemStack out : plan )
					{

						IAEItemStack o = out.copy();
						o.reset();
						o.setStackSize( out.getStackSize() );

						final IAEItemStack p = out.copy();
						p.reset();
						p.setStackSize( out.getCountRequestable() );

						final IStorageGrid sg = this.getGrid().getCache( IStorageGrid.class );
						final IMEInventory<IAEItemStack> items = sg.getItemInventory();

						IAEItemStack m = null;
						if( c != null && this.result.isSimulation() )
						{
							m = o.copy();
							o = items.extractItems( o, Actionable.SIMULATE, this.getActionSource() );

							if( o == null )
							{
								o = m.copy();
								o.setStackSize( 0 );
							}

							m.setStackSize( m.getStackSize() - o.getStackSize() );
						}

						if( o.getStackSize() > 0 )
						{
							a.appendItem( o );
						}

						if( p.getStackSize() > 0 )
						{
							b.appendItem( p );
						}

						if( c != null && m != null && m.getStackSize() > 0 )
						{
							c.appendItem( m );
						}
					}

					for( final Object g : this.crafters )
					{
						if( g instanceof EntityPlayer )
						{
							NetworkHandler.instance.sendTo( a, (EntityPlayerMP) g );
							NetworkHandler.instance.sendTo( b, (EntityPlayerMP) g );
							if( c != null )
							{
								NetworkHandler.instance.sendTo( c, (EntityPlayerMP) g );
							}
						}
					}
				}
				catch( final IOException e )
				{
					// :P
				}
			}
			catch( final Throwable e )
			{
				this.getPlayerInv().player.addChatMessage( new ChatComponentText( "Error: " + e.toString() ) );
				WCTLog.debug( e.getMessage() );
				this.setValidContainer( false );
				this.result = null;
			}

			this.setJob( null );
		}
		this.verifyPermissions( SecurityPermissions.CRAFT, false );
	}

	private IGrid getGrid()
	{
		return this.obj2.getTargetGrid();
	}

	private boolean cpuMatches( final ICraftingCPU c )
	{
		return c.getAvailableStorage() >= this.getUsedBytes() && !c.isBusy();
	}

	private void sendCPUs()
	{
		Collections.sort( this.cpus );

		if( this.getSelectedCpu() >= this.cpus.size() )
		{
			this.setSelectedCpu( -1 );
			this.setCpuAvailableBytes( 0 );
			this.setCpuCoProcessors( 0 );
			this.setName( "" );
		}
		else if( this.getSelectedCpu() != -1 )
		{
			this.setName( this.cpus.get( this.getSelectedCpu() ).getName() );
			this.setCpuAvailableBytes( this.cpus.get( this.getSelectedCpu() ).getSize() );
			this.setCpuCoProcessors( this.cpus.get( this.getSelectedCpu() ).getProcessors() );
		}
	}

	public void startJob()
	{
		int originalGui=0;

		final WCTIActionHost ah = this.getActionHost();
		if( ah instanceof WirelessTerminalGuiObject )
		{
			originalGui = Reference.GUI_WCT;
		}

		if( this.result != null && !this.isSimulation() )
		{
			final ICraftingGrid cc = this.getGrid().getCache( ICraftingGrid.class );
			final ICraftingLink g = cc.submitJob( this.result, null, this.getSelectedCpu() == -1 ? null : this.cpus.get( this.getSelectedCpu() ).getCpu(), true, this.getActionSrc() );
			this.setAutoStart( false );
			if( g != null && originalGui > 0)// && this.getOpenContext() != null )
			{
				NetworkHandler.instance.sendTo( new PacketSwitchGuis( originalGui ), (EntityPlayerMP) this.getInventoryPlayer().player );

				//final TileEntity te = this.getOpenContext().getTile();
				//Platform.openGUI( this.getInventoryPlayer().player, te, this.getOpenContext().getSide(), originalGui );
				EntityPlayerMP player = (EntityPlayerMP) this.getInventoryPlayer().player;
				World world = player.worldObj;
				int x = (int)player.posX;
				int y = (int)player.posY;
				int z = (int)player.posZ;
				WCTGuiHandler.launchGui(originalGui, player, world, x, y, z);
			}
		}
	}

	private BaseActionSource getActionSrc()
	{
		return new WCTPlayerSource( this.getPlayerInv().player, (WCTIActionHost) this.getTarget() );
	}

	@Override
	public void removeCraftingFromCrafters( final ICrafting c )
	{
		super.removeCraftingFromCrafters( c );
		if( this.getJob() != null )
		{
			this.getJob().cancel( true );
			this.setJob( null );
		}
	}

	@Override
	public void onContainerClosed( final EntityPlayer par1EntityPlayer )
	{
		super.onContainerClosed( par1EntityPlayer );
		if( this.getJob() != null )
		{
			this.getJob().cancel( true );
			this.setJob( null );
		}
	}

	public World getWorld()
	{
		return this.getPlayerInv().player.worldObj;
	}

	public boolean isAutoStart()
	{
		return this.autoStart;
	}

	public void setAutoStart( final boolean autoStart )
	{
		this.autoStart = autoStart;
	}

	public long getUsedBytes()
	{
		return this.bytesUsed;
	}

	private void setUsedBytes( final long bytesUsed )
	{
		this.bytesUsed = bytesUsed;
	}

	public long getCpuAvailableBytes()
	{
		return this.cpuBytesAvail;
	}

	private void setCpuAvailableBytes( final long cpuBytesAvail )
	{
		this.cpuBytesAvail = cpuBytesAvail;
	}

	public int getCpuCoProcessors()
	{
		return this.cpuCoProcessors;
	}

	private void setCpuCoProcessors( final int cpuCoProcessors )
	{
		this.cpuCoProcessors = cpuCoProcessors;
	}

	public int getSelectedCpu()
	{
		return this.selectedCpu;
	}

	private void setSelectedCpu( final int selectedCpu )
	{
		this.selectedCpu = selectedCpu;
	}

	public String getName()
	{
		return this.myName;
	}

	private void setName( @Nonnull final String myName )
	{
		this.myName = myName;
	}

	public boolean hasNoCPU()
	{
		return this.noCPU;
	}

	private void setNoCPU( final boolean noCPU )
	{
		this.noCPU = noCPU;
	}

	public boolean isSimulation()
	{
		return this.simulation;
	}

	private void setSimulation( final boolean simulation )
	{
		this.simulation = simulation;
	}

	public Future<ICraftingJob> getJob()
	{
		return this.job;
	}

	public void setJob( final Future<ICraftingJob> job )
	{
		this.job = job;
	}
}
