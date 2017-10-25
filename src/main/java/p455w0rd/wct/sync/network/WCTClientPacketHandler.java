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
package p455w0rd.wct.sync.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketThreadUtil;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import p455w0rd.wct.sync.PacketCallState;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.WCTPacketHandlerBase;
import p455w0rd.wct.util.WCTUtils;

public class WCTClientPacketHandler extends WCTPacketHandlerBase implements IPacketHandler {

	private static final WCTClientPacketHandler INSTANCE = new WCTClientPacketHandler();

	public static final WCTClientPacketHandler instance() {
		return INSTANCE;
	}

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
					appEngPacket.clientPacketData(manager, appEngPacket, WCTUtils.player());
				}
			};

			pack.setCallParam(callState);
			PacketThreadUtil.checkThreadAndEnqueue(pack, handler, Minecraft.getMinecraft());
			callState.call(pack);
		}
		catch (final Exception e) {
		}
	}
}