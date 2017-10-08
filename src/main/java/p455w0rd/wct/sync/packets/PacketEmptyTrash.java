package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import p455w0rd.wct.init.ModGlobals.Mods;
import p455w0rd.wct.integration.Baubles;
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
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer playerIn) {
		if (playerIn instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) playerIn;
			if (Mods.BAUBLES.isLoaded()) {
				ItemStack wct = WCTUtils.getWirelessTerm(player.inventory);
				NBTTagCompound wctNBT = wct.getTagCompound();
				NBTTagCompound tagCompound = new NBTTagCompound();
				NBTTagList tagList = new NBTTagList();
				tagList.appendTag(tagCompound);
				wctNBT.setTag("TrashSlot", tagList);
				wct.setTagCompound(wctNBT);
			}
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		if (Mods.BAUBLES.isLoaded()) {
			Baubles.doForcedSync(player, WCTUtils.getWirelessTerm(player.inventory));
		}
	}

}
