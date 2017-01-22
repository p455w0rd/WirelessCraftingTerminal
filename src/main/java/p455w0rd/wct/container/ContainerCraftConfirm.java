package p455w0rd.wct.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import p455w0rd.wct.api.networking.security.WCTIActionHost;
import p455w0rd.wct.api.networking.security.WCTPlayerSource;
import p455w0rd.wct.container.guisync.GuiSync;
import p455w0rd.wct.handlers.GuiHandler;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketMEInventoryUpdate;
import p455w0rd.wct.sync.packets.PacketSetJobBytes;
import p455w0rd.wct.sync.packets.PacketSwitchGuis;
import p455w0rd.wct.sync.packets.PacketUpdateCPUInfo;

public class ContainerCraftConfirm extends WCTBaseContainer {

	public final ArrayList<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();
	private Future<ICraftingJob> job;
	private ICraftingJob result;
	@GuiSync(0)
	public long bytesUsed;
	@GuiSync(1)
	public long cpuBytesAvail;
	@GuiSync(2)
	public int cpuCoProcessors;
	@GuiSync(3)
	public boolean autoStart = false;
	@GuiSync(4)
	public boolean simulation = true;
	@GuiSync(5)
	public int selectedCpu = -1;
	@GuiSync(6)
	public boolean noCPU = true;
	@GuiSync(7)
	public String myName = "";
	InventoryPlayer inventoryPlayer;

	public ContainerCraftConfirm(final InventoryPlayer ip, final ITerminalHost te) {
		super(ip, te);
		inventoryPlayer = ip;
	}

	public void cycleCpu(final boolean next) {
		if (next) {
			setSelectedCpu(getSelectedCpu() + 1);
		}
		else {
			setSelectedCpu(getSelectedCpu() - 1);
		}

		if (getSelectedCpu() < -1) {
			setSelectedCpu(cpus.size() - 1);
		}
		else if (getSelectedCpu() >= cpus.size()) {
			setSelectedCpu(-1);
		}

		if (getSelectedCpu() == -1) {
			setCpuAvailableBytes(0);
			setCpuCoProcessors(0);
			setName("");
			try {
				NetworkHandler.instance().sendTo(new PacketUpdateCPUInfo(0, 0), (EntityPlayerMP) getPlayerInv().player);
			}
			catch (IOException e) {
				//
			}
		}
		else {
			setName(cpus.get(getSelectedCpu()).getName());
			setCpuAvailableBytes(cpus.get(getSelectedCpu()).getSize());
			setCpuCoProcessors(cpus.get(getSelectedCpu()).getProcessors());

			try {
				NetworkHandler.instance().sendTo(new PacketUpdateCPUInfo((int) getCpuAvailableBytes(), getCpuCoProcessors()), (EntityPlayerMP) getPlayerInv().player);
			}
			catch (IOException e) {
				//
			}
		}
	}

	@Override
	public void detectAndSendChanges() {
		if (Platform.isClient()) {
			return;
		}

		final ICraftingGrid cc = getGrid().getCache(ICraftingGrid.class);
		final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

		int matches = 0;
		boolean changed = false;
		for (final ICraftingCPU c : cpuSet) {
			boolean found = false;
			for (final CraftingCPURecord ccr : cpus) {
				if (ccr.getCpu() == c) {
					found = true;
				}
			}

			final boolean matched = cpuMatches(c);

			if (matched) {
				matches++;
			}

			if (found == !matched) {
				changed = true;
			}
		}

		if (changed || cpus.size() != matches) {
			cpus.clear();
			for (final ICraftingCPU c : cpuSet) {
				if (cpuMatches(c)) {
					cpus.add(new CraftingCPURecord(c.getAvailableStorage(), c.getCoProcessors(), c));
				}
			}

			sendCPUs();
		}

		setNoCPU(cpus.isEmpty());

		super.detectAndSendChanges();

		if (getJob() != null && getJob().isDone()) {
			try {
				result = getJob().get();

				if (!result.isSimulation()) {
					setSimulation(false);
					if (isAutoStart()) {
						startJob();
						return;
					}
				}
				else {
					setSimulation(true);
				}

				try {
					final PacketMEInventoryUpdate a = new PacketMEInventoryUpdate((byte) 0);
					final PacketMEInventoryUpdate b = new PacketMEInventoryUpdate((byte) 1);
					final PacketMEInventoryUpdate c = result.isSimulation() ? new PacketMEInventoryUpdate((byte) 2) : null;

					final IItemList<IAEItemStack> plan = AEApi.instance().storage().createItemList();
					result.populatePlan(plan);

					try {
						NetworkHandler.instance().sendTo(new PacketSetJobBytes((int) result.getByteTotal()), (EntityPlayerMP) getPlayerInv().player);
					}
					catch (IOException e) {
						//
					}

					setUsedBytes(result.getByteTotal());

					for (final IAEItemStack out : plan) {

						IAEItemStack o = out.copy();
						o.reset();
						o.setStackSize(out.getStackSize());

						final IAEItemStack p = out.copy();
						p.reset();
						p.setStackSize(out.getCountRequestable());

						final IStorageGrid sg = getGrid().getCache(IStorageGrid.class);
						final IMEInventory<IAEItemStack> items = sg.getItemInventory();

						IAEItemStack m = null;
						if (c != null && result.isSimulation()) {
							m = o.copy();
							o = items.extractItems(o, Actionable.SIMULATE, getActionSource());

							if (o == null) {
								o = m.copy();
								o.setStackSize(0);
							}

							m.setStackSize(m.getStackSize() - o.getStackSize());
						}

						if (o.getStackSize() > 0) {
							a.appendItem(o);
						}

						if (p.getStackSize() > 0) {
							b.appendItem(p);
						}

						if (c != null && m != null && m.getStackSize() > 0) {
							c.appendItem(m);
						}
					}

					for (final Object g : listeners) {
						if (g instanceof EntityPlayer) {
							NetworkHandler.instance().sendTo(a, (EntityPlayerMP) g);
							NetworkHandler.instance().sendTo(b, (EntityPlayerMP) g);
							if (c != null) {
								NetworkHandler.instance().sendTo(c, (EntityPlayerMP) g);
							}
						}
					}
				}
				catch (final IOException e) {
					// :P
				}
			}
			catch (final Throwable e) {
				getPlayerInv().player.addChatMessage(new TextComponentString("Error: " + e.toString()));
				setValidContainer(false);
				result = null;
			}

			setJob(null);
		}
		verifyPermissions(SecurityPermissions.CRAFT, false);
	}

