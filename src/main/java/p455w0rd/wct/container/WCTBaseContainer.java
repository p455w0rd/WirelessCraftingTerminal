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
package p455w0rd.wct.container;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotInaccessible;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import p455w0rd.wct.api.IWirelessCraftingTermHandler;
import p455w0rd.wct.api.networking.security.WCTIActionHost;
import p455w0rd.wct.api.networking.security.WCTPlayerSource;
import p455w0rd.wct.container.guisync.GuiSync;
import p455w0rd.wct.container.guisync.SyncData;
import p455w0rd.wct.container.slot.SlotPlayerHotBar;
import p455w0rd.wct.container.slot.SlotPlayerInv;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketInventoryAction;
import p455w0rd.wct.sync.packets.PacketMEInventoryUpdate;
import p455w0rd.wct.sync.packets.PacketPartialItem;
import p455w0rd.wct.sync.packets.PacketValueConfig;

public abstract class WCTBaseContainer extends AEBaseContainer {

	protected final InventoryPlayer inventoryPlayer;
	protected IActionSource mySrc;
	protected final HashSet<Integer> locked = new HashSet<Integer>();
	public WCTGuiObject obj;
	protected final List<PacketPartialItem> dataChunks = new LinkedList<PacketPartialItem>();
	protected final HashMap<Integer, SyncData> syncData = new HashMap<Integer, SyncData>();
	private boolean isContainerValid = true;
	protected String customName;
	protected ContainerOpenContext openContext;
	protected IMEInventoryHandler<IAEItemStack> cellInv;
	protected IEnergySource powerSrc;
	protected boolean sentCustomName;
	protected int ticksSinceCheck = 900;
	protected IAEItemStack clientRequestedTargetItem = null;
	protected IMEMonitor<IAEItemStack> monitor;

	public WCTBaseContainer(final InventoryPlayer ip, final Object anchor) {
		super(ip, null, null);
		inventoryPlayer = ip;
		obj = anchor instanceof WCTGuiObject ? (WCTGuiObject) anchor : null;

		if (obj == null) {
			setValidContainer(false);
		}
		else {
			mySrc = new WCTPlayerSource(ip.player, getActionHost(obj));
		}
		prepareSync();
	}

	public List<IContainerListener> getListeners() {
		return listeners;
	}

	protected static WCTGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (!it.isEmpty()) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WCTGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	protected static WCTIActionHost getActionHost(Object object) {
		if (object instanceof WCTIActionHost) {
			return (WCTIActionHost) object;
		}
		return null;
	}

	public void prepareSync() {
		for (final Field f : this.getClass().getFields()) {
			if (f.isAnnotationPresent(GuiSync.class)) {
				final GuiSync annotation = f.getAnnotation(GuiSync.class);
				if (!syncData.containsKey(annotation.value())) {
					syncData.put(annotation.value(), new SyncData(this, f, annotation));
				}
			}
		}
	}

	protected void queueInventory(final IContainerListener c) {
		if (Platform.isServer() && c instanceof EntityPlayerMP && monitor != null) {
			try {
				PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();
				final IItemList<IAEItemStack> monitorCache = monitor.getStorageList();

				for (final IAEItemStack send : monitorCache) {
					try {
						piu.appendItem(send);
					}
					catch (final BufferOverflowException boe) {
						ModNetworking.instance().sendTo(piu, (EntityPlayerMP) c);

						piu = new PacketMEInventoryUpdate();
						piu.appendItem(send);
					}
				}
				if (piu != null && c != null) {
					ModNetworking.instance().sendTo(piu, (EntityPlayerMP) c);
				}
			}
			catch (final IOException e) {
			}
		}
	}

	public void postPartial(final PacketPartialItem packetPartialItem) {
		dataChunks.add(packetPartialItem);
		if (packetPartialItem.getPageCount() == dataChunks.size()) {
			parsePartials();
		}
	}

