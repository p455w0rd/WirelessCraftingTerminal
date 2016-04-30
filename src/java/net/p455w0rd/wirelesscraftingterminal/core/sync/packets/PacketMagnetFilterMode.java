package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;

public class PacketMagnetFilterMode extends WCTPacket {

	boolean isWhitelisting;

	public PacketMagnetFilterMode(final ByteBuf stream) {
		this.isWhitelisting = stream.readBoolean();
	}

	public PacketMagnetFilterMode(final boolean isWhitelisting) {
		this.isWhitelisting = isWhitelisting;
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			GuiMagnet.mode = this.isWhitelisting;
		}
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(this.getPacketID());
		data.writeBoolean(this.isWhitelisting);
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
			magnetItem.getTagCompound().setBoolean("Whitelisting", this.isWhitelisting);
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		GuiMagnet.mode = this.isWhitelisting;
	}
}
