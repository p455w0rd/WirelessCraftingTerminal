package net.p455w0rd.wirelesscraftingterminal.implementation;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.tile.misc.TileSecurity;
import appeng.tile.networking.TileWireless;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.MachineSource;
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.WCTIActionHost;
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.WCTPlayerSource;

/**
 * Provides wireless access to a ME network.
 * 
 * @author Nividica
 * 
 */
public abstract class WirelessAELink implements IStorageMonitorable {

	protected final EntityPlayer player;

	protected final String encryptionKey;

	protected IWirelessAccessPoint accessPoint;

	protected DimensionalCoord apLocation = null;

	protected BaseActionSource actionSource;

	public WirelessAELink(final @Nullable EntityPlayer player, @Nonnull final String encryptionKey) {
		this.player = player;
		this.encryptionKey = encryptionKey;
		this.linkWithNewAP();
	}

	private static boolean isAPInRange(final DimensionalCoord APLocation, final double APRange, final World world, final int x, final int y, final int z) {
		if (!APLocation.isInWorld(world)) {
			return false;
		}
		double squareDistance = getSquaredDistanceFromAP(APLocation, x, y, z);
		return squareDistance <= (APRange * APRange);
	}

	private static ArrayList<IWirelessAccessPoint> locateAPsInRange(final World world, final int x, final int y, final int z, final IGrid grid) {
		IMachineSet accessPoints = grid.getMachines(TileWireless.class);
		if (accessPoints.isEmpty()) {
			return null;
		}
		ArrayList<IWirelessAccessPoint> aps = new ArrayList<IWirelessAccessPoint>();
		for (IGridNode APNode : accessPoints) {
			IWirelessAccessPoint AP = (IWirelessAccessPoint) APNode.getMachine();
			if (AP.isActive()) {
				if (isAPInRange(AP.getLocation(), AP.getRange(), world, x, y, z)) {
					aps.add(AP);
				}
			}
		}

		return aps;
	}

	protected static double getSquaredDistanceFromAP(final DimensionalCoord locationAP, final int x, final int y, final int z) {
		if (locationAP == null) {
			return Double.MAX_VALUE;
		}
		int dX = locationAP.x - x, dY = locationAP.y - y, dZ = locationAP.z - z;
		return ((dX * dX) + (dY * dY) + (dZ * dZ));
	}

	public static ArrayList<IWirelessAccessPoint> locateAPsInRange(final World world, final int x, final int y, final int z, final String encryptionKey) {
		long encryptionValue;
		try {
			encryptionValue = Long.parseLong(encryptionKey);
		}
		catch (NumberFormatException e) {
			return null;
		}

		Object source = AEApi.instance().registries().locatable().getLocatableBy(encryptionValue);

		if (!(source instanceof TileSecurity)) {
			return null;
		}
		TileSecurity securityHost = (TileSecurity) source;
		IGrid grid;
		try {
			grid = securityHost.getGridNode(ForgeDirection.UNKNOWN).getGrid();
		}
		catch (Exception e) {
			return null;
		}

		return locateAPsInRange(world, x, y, z, grid);
	}

	public static ArrayList<IWirelessAccessPoint> locateAPsInRangeOfPlayer(final EntityPlayer player, final String encryptionKey) {
		return locateAPsInRange(player.worldObj, (int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ), encryptionKey);
	}

	private boolean isAPInRangeAndActive() {
		if (this.accessPoint != null) {
			if (this.accessPoint.isActive()) {
				return isAPInRange(this.apLocation, this.accessPoint.getRange(), this.getUserWorld(), this.getUserPositionX(), this.getUserPositionY(), this.getUserPositionZ());
			}
		}
		return false;
	}

	private boolean linkWithNewAP() {
		World w = this.getUserWorld();
		int x = this.getUserPositionX();
		int y = this.getUserPositionY();
		int z = this.getUserPositionZ();

		IGrid grid = null;
		if (this.accessPoint != null) {
			try {
				grid = this.accessPoint.getGrid();
			}
			catch (Exception e) {
			}
		}

		ArrayList<IWirelessAccessPoint> apList;
		if (grid != null) {
			apList = locateAPsInRange(w, x, y, z, grid);
		}
		else {
			apList = locateAPsInRange(w, x, y, z, this.encryptionKey);
		}

		if ((apList == null) || (apList.isEmpty())) {
			this.accessPoint = null;
			return false;
		}

		IWirelessAccessPoint closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (IWirelessAccessPoint ap : apList) {
			double dist = getSquaredDistanceFromAP(ap.getLocation(), x, y, z);
			if (dist < closestDistance) {
				closestDistance = dist;
				closest = ap;
			}
		}
		this.setAP(closest);

		return true;
	}

	private void setAP(final IWirelessAccessPoint accessPoint) {
		this.accessPoint = accessPoint;
		this.apLocation = this.accessPoint.getLocation();
		if (this.player != null) {
			this.actionSource = new WCTPlayerSource(this.player, (WCTIActionHost) this.accessPoint);
		}
		else {
			this.actionSource = new MachineSource((WCTIActionHost) this.accessPoint);
		}
	}

	protected abstract int getUserPositionX();

	protected abstract int getUserPositionY();

	protected abstract int getUserPositionZ();

	protected abstract World getUserWorld();

	protected abstract boolean hasPowerToCommunicate();

	public IEnergyGrid getEnergyGrid() {
		if (this.accessPoint == null) {
			return null;
		}

		try {
			return this.accessPoint.getActionableNode().getGrid().getCache(IEnergyGrid.class);
		}
		catch (Exception e) {
			// Ignored
		}

		return null;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory() {
		if ((this.accessPoint == null) || !this.isConnected()) {
			return null;
		}

		try {
			IStorageGrid storageGrid = this.accessPoint.getActionableNode().getGrid().getCache(IStorageGrid.class);
			return storageGrid.getFluidInventory();
		}
		catch (Exception e) {
			// Ignored
		}

		return null;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory() {
		if ((this.accessPoint == null) || !this.isConnected()) {
			return null;
		}

		try {
			IStorageGrid storageGrid = this.accessPoint.getActionableNode().getGrid().getCache(IStorageGrid.class);
			return storageGrid.getItemInventory();
		}
		catch (Exception e) {
			// Ignored
		}

		return null;
	}

	public boolean isConnected() {
		if (!this.hasPowerToCommunicate()) {
			return false;
		}

		if (this.isAPInRangeAndActive()) {
			return true;
		}

		return this.linkWithNewAP();
	}
}