	private IGrid getGrid() {
		final WCTIActionHost h = ((WCTIActionHost) getTarget());
		return h.getActionableNode().getGrid();
		//return obj2.getTargetGrid();
	}

	private boolean cpuMatches(final ICraftingCPU c) {
		return c.getAvailableStorage() >= getUsedBytes() && !c.isBusy();
	}

	private void sendCPUs() {
		Collections.sort(cpus);

		if (getSelectedCpu() >= cpus.size()) {
			setSelectedCpu(-1);
			setCpuAvailableBytes(0);
			setCpuCoProcessors(0);
			setName("");
		}
		else if (getSelectedCpu() != -1) {
			setName(cpus.get(getSelectedCpu()).getName());
			setCpuAvailableBytes(cpus.get(getSelectedCpu()).getSize());
			setCpuCoProcessors(cpus.get(getSelectedCpu()).getProcessors());
		}
	}

	public void startJob() {
		int originalGui = 0;

		final WCTIActionHost ah = getActionHost();
		if (ah instanceof WCTGuiObject) {
			originalGui = GuiHandler.GUI_WCT;
		}

		if (result != null && !isSimulation()) {
			final ICraftingGrid cc = getGrid().getCache(ICraftingGrid.class);
			final ICraftingLink g = cc.submitJob(result, null, getSelectedCpu() == -1 ? null : cpus.get(getSelectedCpu()).getCpu(), true, getActionSrc());
			setAutoStart(false);
			if (g != null && originalGui == 0)// && this.getOpenContext() != null )
			{
				NetworkHandler.instance().sendTo(new PacketSwitchGuis(originalGui), (EntityPlayerMP) getInventoryPlayer().player);

				//final TileEntity te = this.getOpenContext().getTile();
				//Platform.openGUI( this.getInventoryPlayer().player, te, this.getOpenContext().getSide(), originalGui );
				EntityPlayerMP player = (EntityPlayerMP) getInventoryPlayer().player;
				World world = player.worldObj;
				int x = (int) player.posX;
				int y = (int) player.posY;
				int z = (int) player.posZ;
				GuiHandler.open(originalGui, player, world, new BlockPos(x, y, z));
			}
		}
	}

	private BaseActionSource getActionSrc() {
		return new WCTPlayerSource(getPlayerInv().player, (WCTIActionHost) getTarget());
	}

	@Override
	public void removeListener(final IContainerListener c) {
		super.removeListener(c);
		if (getJob() != null) {
			getJob().cancel(true);
			setJob(null);
		}
	}

	@Override
	public void onContainerClosed(final EntityPlayer par1EntityPlayer) {
		super.onContainerClosed(par1EntityPlayer);
		if (getJob() != null) {
			getJob().cancel(true);
			setJob(null);
		}
	}

	public World getWorld() {
		return getPlayerInv().player.worldObj;
	}

	public boolean isAutoStart() {
		return autoStart;
	}

	public void setAutoStart(final boolean autoStart) {
		this.autoStart = autoStart;
	}

	public long getUsedBytes() {
		return bytesUsed;
	}

	private void setUsedBytes(final long bytesUsed) {
		this.bytesUsed = bytesUsed;
	}

	public long getCpuAvailableBytes() {
		return cpuBytesAvail;
	}

	private void setCpuAvailableBytes(final long cpuBytesAvail) {
		this.cpuBytesAvail = cpuBytesAvail;
	}

	public int getCpuCoProcessors() {
		return cpuCoProcessors;
	}

	private void setCpuCoProcessors(final int cpuCoProcessors) {
		this.cpuCoProcessors = cpuCoProcessors;
	}

	public int getSelectedCpu() {
		return selectedCpu;
	}

	private void setSelectedCpu(final int selectedCpu) {
		this.selectedCpu = selectedCpu;
	}

	public String getName() {
		return myName;
	}

	private void setName(@Nonnull final String myName) {
		this.myName = myName;
	}

	public boolean hasNoCPU() {
		return noCPU;
	}

	private void setNoCPU(final boolean noCPU) {
		this.noCPU = noCPU;
	}

	public boolean isSimulation() {
		return simulation;
	}

	private void setSimulation(final boolean simulation) {
		this.simulation = simulation;
	}

	public Future<ICraftingJob> getJob() {
		return job;
	}

	public void setJob(final Future<ICraftingJob> job) {
		this.job = job;
	}
}
