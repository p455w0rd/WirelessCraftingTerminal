package net.p455w0rd.wirelesscraftingterminal.core.sync.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;

public class NetworkHandler {

	public static NetworkHandler instance;

	private final FMLEventChannel ec;
	private final String myChannelName;

	private final IPacketHandler clientHandler;
	private final IPacketHandler serveHandler;

	public NetworkHandler(final String channelName) {
		FMLCommonHandler.instance().bus().register(this);
		this.ec = NetworkRegistry.INSTANCE.newEventDrivenChannel(this.myChannelName = channelName);
		this.ec.register(this);

		this.clientHandler = this.createClientSide();
		this.serveHandler = this.createServerSide();
	}

	private IPacketHandler createClientSide() {
		try {
			return new WCTClientPacketHandler();
		}
		catch (final Throwable t) {
			return null;
		}
	}

	private IPacketHandler createServerSide() {
		try {
			return new WCTServerPacketHandler();
		}
		catch (final Throwable t) {
			return null;
		}
	}

	@SubscribeEvent
	public void serverPacket(final ServerCustomPacketEvent ev) {
		final NetHandlerPlayServer srv = (NetHandlerPlayServer) ev.packet.handler();
		if (this.serveHandler != null) {
			this.serveHandler.onPacketData(null, ev.packet, srv.playerEntity);
		}
	}

	@SubscribeEvent
	public void clientPacket(final ClientCustomPacketEvent ev) {
		if (this.clientHandler != null) {
			this.clientHandler.onPacketData(null, ev.packet, null);
		}
	}

	public String getChannel() {
		return this.myChannelName;
	}

	public void sendToAll(final WCTPacket message) {
		this.ec.sendToAll(message.getProxy());
	}

	public void sendTo(final WCTPacket message, final EntityPlayerMP player) {
		this.ec.sendTo(message.getProxy(), player);
	}

	public void sendToAllAround(final WCTPacket message, final NetworkRegistry.TargetPoint point) {
		this.ec.sendToAllAround(message.getProxy(), point);
	}

	public void sendToDimension(final WCTPacket message, final int dimensionId) {
		this.ec.sendToDimension(message.getProxy(), dimensionId);
	}

	public void sendToServer(final WCTPacket message) {
		this.ec.sendToServer(message.getProxy());
	}
}
