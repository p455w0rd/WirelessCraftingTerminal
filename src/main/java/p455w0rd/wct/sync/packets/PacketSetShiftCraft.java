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
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.ae2wtlib.api.networking.INetworkInfo;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class PacketSetShiftCraft extends WCTPacket {

	boolean mode;

	public PacketSetShiftCraft(final ByteBuf stream) {
		mode = stream.readBoolean();
	}

	public PacketSetShiftCraft(boolean modeIn) {
		mode = modeIn;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeBoolean(modeIn);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerWT) {
			ItemStack wirelessTerminal = ((ContainerWT) player.openContainer).getWirelessTerminal();
			if (!wirelessTerminal.isEmpty()) {
				if (!wirelessTerminal.hasTagCompound()) {
					wirelessTerminal.setTagCompound(new NBTTagCompound());
				}
				wirelessTerminal.getTagCompound().setBoolean(WCTUtils.SHIFTCRAFT_NBT, mode);
			}
		}
	}

}