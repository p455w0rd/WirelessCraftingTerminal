package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketSetJobBytes extends WCTPacket {
	
	private final int bytez;

	// automatic.
	public PacketSetJobBytes(final ByteBuf stream) throws IOException, ClassNotFoundException {
		this.bytez = stream.readInt();
	}

	public PacketSetJobBytes(int numBytes) throws IOException {
		this.bytez = numBytes;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(this.getPacketID());
		data.writeInt(this.bytez);
		this.configureWrite(data);
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		GuiCraftConfirm.setJobBytes(this.bytez);
	}
}
