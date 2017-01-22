package p455w0rd.wct.container;

import java.io.IOException;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.ICustomNameObject;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import p455w0rd.wct.api.IWirelessCraftingTermHandler;
import p455w0rd.wct.container.guisync.GuiSync;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketMEInventoryUpdate;
import p455w0rd.wct.sync.packets.PacketValueConfig;
import p455w0rd.wct.util.WCTUtils;

public class ContainerCraftingCPU extends WCTBaseContainer implements IMEMonitorHandlerReceiver<IAEItemStack>, ICustomNameObject {

	private final IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
	private CraftingCPUCluster monitor = null;
	private String cpuName = null;
	private final WCTGuiObject obj;

	@GuiSync(0)
	public long eta = -1;

	public ContainerCraftingCPU(final InventoryPlayer ip, final Object te) {
		super(ip, te);
		obj = getGuiObject(WCTUtils.getWirelessTerm(ip), ip.player, ip.player.worldObj, (int) ip.player.posX, (int) ip.player.posY, (int) ip.player.posZ);

		if (obj == null || (getNetwork() == null && Platform.isServer())) {
			setValidContainer(false);
		}

	}

	@Override
	protected WCTGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (it != null) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WCTGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
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
	public boolean hasCustomName() {
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

	IGrid getNetwork() {
		return obj.getTargetGrid();
	}
}
