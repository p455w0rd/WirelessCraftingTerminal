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

import java.util.concurrent.Future;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import p455w0rd.wct.api.networking.security.WCTIActionHost;
import p455w0rd.wct.container.ContainerCraftAmount;
import p455w0rd.wct.container.ContainerCraftConfirm;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;
import p455w0rd.wct.util.WCTUtils;

public class PacketCraftRequest extends WCTPacket {

	private final long amount;
	private final boolean heldShift;

	public PacketCraftRequest(final ByteBuf stream) {
		heldShift = stream.readBoolean();
		amount = stream.readLong();
	}

	public PacketCraftRequest(final int craftAmt, final boolean shift) {
		amount = craftAmt;
		heldShift = shift;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt(getPacketID());
		data.writeBoolean(shift);
		data.writeLong(amount);

		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerCraftAmount) {
			final ContainerCraftAmount cca = (ContainerCraftAmount) player.openContainer;
			final Object target = cca.getTarget();//.getTarget();
			if (target instanceof WCTIActionHost) {
				final WCTIActionHost ah = (WCTIActionHost) target;
				final IGridNode gn = ah.getActionableNode(true);

				if (gn == null) {
					return;
				}

				final IGrid g = gn.getGrid();
				if (g == null || cca.getItemToCraft() == null) {
					return;
				}

				cca.getItemToCraft().setStackSize(amount);

				Future<ICraftingJob> futureJob = null;
				try {
					final ICraftingGrid cg = g.getCache(ICraftingGrid.class);
					futureJob = cg.beginCraftingJob(cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.getItemToCraft(), null);

					int x = (int) player.posX;
					int y = (int) player.posY;
					int z = (int) player.posZ;

					ModGuiHandler.open(ModGuiHandler.GUI_CRAFT_CONFIRM, player, WCTUtils.world(player), new BlockPos(x, y, z));
					//Platform.openGUI(player, null, cca.getOpenContext().getSide(), GuiBridge.GUI_CRAFTING_CONFIRM);

					if (player.openContainer instanceof ContainerCraftConfirm) {
						final ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;
						ccc.setAutoStart(heldShift);
						ccc.setJob(futureJob);
						cca.detectAndSendChanges();
					}
				}
				catch (final Throwable e) {
					if (futureJob != null) {
						futureJob.cancel(true);
					}
				}
			}
		}
	}
}
