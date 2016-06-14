package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.WCTBaseContainer;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketSwapSlots extends WCTPacket
{

	private final int slotA;
	private final int slotB;

	// automatic.
	public PacketSwapSlots( final ByteBuf stream )
	{
		this.slotA = stream.readInt();
		this.slotB = stream.readInt();
	}

	// api
	public PacketSwapSlots( final int slotA, final int slotB )
	{
		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( this.slotA = slotA );
		data.writeInt( this.slotB = slotB );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player )
	{
		if ( player != null)
		{
			if (player.openContainer instanceof ContainerWirelessCraftingTerminal) {
				( (ContainerWirelessCraftingTerminal) player.openContainer ).swapSlotContents( this.slotA, this.slotB );
			}
			if (player.openContainer instanceof WCTBaseContainer) {
				( (WCTBaseContainer) player.openContainer ).swapSlotContents( this.slotA, this.slotB );
			}
		}
	}
}
