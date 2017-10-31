package p455w0rd.wct.sync;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.sync.network.NetworkHandler;

@SuppressWarnings("rawtypes")
public abstract class WCTPacket implements Packet {

	private PacketBuffer p;
	private PacketCallState caller;

	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		throw new UnsupportedOperationException("This packet ( " + getPacketID() + " does not implement a server side handler.");
	}

	public final int getPacketID() {
		return WCTPacketHandlerBase.PacketTypes.getID(this.getClass()).ordinal();
	}

	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		throw new UnsupportedOperationException("This packet ( " + getPacketID() + " does not implement a client side handler.");
	}

	protected void configureWrite(final ByteBuf data) {
		data.capacity(data.readableBytes());
		p = new PacketBuffer(data);
	}

	public FMLProxyPacket getProxy() {
		if (p.array().length > 2 * 1024 * 1024) // 2k walking room :)
		{
			throw new IllegalArgumentException("Sorry AE2 made a " + p.array().length + " byte packet by accident!");
		}

		final FMLProxyPacket pp = new FMLProxyPacket(p, NetworkHandler.instance().getChannel());

		return pp;
	}

	@Override
	public void readPacketData(final PacketBuffer buf) throws IOException {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public void writePacketData(final PacketBuffer buf) throws IOException {
		throw new RuntimeException("Not Implemented");
	}

	public void setCallParam(final PacketCallState call) {
		caller = call;
	}

	@Override
	public void processPacket(final INetHandler handler) {
		caller.call(this);
	}

}
