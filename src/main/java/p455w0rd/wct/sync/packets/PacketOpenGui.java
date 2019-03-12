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

public class PacketOpenGui extends WCTPacket {

	private final int whichGui;
	private final int slot;
	private final boolean isBauble;

	public PacketOpenGui(final ByteBuf stream) {
		whichGui = stream.readInt();
		slot = stream.readInt();
		isBauble = stream.readBoolean();
	}

	public PacketOpenGui(int gui, int slot, boolean isBauble) {
		whichGui = gui;
		this.slot = slot;
		this.isBauble = isBauble;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(gui);
		data.writeInt(slot);
		data.writeBoolean(isBauble);
		configureWrite(data);
		ModGuiHandler.setIsBauble(isBauble);
		ModGuiHandler.setSlot(slot);
	}

	public PacketOpenGui(int gui) {
		this(gui, -1, false);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerPlayer && slot >= 0) {
			ModGuiHandler.open(whichGui, player, player.getEntityWorld(), new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ), false, isBauble, slot);
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
	}
}
