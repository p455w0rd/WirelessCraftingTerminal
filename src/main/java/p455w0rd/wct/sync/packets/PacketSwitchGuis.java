package p455w0rd.wct.sync.packets;

import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import p455w0rd.wct.client.gui.GuiWCT;
import p455w0rd.wct.handlers.GuiHandler;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.util.WCTUtils;

public class PacketSwitchGuis extends WCTPacket {

	private final int newGui;

	// automatic.
	public PacketSwitchGuis(final ByteBuf stream) {
		newGui = stream.readInt();
	}

	// api
	public PacketSwitchGuis(final int newGui) {
		this.newGui = newGui;

		if (Platform.isClient()) {
			GuiWCT.setSwitchingGuis(true);
		}

		final ByteBuf data = Unpooled.buffer();

		data.writeInt(getPacketID());
		data.writeInt(newGui);

		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		World world = WCTUtils.world(player);
		int x = (int) player.posX;
		int y = (int) player.posY;
		int z = (int) player.posZ;
		GuiHandler.open(newGui, player, world, new BlockPos(x, y, z));
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		GuiWCT.setSwitchingGuis(true);
	}
}
