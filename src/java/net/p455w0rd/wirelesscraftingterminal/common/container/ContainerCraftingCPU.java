package net.p455w0rd.wirelesscraftingterminal.common.container;

import java.io.IOException;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.ICustomNameObject;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.common.container.guisync.GuiSync;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketMEInventoryUpdate;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketValueConfig;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;


public class ContainerCraftingCPU extends WCTBaseContainer implements IMEMonitorHandlerReceiver<IAEItemStack>, ICustomNameObject
{

	private final IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
	private CraftingCPUCluster monitor = null;
	private String cpuName = null;
	private final WirelessTerminalGuiObject obj;

	@GuiSync( 0 )
	public long eta = -1;

	public ContainerCraftingCPU( final InventoryPlayer ip, final Object te )
	{
		super( ip, te );
		this.obj = getGuiObject(RandomUtils.getWirelessTerm(ip), ip.player, ip.player.worldObj, (int) ip.player.posX, (int) ip.player.posY, (int) ip.player.posZ);
		if (this.obj == null) {
			this.setValidContainer(false);
		}
		/*
		final IGridHost host = (IGridHost) ( te instanceof IGridHost ? te : null );

		if( host != null )
		{
			this.findNode( host, ForgeDirection.UNKNOWN );
			for( final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
			{
				this.findNode( host, d );
			}
		}

		if( te instanceof TileCraftingTile )
		{
			this.setCPU( (ICraftingCPU) ( (IAEMultiBlock) te ).getCluster() );
		}

		if( this.getNetwork() == null && Platform.isServer() )
		{
			this.setValidContainer( false );
		}
		*/
	}
	
	protected WirelessTerminalGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (it != null) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WirelessTerminalGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	protected void setCPU( final ICraftingCPU c )
	{
		if( c == this.getMonitor() )
		{
			return;
		}

		if( this.getMonitor() != null )
		{
			this.getMonitor().removeListener( this );
		}

		for( final Object g : this.crafters )
		{
			if( g instanceof EntityPlayer )
			{
				try
				{
					NetworkHandler.instance.sendTo( new PacketValueConfig( "CraftingStatus", "Clear" ), (EntityPlayerMP) g );
				}
				catch( final IOException e )
				{
					WCTLog.debug( e.getMessage() );
				}
			}
		}

		if( c instanceof CraftingCPUCluster )
		{
			this.cpuName = c.getName();
			this.setMonitor( (CraftingCPUCluster) c );
			this.list.resetStatus();
			this.getMonitor().getListOfItem( this.list, CraftingItemList.ALL );
			this.getMonitor().addListener( this, null );
			this.setEstimatedTime( 0 );
		}
		else
		{
			this.setMonitor( null );
			this.cpuName = "";
			this.setEstimatedTime( -1 );
		}
	}

	public void cancelCrafting()
	{
		if( this.getMonitor() != null )
		{
			this.getMonitor().cancel();
		}
		this.setEstimatedTime( -1 );
	}

	@Override
	public void removeCraftingFromCrafters( final ICrafting c )
	{
		super.removeCraftingFromCrafters( c );

		if( this.crafters.isEmpty() && this.getMonitor() != null )
		{
			this.getMonitor().removeListener( this );
		}
	}

	@Override
	public void onContainerClosed( final EntityPlayer player )
	{
		super.onContainerClosed( player );
		if( this.getMonitor() != null )
		{
			this.getMonitor().removeListener( this );
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		if( Platform.isServer() && this.getMonitor() != null && !this.list.isEmpty() )
		{
			try
			{
				if( this.getEstimatedTime() >= 0 )
				{
					final long elapsedTime = this.getMonitor().getElapsedTime();
					final double remainingItems = this.getMonitor().getRemainingItemCount();
					final double startItems = this.getMonitor().getStartItemCount();
					final long eta = (long) ( elapsedTime / Math.max( 1d, ( startItems - remainingItems ) ) * remainingItems );
					this.setEstimatedTime( eta );
				}

				final PacketMEInventoryUpdate a = new PacketMEInventoryUpdate( (byte) 0 );
				final PacketMEInventoryUpdate b = new PacketMEInventoryUpdate( (byte) 1 );
				final PacketMEInventoryUpdate c = new PacketMEInventoryUpdate( (byte) 2 );

				for( final IAEItemStack out : this.list )
				{
					a.appendItem( this.getMonitor().getItemStack( out, CraftingItemList.STORAGE ) );
					b.appendItem( this.getMonitor().getItemStack( out, CraftingItemList.ACTIVE ) );
					c.appendItem( this.getMonitor().getItemStack( out, CraftingItemList.PENDING ) );
				}

				this.list.resetStatus();

				for( final Object g : this.crafters )
				{
					if( g instanceof EntityPlayer )
					{
						if( !a.isEmpty() )
						{
							NetworkHandler.instance.sendTo( a, (EntityPlayerMP) g );
						}

						if( !b.isEmpty() )
						{
							NetworkHandler.instance.sendTo( b, (EntityPlayerMP) g );
						}

						if( !c.isEmpty() )
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
		super.detectAndSendChanges();
	}

	@Override
	public boolean isValid( final Object verificationToken )
	{
		return true;
	}

	@Override
	public void postChange( final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final BaseActionSource actionSource )
	{
		for( IAEItemStack is : change )
		{
			is = is.copy();
			is.setStackSize( 1 );
			this.list.add( is );
		}
	}

	@Override
	public void onListUpdate()
	{

	}

	@Override
	public String getCustomName()
	{
		return this.cpuName;
	}

	@Override
	public boolean hasCustomName()
	{
		return this.cpuName != null && this.cpuName.length() > 0;
	}

	public long getEstimatedTime()
	{
		return this.eta;
	}

	private void setEstimatedTime( final long eta )
	{
		this.eta = eta;
	}

	CraftingCPUCluster getMonitor()
	{
		return this.monitor;
	}

	private void setMonitor( final CraftingCPUCluster monitor )
	{
		this.monitor = monitor;
	}

	IGrid getNetwork()
	{
		return this.obj.getTargetGrid();
	}
}
