package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

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
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer playerIn) {
		if (playerIn instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) playerIn;
			if (player.openContainer instanceof ContainerWCT) {
				ContainerWCT container = (ContainerWCT) player.openContainer;
				container.getTrashSlot().clearStack();
				//container.detectAndSendChanges();
				/*
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
				*/
			}
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {

	}

}
