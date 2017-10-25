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
import net.minecraft.nbt.NBTTagList;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.util.WCTUtils;

public class PacketMagnetFilter extends WCTPacket {

	// 0 = initialize
	// 1 = whitelist/blacklist
	// 2 = ignore NBT
	// 3 = ignore meta
	// 4 = use oredict
	int whichMode;
	boolean modeValue;

	public PacketMagnetFilter(final ByteBuf stream) {
		whichMode = stream.readInt();
		modeValue = stream.readBoolean();
	}

	public PacketMagnetFilter(final int mode, final boolean modeVal) {
		modeValue = modeVal;
		whichMode = mode;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(whichMode);
		data.writeBoolean(modeValue);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack magnetItem = WCTUtils.getMagnet(player.inventory);

		if (magnetItem.getItem() != ModItems.MAGNET_CARD) {
			return;
		}
		if (!magnetItem.isEmpty()) {
			if (!magnetItem.hasTagCompound()) {
				magnetItem.setTagCompound(new NBTTagCompound());
			}
			if (whichMode == 0) {
				magnetItem.getTagCompound().setBoolean("Initialized", modeValue);
			}
			else if (whichMode == 1) {
				magnetItem.getTagCompound().setBoolean("Whitelisting", modeValue);
			}
			else if (whichMode == 2) {
				magnetItem.getTagCompound().setBoolean("IgnoreNBT", modeValue);
			}
			else if (whichMode == 3) {
				magnetItem.getTagCompound().setBoolean("IgnoreMeta", modeValue);
			}
			else if (whichMode == 4) {
				magnetItem.getTagCompound().setBoolean("UseOreDict", modeValue);
			}
			else {
				return;
			}

			if (WCTUtils.isMagnetInstalled(player.inventory)) {
				ItemStack wct = WCTUtils.getWirelessTerm(player.inventory);
				if (!wct.isEmpty() && wct.hasTagCompound()) {
					NBTTagCompound newNBT = wct.getSubCompound("MagnetSlot");
					if (newNBT != null) {
						NBTTagList magnetNBTList = newNBT.getTagList("Items", 10);
						if (magnetNBTList != null && !magnetNBTList.hasNoTags()) {
							NBTTagCompound magnetNBTForm = magnetNBTList.getCompoundTagAt(0);
							if (magnetNBTForm != null) {
								NBTTagCompound newMagnetNBTForm = magnetItem.serializeNBT();
								magnetNBTList.set(0, newMagnetNBTForm);
							}
							wct.getSubCompound("MagnetSlot").setTag("Items", magnetNBTList);
						}
					}
				}
			}
		}
	}
}
