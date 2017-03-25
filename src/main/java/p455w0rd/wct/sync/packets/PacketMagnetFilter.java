package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
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
	ItemStack stack;

	public PacketMagnetFilter(final ByteBuf stream) {
		whichMode = stream.readInt();
		modeValue = stream.readBoolean();
		stack = ByteBufUtils.readItemStack(stream);
	}

	public PacketMagnetFilter(final int mode, final boolean modeVal) {
		this(mode, modeVal, null);
	}

	public PacketMagnetFilter(final int mode, final boolean modeVal, ItemStack stackIn) {
		modeValue = modeVal;
		whichMode = mode;
		stack = stackIn;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(whichMode);
		data.writeBoolean(modeValue);
		ByteBufUtils.writeItemStack(data, stack);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack magnetItem = stack == null ? WCTUtils.getMagnet(player.inventory) : stack;

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

			if (WCTUtils.isMagnetInstalled(player.inventory)) {
				ItemStack wct = WCTUtils.getWirelessTerm(player.inventory);
				NBTTagCompound newNBT = wct.getTagCompound();
				NBTTagList magnetNBTForm = wct.getTagCompound().getTagList("MagnetSlot", 10);
				if (magnetNBTForm.getCompoundTagAt(0) != null) {
					magnetNBTForm.set(0, magnetItem.serializeNBT());
				}
				newNBT.setTag("MagnetSlot", magnetNBTForm);
				//wct.writeToNBT(newNBT);
			}
			//player.getServer().saveAllWorlds(true);
		}
	}
}
