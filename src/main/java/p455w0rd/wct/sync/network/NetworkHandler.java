package p455w0rd.wct.sync.network;

import appeng.core.worlddata.WorldData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.*;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.*;
import p455w0rd.wct.sync.WCTPacket;

public class NetworkHandler {
	private static NetworkHandler instance;

	private final FMLEventChannel ec;
	private final String myChannelName;

	private final IPacketHandler clientHandler;
	private final IPacketHandler serveHandler;

	public NetworkHandler(final String channelName) {
		FMLCommonHandler.instance().bus().register(this);
		ec = NetworkRegistry.INSTANCE.newEventDrivenChannel(myChannelName = channelName);
		ec.register(this);

		clientHandler = createClientSide();
		serveHandler = createServerSide();
	}

	public static void init(final String channelName) {
		instance = new NetworkHandler(channelName);
	}

	public static NetworkHandler instance() {
		return instance;
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
	public void newConnection(final ServerConnectionFromClientEvent ev) {
		WorldData.instance().dimensionData().sendToPlayer(ev.getManager());
	}

	@SubscribeEvent
	public void newConnection(final PlayerLoggedInEvent loginEvent) {
		if (loginEvent.player instanceof EntityPlayerMP) {
			WorldData.instance().dimensionData().sendToPlayer(null);
		}
	}

	@SubscribeEvent
	public void serverPacket(final ServerCustomPacketEvent ev) {
		final NetHandlerPlayServer srv = (NetHandlerPlayServer) ev.getPacket().handler();
		if (serveHandler != null) {
			try {
				serveHandler.onPacketData(null, ev.getHandler(), ev.getPacket(), srv.playerEntity);
			}
			catch (final ThreadQuickExitException ignored) {

			}
		}
	}

	@SubscribeEvent
	public void clientPacket(final ClientCustomPacketEvent ev) {
		if (clientHandler != null) {
			try {
				clientHandler.onPacketData(null, ev.getHandler(), ev.getPacket(), null);
			}
			catch (final ThreadQuickExitException ignored) {

			}
		}
	}

	public String getChannel() {
		return myChannelName;
	}

	public void sendToAll(final WCTPacket message) {
		ec.sendToAll(message.getProxy());
	}

	public void sendTo(final WCTPacket message, final EntityPlayerMP player) {
		ec.sendTo(message.getProxy(), player);
	}

	public void sendToAllAround(final WCTPacket message, final NetworkRegistry.TargetPoint point) {
		ec.sendToAllAround(message.getProxy(), point);
	}

	public void sendToDimension(final WCTPacket message, final int dimensionId) {
		ec.sendToDimension(message.getProxy(), dimensionId);
	}

	public void sendToServer(final WCTPacket message) {
		ec.sendToServer(message.getProxy());
	}
}
