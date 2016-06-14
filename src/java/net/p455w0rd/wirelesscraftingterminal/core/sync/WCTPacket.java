package net.p455w0rd.wirelesscraftingterminal.core.sync;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;

public abstract class WCTPacket {

	private ByteBuf p;

	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		throw new UnsupportedOperationException("This packet ( " + this.getPacketID() + " does not implement a server side handler.");
	}

	public final int getPacketID() {
		return WCTPacketHandlerBase.PacketTypes.getID(this.getClass()).ordinal();
	}

	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		throw new UnsupportedOperationException("This packet ( " + this.getPacketID() + " does not implement a client side handler.");
	}

	protected void configureWrite(final ByteBuf data) {
		data.capacity(data.readableBytes());
		this.p = data;
	}

	public FMLProxyPacket getProxy() {
		if (this.p.array().length > 2 * 1024 * 1024) {
			throw new IllegalArgumentException("Sorry WCT made a " + this.p.array().length + " byte packet by accident!");
		}

		final FMLProxyPacket pp = new FMLProxyPacket(this.p, NetworkHandler.instance.getChannel());

		return pp;
	}
}