package p455w0rd.wct.sync.packets;

import io.netty.buffer.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import p455w0rd.wct.container.*;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

public class PacketProgressBar extends WCTPacket {

	private final short id;
	private final long value;

	// automatic.
	public PacketProgressBar(final ByteBuf stream) {
		id = stream.readShort();
		value = stream.readLong();
	}

	// api
	public PacketProgressBar(final int shortID, final long value) {
		id = (short) shortID;
		this.value = value;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt(getPacketID());
		data.writeShort(shortID);
		data.writeLong(value);

		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		final Container c = player.openContainer;
		if (c instanceof ContainerWCT) {
			((ContainerWCT) c).updateFullProgressBar(id, value);
		}
		if (c instanceof WCTBaseContainer) {
			((WCTBaseContainer) c).updateFullProgressBar(id, value);
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		final Container c = player.openContainer;
		if (c instanceof ContainerWCT) {
			((ContainerWCT) c).updateFullProgressBar(id, value);
		}
		if (c instanceof WCTBaseContainer) {
			((WCTBaseContainer) c).updateFullProgressBar(id, value);
		}
	}
}
