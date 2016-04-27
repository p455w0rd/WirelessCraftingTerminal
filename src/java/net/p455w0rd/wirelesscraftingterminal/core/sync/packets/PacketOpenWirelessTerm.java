package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.api.WCTApi;
import net.p455w0rd.wirelesscraftingterminal.common.WirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class PacketOpenWirelessTerm extends WCTPacket {

	// automatic.
	public PacketOpenWirelessTerm(final ByteBuf stream) {

	}

	public PacketOpenWirelessTerm() {
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(this.getPacketID());
		this.configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerPlayer) {
			WCTApi.instance().interact().openWirelessCraftingTerminalGui(player);
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		//WCTApi.instance().interact().openWirelessCraftingTerminalGui(player);
	}
}
