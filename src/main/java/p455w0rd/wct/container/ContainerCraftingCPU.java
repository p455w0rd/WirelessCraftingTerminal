package p455w0rd.wct.container;

import java.io.IOException;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.InventoryAction;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import p455w0rd.wct.container.guisync.GuiSync;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketMEInventoryUpdate;
import p455w0rd.wct.sync.packets.PacketValueConfig;

public class ContainerCraftingCPU extends WCTBaseContainer implements IMEMonitorHandlerReceiver<IAEItemStack>, ICustomNameObject {

	private final IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
	private IGrid network;
	private CraftingCPUCluster monitor = null;
	private String cpuName = null;

	@GuiSync(0)
	public long eta = -1;

	public ContainerCraftingCPU(final InventoryPlayer ip, final Object te) {
		super(ip, te);
		final IActionHost host = (IActionHost) (te instanceof IActionHost ? te : null);

		if (host != null && host.getActionableNode() != null) {
			setNetwork(host.getActionableNode().getGrid());
		}

		if (te instanceof TileCraftingTile) {
			setCPU((ICraftingCPU) ((TileCraftingTile) te).getCluster());
		}

		if (getNetwork() == null && Platform.isServer()) {
			setValidContainer(false);
		}
	}

	protected void setCPU(final ICraftingCPU c) {
		if (c == getMonitor()) {
			return;
		}

		if (getMonitor() != null) {
			getMonitor().removeListener(this);
		}

		for (final Object g : listeners) {
			if (g instanceof EntityPlayer) {
				try {
					NetworkHandler.instance().sendTo(new PacketValueConfig("CraftingStatus", "Clear"), (EntityPlayerMP) g);
				}
				catch (final IOException e) {
				}
			}
		}

		if (c instanceof CraftingCPUCluster) {
			cpuName = c.getName();
			setMonitor((CraftingCPUCluster) c);
			list.resetStatus();
			getMonitor().getListOfItem(list, CraftingItemList.ALL);
			getMonitor().addListener(this, null);
			setEstimatedTime(0);
		}
		else {
			setMonitor(null);
			cpuName = "";
			setEstimatedTime(-1);
		}
	}

	public void cancelCrafting() {
		if (getMonitor() != null) {
			getMonitor().cancel();
		}
		setEstimatedTime(-1);
	}

	@Override
	public void removeListener(final IContainerListener c) {
		super.removeListener(c);

		if (listeners.isEmpty() && getMonitor() != null) {
			getMonitor().removeListener(this);
		}
	}

	@Override
	public void onContainerClosed(final EntityPlayer player) {
		super.onContainerClosed(player);
		if (getMonitor() != null) {
			getMonitor().removeListener(this);
		}
	}

	@Override
	public void detectAndSendChanges() {
		if (Platform.isServer() && getMonitor() != null && !list.isEmpty()) {
			try {
				if (getEstimatedTime() >= 0) {
					final long elapsedTime = getMonitor().getElapsedTime();
					final double remainingItems = getMonitor().getRemainingItemCount();
					final double startItems = getMonitor().getStartItemCount();
					final long eta = (long) (elapsedTime / Math.max(1d, (startItems - remainingItems)) * remainingItems);
					setEstimatedTime(eta);
				}

				final PacketMEInventoryUpdate a = new PacketMEInventoryUpdate((byte) 0);
				final PacketMEInventoryUpdate b = new PacketMEInventoryUpdate((byte) 1);
				final PacketMEInventoryUpdate c = new PacketMEInventoryUpdate((byte) 2);

				for (final IAEItemStack out : list) {
					a.appendItem(getMonitor().getItemStack(out, CraftingItemList.STORAGE));
					b.appendItem(getMonitor().getItemStack(out, CraftingItemList.ACTIVE));
					c.appendItem(getMonitor().getItemStack(out, CraftingItemList.PENDING));
				}

				list.resetStatus();

				for (final Object g : listeners) {
					if (g instanceof EntityPlayer) {
						if (!a.isEmpty()) {
							NetworkHandler.instance().sendTo(a, (EntityPlayerMP) g);
						}

						if (!b.isEmpty()) {
							NetworkHandler.instance().sendTo(b, (EntityPlayerMP) g);
						}

						if (!c.isEmpty()) {
							NetworkHandler.instance().sendTo(c, (EntityPlayerMP) g);
						}
					}
				}
			}
			catch (final IOException e) {
				// :P
			}
		}
		super.detectAndSendChanges();
	}

	@Override
	public boolean isValid(final Object verificationToken) {
		return true;
	}

	@Override
	public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final BaseActionSource actionSource) {
		for (IAEItemStack is : change) {
			is = is.copy();
			is.setStackSize(1);
			list.add(is);
		}
	}

	@Override
	public void onListUpdate() {

	}

	@Override
	public String getCustomName() {
		return cpuName;
	}

	@Override
	public String getCustomInventoryName() {
		return getCustomName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return cpuName != null && cpuName.length() > 0;
	}

	public long getEstimatedTime() {
		return eta;
	}

	private void setEstimatedTime(final long eta) {
		this.eta = eta;
	}

	CraftingCPUCluster getMonitor() {
		return monitor;
	}

	private void setMonitor(final CraftingCPUCluster monitor) {
		this.monitor = monitor;
	}

	/*
		IGrid getNetwork() {
			return obj.getTargetGrid();
		}
		*/
	IGrid getNetwork() {
		return network;
	}

	private void setNetwork(final IGrid network) {
		this.network = network;
	}

	@Override
	public void doAction(EntityPlayerMP player, InventoryAction action, int slot, long id) {
	}
}
