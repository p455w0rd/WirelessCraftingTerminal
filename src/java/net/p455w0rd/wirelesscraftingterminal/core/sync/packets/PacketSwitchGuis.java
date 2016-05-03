package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketSwitchGuis extends WCTPacket {

	private final int newGui;

	// automatic.
	public PacketSwitchGuis(final ByteBuf stream) {
		this.newGui = stream.readInt();
	}

	// api
	public PacketSwitchGuis(final int newGui) {
		this.newGui = newGui;

		if (Platform.isClient()) {
			GuiWirelessCraftingTerminal.setSwitchingGuis(true);
		}

		final ByteBuf data = Unpooled.buffer();

		data.writeInt(this.getPacketID());
		data.writeInt(newGui);

		this.configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		World world = player.worldObj;
		int x = (int) player.posX;
		int y = (int) player.posY;
		int z = (int) player.posZ;
		WCTGuiHandler.launchGui(newGui, player, world, x, y, z);
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		GuiWirelessCraftingTerminal.setSwitchingGuis(true);
	}
}
