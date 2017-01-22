package p455w0rd.wct.sync.packets;

import io.netty.buffer.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.util.WCTUtils;

public class PacketEmptyTrash extends WCTPacket {

	public PacketEmptyTrash(final ByteBuf stream) {
	}

	// api
	public PacketEmptyTrash() {
		final ByteBuf data = Unpooled.buffer();

		data.writeInt(getPacketID());
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack wirelessTerm = WCTUtils.getWirelessTerm(player.inventory);
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
