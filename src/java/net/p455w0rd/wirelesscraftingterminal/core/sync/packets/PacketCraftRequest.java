package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import java.util.concurrent.Future;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftAmount;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;

public class PacketCraftRequest extends WCTPacket {

	private final long amount;
	private final boolean heldShift;

	public PacketCraftRequest(final ByteBuf stream) {
		this.heldShift = stream.readBoolean();
		this.amount = stream.readLong();
	}

	public PacketCraftRequest(final int craftAmt, final boolean shift) {
		this.amount = craftAmt;
		this.heldShift = shift;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt(this.getPacketID());
		data.writeBoolean(shift);
		data.writeLong(this.amount);

		this.configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		if (player.openContainer instanceof ContainerCraftAmount) {
			final ContainerCraftAmount cca = (ContainerCraftAmount) player.openContainer;
			final Object target = cca.getTarget();
			if (target instanceof IGridHost) {
				final IGridHost gh = (IGridHost) target;
				final IGridNode gn = gh.getGridNode(ForgeDirection.UNKNOWN);
				if (gn == null) {
					return;
				}

				final IGrid g = gn.getGrid();
				if (g == null || cca.getItemToCraft() == null) {
					return;
				}

				cca.getItemToCraft().setStackSize(this.amount);

				Future<ICraftingJob> futureJob = null;
				try {
					final ICraftingGrid cg = g.getCache(ICraftingGrid.class);
					futureJob = cg.beginCraftingJob(cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.getItemToCraft(), null);

					int x = player.serverPosX;
					int y = player.serverPosY;
					int z = player.serverPosZ;

					WCTGuiHandler.launchGui(Reference.GUI_CRAFT_CONFIRM, player, player.worldObj, x, y, z);

					if (player.openContainer instanceof ContainerCraftConfirm) {
						final ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;
						ccc.setAutoStart(this.heldShift);
						ccc.setJob(futureJob);
						cca.detectAndSendChanges();
					}
				}
				catch (final Throwable e) {
					if (futureJob != null) {
						futureJob.cancel(true);
					}
					WCTLog.debug(e.getMessage());
				}
			}
		}
	}
}
