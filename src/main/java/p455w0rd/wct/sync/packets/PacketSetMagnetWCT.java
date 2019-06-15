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
import net.minecraft.nbt.NBTTagCompound;
import p455w0rd.ae2wtlib.api.container.ContainerWT;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.items.ItemMagnet.MagnetFunctionMode;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.ae2wtlib.api.networking.INetworkInfo;

/**
 * @author p455w0rd
 *
 */
public class PacketSetMagnetWCT extends WCTPacket {

	MagnetFunctionMode mode;

	public PacketSetMagnetWCT(final ByteBuf stream) {
		mode = MagnetFunctionMode.VALUES[stream.readInt()];
	}

	// api
	public PacketSetMagnetWCT(MagnetFunctionMode mode) {
		this.mode = mode;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(mode.ordinal());
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerWT) {
			ItemStack wirelessTerminal = ((ContainerWT) player.openContainer).getWirelessTerminal();
			if (!wirelessTerminal.isEmpty()) {
				if (!wirelessTerminal.hasTagCompound()) {
					wirelessTerminal.setTagCompound(new NBTTagCompound());
				}
				ItemMagnet.setMagnetFunctionMode(ItemMagnet.getMagnetFromWCT(wirelessTerminal), mode);
			}
		}
	}

}