	private void parsePartials() {
		int total = 0;
		for (final PacketPartialItem ppi : dataChunks) {
			total += ppi.getSize();
		}

		final byte[] buffer = new byte[total];
		int cursor = 0;

		for (final PacketPartialItem ppi : dataChunks) {
			cursor = ppi.write(buffer, cursor);
		}

		try {
			final NBTTagCompound data = CompressedStreamTools.readCompressed(new ByteArrayInputStream(buffer));
			if (data != null) {
				setTargetStack(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(new ItemStack(data)));
			}
		}
		catch (final IOException e) {
		}

		dataChunks.clear();
	}

	@Override
	public IAEItemStack getTargetStack() {
		return clientRequestedTargetItem;
	}

	@Override
	public void setTargetStack(final IAEItemStack stack) {
		// client doesn't need to re-send, makes for lower overhead rapid packets.
		if (Platform.isClient()) {
			/*
			final ItemStack a = stack == null ? ItemStack.EMPTY : stack.createItemStack();
			final ItemStack b = clientRequestedTargetItem == null ? ItemStack.EMPTY : clientRequestedTargetItem.createItemStack();
			
			if (Platform.itemComparisons().isSameItem(a, b)) {
				return;
			}
			*/
			if (stack == null && clientRequestedTargetItem == null) {
				return;
			}
			if (stack != null && stack.isSameType(clientRequestedTargetItem)) {
				return;
			}

			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			final NBTTagCompound item = new NBTTagCompound();

			if (stack != null) {
				stack.writeToNBT(item);
			}

			try {
				CompressedStreamTools.writeCompressed(item, stream);

				final int maxChunkSize = 30000;
				final List<byte[]> miniPackets = new LinkedList<byte[]>();

				final byte[] data = stream.toByteArray();

				final ByteArrayInputStream bis = new ByteArrayInputStream(data, 0, stream.size());
				while (bis.available() > 0) {
					final int nextBLock = bis.available() > maxChunkSize ? maxChunkSize : bis.available();
					final byte[] nextSegment = new byte[nextBLock];
					bis.read(nextSegment);
					miniPackets.add(nextSegment);
				}
				bis.close();
				stream.close();

				int page = 0;
				for (final byte[] packet : miniPackets) {
					final PacketPartialItem ppi = new PacketPartialItem(page, miniPackets.size(), packet);
					page++;
					ModNetworking.instance().sendToServer(ppi);
				}
			}
			catch (final IOException e) {
				return;
			}
		}

		clientRequestedTargetItem = stack == null ? null : stack.copy();
	}

	@Override
	public IActionSource getActionSource() {
		return mySrc;
	}

	@Override
	public void verifyPermissions(final SecurityPermissions security, final boolean requirePower) {
		if (Platform.isClient()) {
			return;
		}

		ticksSinceCheck++;
		if (ticksSinceCheck < 20) {
			return;
		}

		ticksSinceCheck = 0;
		setValidContainer(isValidContainer() && hasAccess(security, requirePower));
	}

	@Override
	protected boolean hasAccess(final SecurityPermissions perm, final boolean requirePower) {
		final IGrid grid = obj.getTargetGrid();
		if (grid != null) {
			final IEnergyGrid eg = grid.getCache(IEnergyGrid.class);
			if (!eg.isNetworkPowered()) {
				return false;
			}
		}
		final ISecurityGrid sg = grid.getCache(ISecurityGrid.class);
		if (sg.hasPermission(getInventoryPlayer().player, perm)) {
			return true;
		}
		return false;
	}

	@Override
	public void lockPlayerInventorySlot(final int idx) {
		locked.add(idx);
	}

	@Override
	public Object getTarget() {
		if (obj != null) {
			return obj;
		}
		return null;
	}

	@Override
	public InventoryPlayer getPlayerInv() {
		return getInventoryPlayer();
	}

	/*
		public final void updateFullProgressBar(final int idx, final long value) {
			if (syncData.containsKey(idx)) {
				syncData.get(idx).update(value);
				return;
			}
	
			updateProgressBar(idx, (int) value);
		}
	*/
	@Override
	public void stringSync(final int idx, final String value) {
		if (syncData.containsKey(idx)) {
			syncData.get(idx).update(value);
		}
	}

