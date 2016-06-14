package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.WCTBaseContainer;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;


public class PacketProgressBar extends WCTPacket
{

	private final short id;
	private final long value;

	// automatic.
	public PacketProgressBar( final ByteBuf stream )
	{
		this.id = stream.readShort();
		this.value = stream.readLong();
	}

	// api
	public PacketProgressBar( final int shortID, final long value )
	{
		this.id = (short) shortID;
		this.value = value;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeShort( shortID );
		data.writeLong( value );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player )
	{
		final Container c = player.openContainer;
		if( c instanceof ContainerWirelessCraftingTerminal )
		{
			( (ContainerWirelessCraftingTerminal) c ).updateFullProgressBar( this.id, this.value );
		}
		if( c instanceof WCTBaseContainer )
		{
			( (WCTBaseContainer) c ).updateFullProgressBar( this.id, this.value );
		}
	}

	@Override
	public void clientPacketData( final INetworkInfo network, final WCTPacket packet, final EntityPlayer player )
	{
		final Container c = player.openContainer;
		if( c instanceof ContainerWirelessCraftingTerminal )
		{
			( (ContainerWirelessCraftingTerminal) c ).updateFullProgressBar( this.id, this.value );
		}
		if( c instanceof WCTBaseContainer )
		{
			( (WCTBaseContainer) c ).updateFullProgressBar( this.id, this.value );
		}
	}
}
