package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import p455w0rd.wct.init.ModGlobals.Mods;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.integration.Baubles;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.sync.network.NetworkHandler;
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
			NetworkHandler.instance().sendToServer(new PacketMagnetFilter(0, true));
		}
		//ItemMagnet.switchMagnetMode(magnetItem, player);
		/*
		if (magnetItem.getItemDamage() == 2) {
			WCTUtils.getWirelessTerm(player.inventory).getTagCompound().getTagList("MagnetSlot", 10).getCompoundTagAt(0).setShort("Damage", (short) 0);
		}
		else {
			WCTUtils.getWirelessTerm(player.inventory).getTagCompound().getTagList("MagnetSlot", 10).getCompoundTagAt(0).setShort("Damage", (short) (magnetItem.getItemDamage() + 1));
		}
		*/
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
			//wct.getTagCompound().getTagList("MagnetSlot", 10).getCompoundTagAt(0).setInteger("MagnetMode", magnetDamage);

		}

	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {

	}
}
