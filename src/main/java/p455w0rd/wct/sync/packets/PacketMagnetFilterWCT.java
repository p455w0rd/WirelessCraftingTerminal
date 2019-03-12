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
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.items.ItemMagnet.MagnetItemMode;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class PacketMagnetFilterWCT extends WCTPacket {

	// 0 = initialize
	// 1 = whitelist/blacklist
	// 2 = ignore NBT
	// 3 = ignore meta
	// 4 = use oredict
	MagnetItemMode whichMode;
	boolean modeValue;
	int slot;
	boolean isBauble;

	public PacketMagnetFilterWCT(final ByteBuf stream) {
		whichMode = MagnetItemMode.VALUES[stream.readInt()];
		slot = stream.readInt();
		isBauble = stream.readBoolean();
		modeValue = stream.readBoolean();
	}

	public PacketMagnetFilterWCT(final MagnetItemMode mode, final boolean modeVal, boolean isBauble, int slot) {
		modeValue = modeVal;
		whichMode = mode;
		this.isBauble = isBauble;
		this.slot = slot;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(whichMode.ordinal());
		data.writeInt(slot);
		data.writeBoolean(isBauble);
		data.writeBoolean(modeValue);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack wirelessTerminal = isBauble ? WTApi.instance().getBaublesUtility().getWTBySlot(player, slot, IWirelessCraftingTerminalItem.class) : WCTUtils.getWCTBySlot(player, slot);
		if (!wirelessTerminal.isEmpty() && wirelessTerminal.hasTagCompound()) {
			ItemStack magnet = ItemMagnet.getMagnetFromWCT(wirelessTerminal);
			if (!magnet.isEmpty()) {
				ItemMagnet.setItemMode(magnet, whichMode, modeValue);
				/*
				NBTTagCompound newNBT = wirelessTerminal.getSubCompound(ItemMagnet.MAGNET_SLOT_NBT);
				if (newNBT != null) {
					NBTTagList magnetNBTList = newNBT.getTagList(ItemMagnet.ITEMS_NBT, 10);
					if (magnetNBTList != null && !magnetNBTList.hasNoTags()) {
						NBTTagCompound magnetNBTForm = magnetNBTList.getCompoundTagAt(0);
						if (magnetNBTForm != null) {
							NBTTagCompound newMagnetNBTForm = magnet.serializeNBT();
							magnetNBTList.set(0, newMagnetNBTForm);
						}
						wirelessTerminal.getSubCompound(ItemMagnet.MAGNET_SLOT_NBT).setTag(ItemMagnet.ITEMS_NBT, magnetNBTList);
					}
				}
				*/
			}
		}
	}
}
