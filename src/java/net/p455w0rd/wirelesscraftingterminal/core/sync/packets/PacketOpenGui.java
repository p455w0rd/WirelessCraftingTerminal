package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketOpenGui extends WCTPacket {

	private final int whichGui;

	// automatic.
	public PacketOpenGui(final ByteBuf stream) {
		this.whichGui = stream.readInt();
	}

	public PacketOpenGui(int gui) {
		this.whichGui = gui;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(this.getPacketID());
		data.writeInt(gui);
		this.configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerPlayer) {
			WCTGuiHandler.launchGui(this.whichGui, player, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
	}
}
