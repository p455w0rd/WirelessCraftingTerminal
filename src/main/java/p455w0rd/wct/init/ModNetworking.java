/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.init;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import p455w0rd.wct.WCT;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.IPacketHandler;
import p455w0rd.wct.sync.network.WCTClientPacketHandler;
import p455w0rd.wct.sync.network.WCTServerPacketHandler;

public class ModNetworking {

	private static final ModNetworking INSTANCE = new ModNetworking();
	private static final String CHANNEL_NAME = "WCT";
	private static final FMLEventChannel CHANNEL = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNEL_NAME);;

	private static final IPacketHandler clientHandler = WCTClientPacketHandler.instance();
	private static final IPacketHandler serverHandler = WCTServerPacketHandler.instance();

	private ModNetworking() {
	}

	public static void preInit() {
		MinecraftForge.EVENT_BUS.register(instance());
		getEventChannel().register(instance());
	}

	public static void postInit() {
		NetworkRegistry.INSTANCE.registerGuiHandler(WCT.INSTANCE, new ModGuiHandler());
	}

	public static ModNetworking instance() {
		return INSTANCE;
	}

	public static FMLEventChannel getEventChannel() {
		return CHANNEL;
	}

	public IPacketHandler getClientHandler() {
		return clientHandler;
	}

	public IPacketHandler getServerHandler() {
		return serverHandler;
	}

	public String getChannel() {
		return CHANNEL_NAME;
	}

	public void sendToAll(final WCTPacket message) {
		getEventChannel().sendToAll(message.getProxy());
	}

	public void sendTo(final WCTPacket message, final EntityPlayerMP player) {
		getEventChannel().sendTo(message.getProxy(), player);
	}

	public void sendToAllAround(final WCTPacket message, final NetworkRegistry.TargetPoint point) {
		getEventChannel().sendToAllAround(message.getProxy(), point);
	}

	public void sendToDimension(final WCTPacket message, final int dimensionId) {
		getEventChannel().sendToDimension(message.getProxy(), dimensionId);
	}

	public void sendToServer(final WCTPacket message) {
		getEventChannel().sendToServer(message.getProxy());
	}

	@SubscribeEvent
	public void serverPacket(final ServerCustomPacketEvent ev) {
		final NetHandlerPlayServer srv = (NetHandlerPlayServer) ev.getPacket().handler();
		WCTServerPacketHandler.instance().onPacketData(null, ev.getHandler(), ev.getPacket(), srv.player);
	}

	@SubscribeEvent
	public void clientPacket(final ClientCustomPacketEvent ev) {
		WCTClientPacketHandler.instance().onPacketData(null, ev.getHandler(), ev.getPacket(), null);
	}

}
