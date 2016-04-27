package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.common.WirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketSwitchGuis extends WCTPacket
{

	private final int newGui;

	// automatic.
	public PacketSwitchGuis( final ByteBuf stream )
	{
		this.newGui = stream.readInt();
	}

	// api
	public PacketSwitchGuis( final int newGui )
	{
		this.newGui = newGui;

		if( Platform.isClient() )
		{
			GuiWirelessCraftingTerminal.setSwitchingGuis( true );
		}

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( newGui );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player )
	{
		World world = player.worldObj;
		if (world.isRemote) {
			int x = player.serverPosX;
			int y = player.serverPosY;
			int z = player.serverPosZ;
			WCTGuiHandler.launchGui(newGui, player, world, x, y, z);
		}
		else {
			int x = (int)player.posX;
			int y = (int)player.posY;
			int z = (int)player.posZ;
			WCTGuiHandler.launchGui(newGui, player, world, x, y, z);
		}
		
		/*
		final Container c = player.openContainer;
		if( c instanceof ContainerWirelessCraftingTerminal )
		{
			final ContainerWirelessCraftingTerminal bc = (ContainerWirelessCraftingTerminal) c;
			final ContainerOpenContext context = bc.getOpenContext();
			if( context != null )
			{
				final TileEntity te = context.getTile();
				Platform.openGUI( player, te, context.getSide(), this.newGui );
			}
		}
		*/
	}

	@Override
	public void clientPacketData( final INetworkInfo network, final WCTPacket packet, final EntityPlayer player )
	{
		GuiWirelessCraftingTerminal.setSwitchingGuis( true );
	}
}
