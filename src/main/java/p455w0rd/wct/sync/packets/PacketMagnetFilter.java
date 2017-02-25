package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import p455w0rd.wct.items.ItemMagnet;
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

		if (!(magnetItem.getItem() instanceof ItemMagnet)) {
			return;
		}
		if (magnetItem != null) {
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
		}
	}
}
