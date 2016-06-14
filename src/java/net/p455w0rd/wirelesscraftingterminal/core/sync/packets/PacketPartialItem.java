package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.WCTBaseContainer;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;


public class PacketPartialItem extends WCTPacket
{

	private final short pageNum;
	private final byte[] data;

	// automatic.
	public PacketPartialItem( final ByteBuf stream )
	{
		this.pageNum = stream.readShort();
		stream.readBytes( this.data = new byte[stream.readableBytes()] );
	}

	// api
	public PacketPartialItem( final int page, final int maxPages, final byte[] buf )
	{

		final ByteBuf data = Unpooled.buffer();

		this.pageNum = (short) ( page | ( maxPages << 8 ) );
		this.data = buf;
		data.writeInt( this.getPacketID() );
		data.writeShort( this.pageNum );
		data.writeBytes( buf );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player )
	{
		if( player.openContainer instanceof ContainerWirelessCraftingTerminal )
		{
			( (ContainerWirelessCraftingTerminal) player.openContainer ).postPartial( this );
		}
		if( player.openContainer instanceof WCTBaseContainer )
		{
			( (WCTBaseContainer) player.openContainer ).postPartial( this );
		}
	}

	public int getPageCount()
	{
		return this.pageNum >> 8;
	}

	public int getSize()
	{
		return this.data.length;
	}

	public int write( final byte[] buffer, final int cursor )
	{
		System.arraycopy( this.data, 0, buffer, cursor, this.data.length );
		return cursor + this.data.length;
	}
}
