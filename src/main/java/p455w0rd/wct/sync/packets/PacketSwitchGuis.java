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
package p455w0rd.wct.sync.packets;

import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import p455w0rd.wct.api.IWCTContainer;
import p455w0rd.wct.client.gui.GuiWCT;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.ae2wtlib.api.container.IWTContainer;
import p455w0rd.ae2wtlib.api.networking.INetworkInfo;

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
		World world = player.getEntityWorld();
		int x = (int) player.posX;
		int y = (int) player.posY;
		int z = (int) player.posZ;
		Container c = player.openContainer;
		boolean isBauble = false;
		boolean isHeld = false;
		int slot = -1;
		if (c instanceof IWTContainer) {
			IWTContainer wtContainer = (IWTContainer) c;
			isBauble = wtContainer.isWTBauble();
			slot = wtContainer.getWTSlot();
		}
		if (c instanceof IWCTContainer) {
			isHeld = ((IWCTContainer) c).isMagnetHeld();
		}
		//WCTApi.instance().openWCTGui(player, isBauble, slot);
		ModGuiHandler.open(newGui, player, world, new BlockPos(x, y, z), isHeld, isBauble, slot);
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		GuiWCT.setSwitchingGuis(true);
	}
}
