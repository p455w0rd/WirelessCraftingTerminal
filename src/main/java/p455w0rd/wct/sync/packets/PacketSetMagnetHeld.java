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
import net.minecraft.item.ItemStack;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.items.ItemMagnet.MagnetFunctionMode;
import p455w0rd.wct.items.ItemMagnet.MagnetItemMode;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

public class PacketSetMagnetHeld extends WCTPacket {

	MagnetFunctionMode mode;

	public PacketSetMagnetHeld(final ByteBuf stream) {
		mode = MagnetFunctionMode.VALUES[stream.readInt()];
	}

	public PacketSetMagnetHeld(MagnetFunctionMode mode) {
		this.mode = mode;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(mode.ordinal());
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack magnetItem = ItemMagnet.getHeldMagnet(player);
		if (magnetItem.isEmpty() || magnetItem.getItem() != ModItems.MAGNET_CARD) {
			return;
		}
		if (!magnetItem.hasTagCompound()) {
			ItemMagnet.setItemMode(magnetItem, MagnetItemMode.INIT, true);
		}
		ItemMagnet.setMagnetFunctionMode(magnetItem, mode);
	}

}