	@Override
	protected void bindPlayerInventory(final InventoryPlayer inventoryPlayer, final int offsetX, final int offsetY) {
		IItemHandler ih = new PlayerInvWrapper(inventoryPlayer);
		// bind player inventory
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				if (locked.contains(j + i * 9 + 9)) {
					addSlotToContainer(new SlotDisabled(ih, j + i * 9 + 9, j * 18 + offsetX, offsetY + i * 18));
				}
				else {
					addSlotToContainer(new SlotPlayerInv(ih, j + i * 9 + 9, j * 18 + offsetX, offsetY + i * 18));
				}
			}
		}

		// bind player hotbar
		for (int i = 0; i < 9; i++) {
			if (locked.contains(i)) {
				addSlotToContainer(new SlotDisabled(ih, i, i * 18 + offsetX, offsetY + 58));
			}
			else {
				addSlotToContainer(new SlotPlayerHotBar(ih, i, i * 18 + offsetX, offsetY + 58));
			}
		}
	}

	public IItemHandler getInventoryByName(final String name) {
		return null;
	}

	@Override
	public Slot addSlotToContainer(final Slot newSlot) {
		if (newSlot instanceof AppEngSlot) {
			((AppEngSlot) newSlot).setContainer(this);
		}
		return super.addSlotToContainer(newSlot);
	}

	@Override
	public void detectAndSendChanges() {
		sendCustomName();

		for (final IContainerListener listener : listeners) {
			for (final SyncData sd : syncData.values()) {
				sd.tick(listener);
			}
		}
		super.detectAndSendChanges();
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
		if (Platform.isClient()) {
			return ItemStack.EMPTY;
		}

		boolean hasMETiles = false;
		for (final Object is : inventorySlots) {
			if (is instanceof InternalSlotME) {
				hasMETiles = true;
				break;
			}
		}

		if (hasMETiles && Platform.isClient()) {
			return ItemStack.EMPTY;
		}

		final AppEngSlot clickSlot = (AppEngSlot) inventorySlots.get(idx); // require AE SLots!

		if (clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessible) {
			return ItemStack.EMPTY;
		}
		if (clickSlot != null && clickSlot.getHasStack()) {
			ItemStack tis = clickSlot.getStack();

			if (tis.isEmpty()) {
				return ItemStack.EMPTY;
			}

			final List<Slot> selectedSlots = new ArrayList<Slot>();

			/**
			 * Gather a list of valid destinations.
			 */
			if (clickSlot.isPlayerSide()) {
				tis = shiftStoreItem(tis);

				// target slots in the container...
				for (final Object inventorySlot : inventorySlots) {
					final AppEngSlot cs = (AppEngSlot) inventorySlot;

					if (!(cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof SlotCraftingMatrix)) {
						if (cs.isItemValid(tis)) {
							selectedSlots.add(cs);
						}
					}
				}
			}
			else {
				// target slots in the container...
				for (final Object inventorySlot : inventorySlots) {
					final AppEngSlot cs = (AppEngSlot) inventorySlot;

					if ((cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof SlotCraftingMatrix)) {
						if (cs.isItemValid(tis)) {
							selectedSlots.add(cs);
						}
					}
				}
			}

			/**
			 * Handle Fake Slot Shift clicking.
			 */
			if (selectedSlots.isEmpty() && clickSlot.isPlayerSide()) {
				if (!tis.isEmpty()) {
					// target slots in the container...
					for (final Object inventorySlot : inventorySlots) {
						final AppEngSlot cs = (AppEngSlot) inventorySlot;
						final ItemStack destination = cs.getStack();

						if (!(cs.isPlayerSide()) && cs instanceof SlotFake) {
							if (Platform.itemComparisons().isSameItem(destination, tis)) {
								return ItemStack.EMPTY;
							}
							else if (destination.isEmpty()) {
								cs.putStack(tis.copy());
								cs.onSlotChanged();
								updateSlot(cs);
								return ItemStack.EMPTY;
							}
						}
					}
				}
			}

			if (!tis.isEmpty()) {
				// find partials..
				for (final Slot d : selectedSlots) {
					if (d instanceof SlotDisabled || d instanceof SlotME) {
						continue;
					}

					if (d.isItemValid(tis)) {
						if (d.getHasStack()) {
							final ItemStack t = d.getStack();

							if (Platform.itemComparisons().isSameItem(tis, t)) // t.isItemEqual(tis))
							{
								int maxSize = t.getMaxStackSize();
								if (maxSize > d.getSlotStackLimit()) {
									maxSize = d.getSlotStackLimit();
								}

								int placeAble = maxSize - t.getCount();

								if (tis.getCount() < placeAble) {
									placeAble = tis.getCount();
								}

								t.grow(placeAble);
								tis.shrink(placeAble);

								if (tis.getCount() <= 0) {
									clickSlot.putStack(ItemStack.EMPTY);
									d.onSlotChanged();

									// if ( hasMETiles ) updateClient();

									updateSlot(clickSlot);
									updateSlot(d);
									return null;
								}
								else {
									updateSlot(d);
								}
							}
						}
					}
				}

				// any match..
				for (final Slot d : selectedSlots) {
					if (d instanceof SlotDisabled || d instanceof SlotME) {
						continue;
					}

					if (d.isItemValid(tis)) {
						if (d.getHasStack()) {
							final ItemStack t = d.getStack();

							if (Platform.itemComparisons().isSameItem(t, tis)) {
								int maxSize = t.getMaxStackSize();
								if (d.getSlotStackLimit() < maxSize) {
									maxSize = d.getSlotStackLimit();
								}

								int placeAble = maxSize - t.getCount();

								if (tis.getCount() < placeAble) {
									placeAble = tis.getCount();
								}

								t.grow(placeAble);
								tis.shrink(placeAble);

								if (tis.getCount() <= 0) {
									clickSlot.putStack(ItemStack.EMPTY);
									d.onSlotChanged();

									// if ( worldEntity != null )
									// worldEntity.markDirty();
									// if ( hasMETiles ) updateClient();

									updateSlot(clickSlot);
									updateSlot(d);
									return ItemStack.EMPTY;
								}
								else {
									updateSlot(d);
								}
							}
						}
						else {
							int maxSize = tis.getMaxStackSize();
							if (maxSize > d.getSlotStackLimit()) {
								maxSize = d.getSlotStackLimit();
							}

							final ItemStack tmp = tis.copy();
							if (tmp.getCount() > maxSize) {
								tmp.setCount(maxSize);
							}

							tis.shrink(tmp.getCount());
							d.putStack(tmp);

							if (tis.getCount() <= 0) {
								clickSlot.putStack(ItemStack.EMPTY);
								d.onSlotChanged();

								// if ( worldEntity != null )
								// worldEntity.markDirty();
								// if ( hasMETiles ) updateClient();

								updateSlot(clickSlot);
								updateSlot(d);
								return ItemStack.EMPTY;
							}
							else {
								updateSlot(d);
							}
						}
					}
				}
			}

			clickSlot.putStack(!tis.isEmpty() ? tis.copy() : ItemStack.EMPTY);
		}

		updateSlot(clickSlot);
		return ItemStack.EMPTY;
	}

	/*
	@Override
	public final void updateProgressBar(final int idx, final int value) {
		if (syncData.containsKey(idx)) {
			syncData.get(idx).update((long) value);
		}
	}
	*/
	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		if (isValidContainer()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canDragIntoSlot(final Slot s) {
		return ((AppEngSlot) s).isDraggable();
	}

	@Override
	public abstract void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id);

	@Override
	protected void updateHeld(final EntityPlayerMP p) {
		if (Platform.isServer()) {
			try {
				ModNetworking.instance().sendTo(new PacketInventoryAction(InventoryAction.UPDATE_HAND, 0, AEItemStack.fromItemStack(p.inventory.getItemStack())), p);
			}
			catch (final IOException e) {
			}
		}
	}

	private ItemStack shiftStoreItem(final ItemStack input) {
		if (getPowerSource() == null || getCellInventory() == null) {
			return input;
		}
		final IAEItemStack ais = Platform.poweredInsert(getPowerSource(), getCellInventory(), AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(input), getActionSource());
		if (ais == null) {
			return ItemStack.EMPTY;
		}
		return ais.createItemStack();
	}

	private void updateSlot(final Slot clickSlot) {
		// ???
		detectAndSendChanges();
	}

	private void sendCustomName() {
		if (!sentCustomName) {
			sentCustomName = true;
			if (Platform.isServer()) {
				ICustomNameObject name = null;

				if (obj instanceof ICustomNameObject) {
					name = (ICustomNameObject) obj;
				}

				if (this instanceof ICustomNameObject) {
					name = (ICustomNameObject) this;
				}

				if (name != null) {
					if (name.hasCustomInventoryName()) {
						setCustomName(name.getCustomInventoryName());
					}

					if (getCustomName() != null) {
						try {
							ModNetworking.instance().sendTo(new PacketValueConfig("CustomName", getCustomName()), (EntityPlayerMP) getInventoryPlayer().player);
						}
						catch (final IOException e) {
						}
					}
				}
			}
		}
	}

	@Override
	public void swapSlotContents(final int slotA, final int slotB) {
		final Slot a = getSlot(slotA);
		final Slot b = getSlot(slotB);

		// NPE protection...
		if (a == null || b == null) {
			return;
		}

		final ItemStack isA = a.getStack();
		final ItemStack isB = b.getStack();

		// something to do?
		if (isA.isEmpty() && isB.isEmpty()) {
			return;
		}

		// can take?

		if (!isA.isEmpty() && !a.canTakeStack(getInventoryPlayer().player)) {
			return;
		}

		if (!isB.isEmpty() && !b.canTakeStack(getInventoryPlayer().player)) {
			return;
		}

		// swap valid?

		if (!isB.isEmpty() && !a.isItemValid(isB)) {
			return;
		}

		if (!isA.isEmpty() && !b.isItemValid(isA)) {
			return;
		}

		ItemStack testA = isB.isEmpty() ? ItemStack.EMPTY : isB.copy();
		ItemStack testB = isA.isEmpty() ? ItemStack.EMPTY : isA.copy();

		// can put some back?
		if (!testA.isEmpty() && testA.getCount() > a.getSlotStackLimit()) {
			if (!testB.isEmpty()) {
				return;
			}

			final int totalA = testA.getCount();
			testA.setCount(a.getSlotStackLimit());
			testB = testA.copy();

			testB.setCount(totalA - testA.getCount());
		}

		if (!testB.isEmpty() && testB.getCount() > b.getSlotStackLimit()) {
			if (!testA.isEmpty()) {
				return;
			}

			final int totalB = testB.getCount();
			testB.setCount(b.getSlotStackLimit());
			testA = testB.copy();

			testA.setCount(totalB - testA.getCount());
		}

		a.putStack(testA);
		b.putStack(testB);
	}

	@Override
	public void onUpdate(final String field, final Object oldValue, final Object newValue) {

	}

	@Override
	public boolean isValidForSlot(final Slot s, final ItemStack i) {
		return true;
	}

	@Override
	public IMEInventoryHandler<IAEItemStack> getCellInventory() {
		return cellInv;
	}

	@Override
	public void setCellInventory(final IMEInventoryHandler<IAEItemStack> cellInv) {
		this.cellInv = cellInv;
	}

	@Override
	public String getCustomName() {
		return customName;
	}

	@Override
	public void setCustomName(final String customName) {
		this.customName = customName;
	}

	@Override
	public InventoryPlayer getInventoryPlayer() {
		return inventoryPlayer;
	}

	@Override
	public boolean isValidContainer() {
		return isContainerValid;
	}

	@Override
	public void setValidContainer(final boolean isContainerValid) {
		this.isContainerValid = isContainerValid;
	}

	@Override
	public ContainerOpenContext getOpenContext() {
		return openContext;
	}

	@Override
	public void setOpenContext(final ContainerOpenContext openContext) {
		this.openContext = openContext;
	}

	@Override
	public IEnergySource getPowerSource() {
		return powerSrc;
	}

	@Override
	public void setPowerSource(final IEnergySource powerSrc) {
		this.powerSrc = powerSrc;
	}

	public WCTGuiObject getObject() {
		return obj;
	}
}
