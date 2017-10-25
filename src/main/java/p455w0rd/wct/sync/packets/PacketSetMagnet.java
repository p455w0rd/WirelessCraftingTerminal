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
import p455w0rd.wct.init.ModIntegration.Mods;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.integration.Baubles;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.util.WCTUtils;

public class PacketSetMagnet extends WCTPacket {

	int magnetDamage;

	public PacketSetMagnet(final ByteBuf stream) {
		magnetDamage = stream.readInt();
	}

	// api
	public PacketSetMagnet(int itemDamage) {
		magnetDamage = itemDamage;
		final ByteBuf data = Unpooled.buffer();

		data.writeInt(getPacketID());
		data.writeInt(magnetDamage);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack magnetItem = WCTUtils.getMagnet(player.inventory);
		if (magnetItem.isEmpty() || magnetItem.getItem() != ModItems.MAGNET_CARD) {
			return;
		}
		if (!magnetItem.hasTagCompound()) {
			ModNetworking.instance().sendToServer(new PacketMagnetFilter(0, true));
		}

		ItemStack wct = WCTUtils.getWirelessTerm(player.inventory);
		if (!wct.isEmpty() && WCTUtils.isMagnetInstalled(player.inventory)) {

			if (Mods.BAUBLES.isLoaded()) {
				wct = !Baubles.getWCTBauble(player).isEmpty() ? Baubles.getWCTBauble(player) : wct;
			}
			NBTTagCompound magnetNBT = wct.getSubCompound("MagnetSlot");
			NBTTagList magnetNBTForm = magnetNBT.getTagList("Items", 10);
			if (magnetNBTForm.getCompoundTagAt(0) != null) {
				new ItemStack(magnetNBTForm.getCompoundTagAt(0)).getTagCompound().setInteger("MagnetMode", magnetDamage);
				magnetNBTForm.set(0, magnetItem.serializeNBT());
			}
			if (Mods.BAUBLES.isLoaded()) {
				if (!Baubles.getWCTBauble(player).isEmpty()) {
					int slotIndex = Baubles.getWCTBaubleSlotIndex(player);
					Baubles.setBaublesItemStack(player, slotIndex, wct);
				}
			}
		}

	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {

	}
}
