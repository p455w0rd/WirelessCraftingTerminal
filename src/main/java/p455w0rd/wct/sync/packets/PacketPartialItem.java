/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.WCTBaseContainer;
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
		else if (player.openContainer instanceof WCTBaseContainer) {
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
