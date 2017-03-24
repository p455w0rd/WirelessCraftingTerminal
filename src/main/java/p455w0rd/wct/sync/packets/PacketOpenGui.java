package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.util.math.BlockPos;
import p455w0rd.wct.handlers.GuiHandler;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.util.WCTUtils;

public class PacketOpenGui extends WCTPacket {

	private final int whichGui;

	// automatic.
	public PacketOpenGui(final ByteBuf stream) {
		whichGui = stream.readInt();
	}

	public PacketOpenGui(int gui) {
		whichGui = gui;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(gui);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerPlayer) {
			GuiHandler.open(whichGui, player, WCTUtils.world(player), new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ));
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
	}
}
