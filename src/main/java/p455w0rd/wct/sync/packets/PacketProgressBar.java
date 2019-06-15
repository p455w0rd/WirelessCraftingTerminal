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
import net.minecraft.inventory.Container;
import p455w0rd.ae2wtlib.api.container.ContainerWT;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.ae2wtlib.api.networking.INetworkInfo;

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
		if (c instanceof ContainerWT) {
			((ContainerWT) c).updateFullProgressBar(id, value);
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		final Container c = player.openContainer;
		if (c instanceof ContainerWT) {
			((ContainerWT) c).updateFullProgressBar(id, value);
		}
	}
}
