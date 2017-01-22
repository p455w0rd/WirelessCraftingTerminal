package p455w0rd.wct.sync.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.*;
import net.minecraft.network.*;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import p455w0rd.wct.sync.*;

public final class WCTServerPacketHandler extends WCTPacketHandlerBase implements IPacketHandler {

	@SuppressWarnings("unchecked")
	@Override
	public void onPacketData(final INetworkInfo manager, final INetHandler handler, final FMLProxyPacket packet, final EntityPlayer player) {
		final ByteBuf stream = packet.payload();

		try {
			final int packetType = stream.readInt();
			final WCTPacket pack = PacketTypes.getPacket(packetType).parsePacket(stream);

			final PacketCallState callState = new PacketCallState() {

				@Override
				public void call(final WCTPacket appEngPacket) {
					appEngPacket.serverPacketData(manager, appEngPacket, player);
				}
			};

			pack.setCallParam(callState);
			PacketThreadUtil.checkThreadAndEnqueue(pack, handler, ((EntityPlayerMP) player).getServer());
			callState.call(pack);
		}
		catch (final Exception e) {
		}
	}
}
