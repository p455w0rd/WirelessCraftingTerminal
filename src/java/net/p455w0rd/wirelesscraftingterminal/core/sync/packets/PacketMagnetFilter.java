package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;

public class PacketMagnetFilter extends WCTPacket {

	// 0 = initialize
	// 1 = whitelist/blacklist
	// 2 = ignore NBT
	// 3 = ignore meta
	// 4 = use oredict
	int whichMode;
	boolean modeValue;

	public PacketMagnetFilter(final ByteBuf stream) {
		this.whichMode = stream.readInt();
		this.modeValue = stream.readBoolean();
	}

	public PacketMagnetFilter(final int mode, final boolean modeVal) {
		this.modeValue = modeVal;
		this.whichMode = mode;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(this.getPacketID());
		data.writeInt(this.whichMode);
		data.writeBoolean(this.modeValue);
		this.configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack magnetItem = RandomUtils.getMagnet(player.inventory);
		
		if (!(magnetItem.getItem() instanceof ItemMagnet)) {
			return;
		}
		if (magnetItem != null) {
			if (!magnetItem.hasTagCompound()) {
				magnetItem.setTagCompound(new NBTTagCompound());
			}
			if (this.whichMode == 0) {
				magnetItem.getTagCompound().setBoolean("Initialized", this.modeValue);
			}
			if (this.whichMode == 1) {
				magnetItem.getTagCompound().setBoolean("Whitelisting", this.modeValue);
			}
			else if (this.whichMode == 2) {
				magnetItem.getTagCompound().setBoolean("IgnoreNBT", this.modeValue);
			}
			else if (this.whichMode == 3) {
				magnetItem.getTagCompound().setBoolean("IgnoreMeta", this.modeValue);
			}
			else if (this.whichMode == 4) {
				magnetItem.getTagCompound().setBoolean("UseOreDict", this.modeValue);
			}
			else {
				return;
			}
		}
	}
}
