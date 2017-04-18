package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import p455w0rd.wct.init.ModGlobals.Mods;
import p455w0rd.wct.integration.Baubles;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.util.WCTUtils;

public class PacketSetMagnet extends WCTPacket {

	int magnetDamage;
	ItemStack magnetStack = null;

	public PacketSetMagnet(final ByteBuf stream) {
		magnetDamage = stream.readInt();
		magnetStack = ByteBufUtils.readItemStack(stream);
	}

	// api
	public PacketSetMagnet(int itemDamage) {
		this(itemDamage, null);
	}

	public PacketSetMagnet(int itemDamage, ItemStack stackIn) {
		magnetDamage = itemDamage;
		magnetStack = stackIn;
		final ByteBuf data = Unpooled.buffer();

		data.writeInt(getPacketID());
		data.writeInt(magnetDamage);
		ByteBufUtils.writeItemStack(data, magnetStack);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack magnetItem = magnetStack == null ? WCTUtils.getMagnet(player.inventory) : magnetStack;
		if (magnetItem == null || !(magnetItem.getItem() instanceof ItemMagnet)) {
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
		if (WCTUtils.isMagnetInstalled(player.inventory)) {
			ItemStack wct = WCTUtils.getWirelessTerm(player.inventory);
			if (Mods.BAUBLES.isLoaded()) {
				wct = Baubles.getWCTBauble(player) != null ? Baubles.getWCTBauble(player) : wct;
			}
			NBTTagList magnetNBTForm = wct.getTagCompound().getTagList("MagnetSlot", 10);
			if (magnetNBTForm.getCompoundTagAt(0) != null) {
				ItemStack.loadItemStackFromNBT(magnetNBTForm.getCompoundTagAt(0)).getTagCompound().setInteger("MagnetMode", magnetDamage);
				magnetNBTForm.set(0, magnetItem.serializeNBT());
			}
			if (Mods.BAUBLES.isLoaded()) {
				if (Baubles.getWCTBauble(player) != null) {
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
