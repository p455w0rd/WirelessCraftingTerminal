package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.items.ItemMagnet;
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
		if (!(magnetItem.getItem() instanceof ItemMagnet)) {
			return;
		}
		if (magnetItem != null) {

			if (magnetItem.getItemDamage() == 2) {
				WCTUtils.getWirelessTerm(player.inventory).getTagCompound().getTagList("MagnetSlot", 10).getCompoundTagAt(0).setShort("Damage", (short) 0);
			}
			else {
				WCTUtils.getWirelessTerm(player.inventory).getTagCompound().getTagList("MagnetSlot", 10).getCompoundTagAt(0).setShort("Damage", (short) (magnetItem.getItemDamage() + 1));
			}
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {

	}
}
