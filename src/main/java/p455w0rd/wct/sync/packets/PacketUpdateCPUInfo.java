package p455w0rd.wct.sync.packets;

import java.io.IOException;

import io.netty.buffer.*;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.wct.client.gui.GuiCraftConfirm;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

public class PacketUpdateCPUInfo extends WCTPacket {

	private final int bytez;
	private final int coCPUz;

	// automatic.
	public PacketUpdateCPUInfo(final ByteBuf stream) throws IOException, ClassNotFoundException {
		bytez = stream.readInt();
		coCPUz = stream.readInt();
	}

	public PacketUpdateCPUInfo(int numBytes, int numCoCPUs) throws IOException {
		bytez = numBytes;
		coCPUz = numCoCPUs;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(bytez);
		data.writeInt(coCPUz);
		configureWrite(data);
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		GuiCraftConfirm.setCurrentCPUAvailBytes(bytez);
		GuiCraftConfirm.setCurrentCPUCoCPUs(coCPUz);
	}
}