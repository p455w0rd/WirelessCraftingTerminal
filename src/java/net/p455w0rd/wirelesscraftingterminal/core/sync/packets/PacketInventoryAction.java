package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import java.io.IOException;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.ClientHelper;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.common.WirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.WCTBaseContainer;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftAmount;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerOpenContext;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketInventoryAction extends WCTPacket
{

	private final InventoryAction action;
	private final int slot;
	private final long id;
	private final IAEItemStack slotItem;

	// automatic.
	public PacketInventoryAction( final ByteBuf stream ) throws IOException
	{
		this.action = InventoryAction.values()[stream.readInt()];
		this.slot = stream.readInt();
		this.id = stream.readLong();
		final boolean hasItem = stream.readBoolean();
		if( hasItem )
		{
			this.slotItem = AEItemStack.loadItemStackFromPacket( stream );
		}
		else
		{
			this.slotItem = null;
		}
	}

	// api
	public PacketInventoryAction( final InventoryAction action, final int slot, final IAEItemStack slotItem ) throws IOException
	{

		if( Platform.isClient() )
		{
			throw new IllegalStateException( "invalid packet, client cannot post inv actions with stacks." );
		}

		this.action = action;
		this.slot = slot;
		this.id = 0;
		this.slotItem = slotItem;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( slot );
		data.writeLong( this.id );

		if( slotItem == null )
		{
			data.writeBoolean( false );
		}
		else
		{
			data.writeBoolean( true );
			slotItem.writeToPacket( data );
		}

		this.configureWrite( data );
	}

	// api
	public PacketInventoryAction( final InventoryAction action, final int slot, final long id )
	{
		this.action = action;
		this.slot = slot;
		this.id = id;
		this.slotItem = null;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( slot );
		data.writeLong( id );
		data.writeBoolean( false );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player )
	{
		final EntityPlayerMP sender = (EntityPlayerMP) player;
		Container baseContainer = sender.openContainer;
		ContainerOpenContext context = null;
		if( sender.openContainer instanceof ContainerWirelessCraftingTerminal ) {
			baseContainer = (ContainerWirelessCraftingTerminal) sender.openContainer;
			context = ((ContainerWirelessCraftingTerminal) baseContainer).getOpenContext();
		}
		
		if( sender.openContainer instanceof WCTBaseContainer ) {
			baseContainer = (WCTBaseContainer) sender.openContainer;
			context = ((WCTBaseContainer) baseContainer).getOpenContext();
		}

			if( this.action == InventoryAction.AUTO_CRAFT )
			{
				//if( context != null )
				//{
					//final TileEntity te = context.getTile();
					
					int x = player.serverPosX;
					int y = player.serverPosY;
					int z = player.serverPosZ;
					
					WCTGuiHandler.launchGui(WirelessCraftingTerminal.GUI_CRAFT_AMOUNT, player, player.worldObj, x, y, z);

					if( sender.openContainer instanceof ContainerCraftAmount )
					{
						final ContainerCraftAmount cca = (ContainerCraftAmount) sender.openContainer;
						
						if (baseContainer instanceof ContainerWirelessCraftingTerminal) {
							if( ((ContainerWirelessCraftingTerminal) baseContainer).getTargetStack() != null )
							{
								cca.getCraftingItem().putStack( ((ContainerWirelessCraftingTerminal) baseContainer).getTargetStack().getItemStack() );
								cca.setItemToCraft( ((ContainerWirelessCraftingTerminal) baseContainer).getTargetStack() );
							}
						}
						
						if (baseContainer instanceof WCTBaseContainer) {
							if( ((WCTBaseContainer) baseContainer).getTargetStack() != null )
							{
								cca.getCraftingItem().putStack( ((WCTBaseContainer) baseContainer).getTargetStack().getItemStack() );
								cca.setItemToCraft( ((WCTBaseContainer) baseContainer).getTargetStack() );
							}
						}

						cca.detectAndSendChanges();
					}
				//}
			}
			else
			{
				if (baseContainer instanceof ContainerWirelessCraftingTerminal) {
					((ContainerWirelessCraftingTerminal) baseContainer).doAction( sender, this.action, this.slot, this.id );
				}
				if (baseContainer instanceof WCTBaseContainer) {
					((WCTBaseContainer) baseContainer).doAction( sender, this.action, this.slot, this.id );
				}
			}
	}

	@Override
	public void clientPacketData( final INetworkInfo network, final WCTPacket packet, final EntityPlayer player )
	{
		if( this.action == InventoryAction.UPDATE_HAND )
		{
			if( this.slotItem == null )
			{
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( null );
			}
			else
			{
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( this.slotItem.getItemStack() );
			}
		}
	}
}