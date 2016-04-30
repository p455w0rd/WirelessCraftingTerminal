package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketEmptyTrash extends WCTPacket {

	public PacketEmptyTrash(final ByteBuf stream) {
	}

	// api
	public PacketEmptyTrash() {
		final ByteBuf data = Unpooled.buffer();

		data.writeInt(this.getPacketID());
		this.configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack wirelessTerm = RandomUtils.getWirelessTerm(player.inventory);
		if (wirelessTerm.getTagCompound() != null) {
			NBTTagCompound nbtTC = wirelessTerm.getTagCompound();
			if (nbtTC.hasKey("TrashSlot")) {
				NBTTagList trashSlot = nbtTC.getTagList("TrashSlot", 10);
				if (trashSlot != null) {
					ItemStack trashItem = ItemStack.loadItemStackFromNBT(trashSlot.getCompoundTagAt(0));
					if (trashItem != null) {
						trashSlot.removeTag(0);
					}
				}
			}
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {

	}

}
