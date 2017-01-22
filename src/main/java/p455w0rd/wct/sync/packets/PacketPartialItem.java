package p455w0rd.wct.sync.packets;

import io.netty.buffer.*;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.wct.container.*;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

public class PacketPartialItem extends WCTPacket {

	private final short pageNum;
	private final byte[] data;

	// automatic.
	public PacketPartialItem(final ByteBuf stream) {
		pageNum = stream.readShort();
		stream.readBytes(data = new byte[stream.readableBytes()]);
	}

	// api
	public PacketPartialItem(final int page, final int maxPages, final byte[] buf) {

		final ByteBuf data = Unpooled.buffer();

		pageNum = (short) (page | (maxPages << 8));
		this.data = buf;
		data.writeInt(getPacketID());
		data.writeShort(pageNum);
		data.writeBytes(buf);

		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerWCT) {
			((ContainerWCT) player.openContainer).postPartial(this);
		}
		if (player.openContainer instanceof WCTBaseContainer) {
			((WCTBaseContainer) player.openContainer).postPartial(this);
		}
	}

	public int getPageCount() {
		return pageNum >> 8;
	}

	public int getSize() {
		return data.length;
	}

	public int write(final byte[] buffer, final int cursor) {
		System.arraycopy(data, 0, buffer, cursor, data.length);
		return cursor + data.length;
	}
}
