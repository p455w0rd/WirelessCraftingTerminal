package p455w0rd.wct.sync.packets;

import java.io.IOException;

import io.netty.buffer.*;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.wct.client.gui.GuiCraftConfirm;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

public class PacketSetJobBytes extends WCTPacket {

	private final int bytez;

	// automatic.
	public PacketSetJobBytes(final ByteBuf stream) throws IOException, ClassNotFoundException {
		bytez = stream.readInt();
	}

	public PacketSetJobBytes(int numBytes) throws IOException {
		bytez = numBytes;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(bytez);
		configureWrite(data);
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		GuiCraftConfirm.setJobBytes(bytez);
	}
}
