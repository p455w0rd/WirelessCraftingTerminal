package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.WCTBaseContainer;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

public class PacketSwapSlots extends WCTPacket {

	private final int slotA;
	private final int slotB;

	// automatic.
	public PacketSwapSlots(final ByteBuf stream) {
		slotA = stream.readInt();
		slotB = stream.readInt();
	}

	// api
	public PacketSwapSlots(final int slotA, final int slotB) {
		final ByteBuf data = Unpooled.buffer();

		data.writeInt(getPacketID());
		data.writeInt(this.slotA = slotA);
		data.writeInt(this.slotB = slotB);

		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player != null) {
			if (player.openContainer instanceof ContainerWCT) {
				((ContainerWCT) player.openContainer).swapSlotContents(slotA, slotB);
			}
			else if (player.openContainer instanceof WCTBaseContainer) {
				((WCTBaseContainer) player.openContainer).swapSlotContents(slotA, slotB);
			}
		}
	}
}
