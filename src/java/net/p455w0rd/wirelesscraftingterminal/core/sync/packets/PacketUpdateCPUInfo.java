package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketUpdateCPUInfo extends WCTPacket {
	
	private final int bytez;
	private final int coCPUz;

	// automatic.
	public PacketUpdateCPUInfo(final ByteBuf stream) throws IOException, ClassNotFoundException {
		this.bytez = stream.readInt();
		this.coCPUz = stream.readInt();
	}

	public PacketUpdateCPUInfo(int numBytes, int numCoCPUs) throws IOException {
		this.bytez = numBytes;
		this.coCPUz = numCoCPUs;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(this.getPacketID());
		data.writeInt(this.bytez);
		data.writeInt(this.coCPUz);
		this.configureWrite(data);
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		GuiCraftConfirm.setCurrentCPUAvailBytes(this.bytez);
		GuiCraftConfirm.setCurrentCPUCoCPUs(this.coCPUz);
	}
}