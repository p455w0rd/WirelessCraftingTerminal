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
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.util.math.BlockPos;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.util.WCTUtils;

public class PacketOpenGui extends WCTPacket {

	private final int whichGui;

	// automatic.
	public PacketOpenGui(final ByteBuf stream) {
		whichGui = stream.readInt();
	}

	public PacketOpenGui(int gui) {
		whichGui = gui;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(gui);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerPlayer) {
			ModGuiHandler.open(whichGui, player, WCTUtils.world(player), new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ));
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
	}
}
