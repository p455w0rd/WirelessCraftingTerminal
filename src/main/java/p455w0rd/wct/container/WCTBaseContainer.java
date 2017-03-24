package p455w0rd.wct.container;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.InventoryAction;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import p455w0rd.wct.api.IWirelessCraftingTermHandler;
import p455w0rd.wct.api.networking.security.WCTIActionHost;
import p455w0rd.wct.api.networking.security.WCTPlayerSource;
import p455w0rd.wct.client.me.InternalSlotME;
import p455w0rd.wct.client.me.SlotME;
import p455w0rd.wct.container.guisync.GuiSync;
import p455w0rd.wct.container.guisync.SyncData;
import p455w0rd.wct.container.slot.AppEngSlot;
import p455w0rd.wct.container.slot.SlotCraftingMatrix;
import p455w0rd.wct.container.slot.SlotCraftingTerm;
import p455w0rd.wct.container.slot.SlotDisabled;
import p455w0rd.wct.container.slot.SlotFake;
import p455w0rd.wct.container.slot.SlotInaccessible;
import p455w0rd.wct.container.slot.SlotPlayerHotBar;
import p455w0rd.wct.container.slot.SlotPlayerInv;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketInventoryAction;
import p455w0rd.wct.sync.packets.PacketPartialItem;
import p455w0rd.wct.sync.packets.PacketValueConfig;
import p455w0rd.wct.util.WCTUtils;

public abstract class WCTBaseContainer extends Container {

	private final InventoryPlayer invPlayer;
	private final BaseActionSource mySrc;
	private final HashSet<Integer> locked = new HashSet<Integer>();
	private final TileEntity tileEntity;
	private final IPart part;
	public final IGuiItemObject obj;
	public final WCTGuiObject obj2;
	private final List<PacketPartialItem> dataChunks = new LinkedList<PacketPartialItem>();
	private final HashMap<Integer, SyncData> syncData = new HashMap<Integer, SyncData>();
	private boolean isContainerValid = true;
	private String customName;
	private ContainerOpenContext openContext;
	private IMEInventoryHandler<IAEItemStack> cellInv;
	private IEnergySource powerSrc;
	private boolean sentCustomName;
	private int ticksSinceCheck = 900;
	private IAEItemStack clientRequestedTargetItem = null;

	public WCTBaseContainer(final InventoryPlayer ip, final TileEntity myTile, final IPart myPart) {
		this(ip, myTile, myPart, null);
	}

	public WCTBaseContainer(final InventoryPlayer ip, final TileEntity myTile, final IPart myPart, final IGuiItemObject gio) {
		invPlayer = ip;
		tileEntity = myTile;
		part = myPart;
		obj = gio;
		EntityPlayer player = ip.player;
		obj2 = getGuiObject(WCTUtils.getWirelessTerm(ip), player, WCTUtils.world(player), (int) player.posX, (int) player.posY, (int) player.posZ);
		mySrc = new WCTPlayerSource(ip.player, getActionHost());
		prepareSync();
	}

	protected WCTGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (it != null) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WCTGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	protected WCTIActionHost getActionHost() {
		if (obj instanceof WCTIActionHost) {
			return (WCTIActionHost) obj;
		}

		if (tileEntity instanceof WCTIActionHost) {
			return (WCTIActionHost) tileEntity;
		}

		if (part instanceof WCTIActionHost) {
			return (WCTIActionHost) part;
		}

		return null;
	}

	private void prepareSync() {
		for (final Field f : this.getClass().getFields()) {
			if (f.isAnnotationPresent(GuiSync.class)) {
				final GuiSync annotation = f.getAnnotation(GuiSync.class);
				if (!syncData.containsKey(annotation.value())) {
					syncData.put(annotation.value(), new SyncData(this, f, annotation));
				}
			}
		}
	}

	public WCTBaseContainer(final InventoryPlayer ip, final Object anchor) {
		invPlayer = ip;
		tileEntity = anchor instanceof TileEntity ? (TileEntity) anchor : null;
		part = anchor instanceof IPart ? (IPart) anchor : null;
		obj = anchor instanceof IGuiItemObject ? (IGuiItemObject) anchor : null;

		EntityPlayer player = ip.player;
		obj2 = getGuiObject(WCTUtils.getWirelessTerm(ip), player, WCTUtils.world(player), (int) player.posX, (int) player.posY, (int) player.posZ);

		if (tileEntity == null && part == null && obj == null) {
			throw new IllegalArgumentException("Must have a valid anchor, instead " + anchor + " in " + ip);
		}

		mySrc = new WCTPlayerSource(ip.player, getActionHost());

		prepareSync();
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
				setTargetStack(AEApi.instance().storage().createItemStack(ItemStack.loadItemStackFromNBT(data)));
			}
		}
		catch (final IOException e) {
		}

		dataChunks.clear();
	}

	public IAEItemStack getTargetStack() {
		return clientRequestedTargetItem;
	}

	public void setTargetStack(final IAEItemStack stack) {
		// client doesn't need to re-send, makes for lower overhead rapid packets.
		if (Platform.isClient()) {
			final ItemStack a = stack == null ? null : stack.getItemStack();
			final ItemStack b = clientRequestedTargetItem == null ? null : clientRequestedTargetItem.getItemStack();

			if (Platform.itemComparisons().isSameItem(a, b)) {
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
					NetworkHandler.instance().sendToServer(ppi);
				}
			}
			catch (final IOException e) {
				return;
			}
		}

		clientRequestedTargetItem = stack == null ? null : stack.copy();
	}

	public BaseActionSource getActionSource() {
		return mySrc;
	}

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

	protected boolean hasAccess(final SecurityPermissions perm, final boolean requirePower) {
		final IGrid grid = obj2.getTargetGrid();
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

	public void lockPlayerInventorySlot(final int idx) {
		locked.add(idx);
	}

	public Object getTarget() {
		if (tileEntity != null) {
			return tileEntity;
		}
		if (part != null) {
			return part;
		}
		if (obj2 != null) {
			return obj2;
		}
		return null;
	}

	public InventoryPlayer getPlayerInv() {
		return getInventoryPlayer();
	}

	public TileEntity getTileEntity() {
		return tileEntity;
	}

	public final void updateFullProgressBar(final int idx, final long value) {
		if (syncData.containsKey(idx)) {
			syncData.get(idx).update(value);
			return;
		}

		updateProgressBar(idx, (int) value);
	}

	public void stringSync(final int idx, final String value) {
		if (syncData.containsKey(idx)) {
			syncData.get(idx).update(value);
		}
	}

	protected void bindPlayerInventory(final InventoryPlayer inventoryPlayer, final int offsetX, final int offsetY) {
		// bind player inventory
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				if (locked.contains(j + i * 9 + 9)) {
					addSlotToContainer(new SlotDisabled(inventoryPlayer, j + i * 9 + 9, 8 + j * 18 + offsetX, offsetY + i * 18));
				}
				else {
					addSlotToContainer(new SlotPlayerInv(inventoryPlayer, j + i * 9 + 9, 8 + j * 18 + offsetX, offsetY + i * 18));
				}
			}
		}

		// bind player hotbar
		for (int i = 0; i < 9; i++) {
			if (locked.contains(i)) {
				addSlotToContainer(new SlotDisabled(inventoryPlayer, i, 8 + i * 18 + offsetX, 58 + offsetY));
			}
			else {
				addSlotToContainer(new SlotPlayerHotBar(inventoryPlayer, i, 8 + i * 18 + offsetX, 58 + offsetY));
			}
		}
	}

	@Override
	protected Slot addSlotToContainer(final Slot newSlot) {
		if (newSlot instanceof AppEngSlot) {
			final AppEngSlot s = (AppEngSlot) newSlot;
			s.setContainer(this);
			return super.addSlotToContainer(newSlot);
		}
		else {
			throw new IllegalArgumentException("Invalid Slot [" + newSlot + "]for AE Container instead of AppEngSlot.");
		}
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
			return null;
		}

		boolean hasMETiles = false;
		for (final Object is : inventorySlots) {
			if (is instanceof InternalSlotME) {
				hasMETiles = true;
				break;
			}
		}

		if (hasMETiles && Platform.isClient()) {
			return null;
		}

		final AppEngSlot clickSlot = (AppEngSlot) inventorySlots.get(idx); // require AE SLots!

		if (clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessible) {
			return null;
		}
		if (clickSlot != null && clickSlot.getHasStack()) {
			ItemStack tis = clickSlot.getStack();

			if (tis == null) {
				return null;
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
				if (tis != null) {
					// target slots in the container...
					for (final Object inventorySlot : inventorySlots) {
						final AppEngSlot cs = (AppEngSlot) inventorySlot;
						final ItemStack destination = cs.getStack();

						if (!(cs.isPlayerSide()) && cs instanceof SlotFake) {
							if (Platform.itemComparisons().isSameItem(destination, tis)) {
								return null;
							}
							else if (destination == null) {
								cs.putStack(tis.copy());
								cs.onSlotChanged();
								updateSlot(cs);
								return null;
							}
						}
					}
				}
			}

			if (tis != null) {
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

								int placeAble = maxSize - t.stackSize;

								if (tis.stackSize < placeAble) {
									placeAble = tis.stackSize;
								}

								t.stackSize += placeAble;
								tis.stackSize -= placeAble;

								if (tis.stackSize <= 0) {
									clickSlot.putStack(null);
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

								int placeAble = maxSize - t.stackSize;

								if (tis.stackSize < placeAble) {
									placeAble = tis.stackSize;
								}

								t.stackSize += placeAble;
								tis.stackSize -= placeAble;

								if (tis.stackSize <= 0) {
									clickSlot.putStack(null);
									d.onSlotChanged();

									// if ( worldEntity != null )
									// worldEntity.markDirty();
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
						else {
							int maxSize = tis.getMaxStackSize();
							if (maxSize > d.getSlotStackLimit()) {
								maxSize = d.getSlotStackLimit();
							}

							final ItemStack tmp = tis.copy();
							if (tmp.stackSize > maxSize) {
								tmp.stackSize = maxSize;
							}

							tis.stackSize -= tmp.stackSize;
							d.putStack(tmp);

							if (tis.stackSize <= 0) {
								clickSlot.putStack(null);
								d.onSlotChanged();

								// if ( worldEntity != null )
								// worldEntity.markDirty();
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

			clickSlot.putStack(tis != null ? tis.copy() : null);
		}

		updateSlot(clickSlot);
		return null;
	}

	@Override
	public final void updateProgressBar(final int idx, final int value) {
		if (syncData.containsKey(idx)) {
			syncData.get(idx).update((long) value);
		}
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		if (isValidContainer()) {
			if (tileEntity instanceof IInventory) {
				return ((IInventory) tileEntity).isUsableByPlayer(entityplayer);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canDragIntoSlot(final Slot s) {
		return ((AppEngSlot) s).isDraggable();
	}

	public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
		if (slot >= 0 && slot < inventorySlots.size()) {
			final Slot s = getSlot(slot);

			if (s instanceof SlotCraftingTerm) {
				switch (action) {
				case CRAFT_SHIFT:
				case CRAFT_ITEM:
				case CRAFT_STACK:
					((SlotCraftingTerm) s).doClick(action, player);
					updateHeld(player);
				default:
				}
			}

			if (s instanceof SlotFake) {
				final ItemStack hand = player.inventory.getItemStack();

				switch (action) {
				case PICKUP_OR_SET_DOWN:

					if (hand == null) {
						s.putStack(null);
					}
					else {
						s.putStack(hand.copy());
					}

					break;
				case PLACE_SINGLE:

					if (hand != null) {
						final ItemStack is = hand.copy();
						is.stackSize = 1;
						s.putStack(is);
					}

					break;
				case SPLIT_OR_PLACE_SINGLE:

					ItemStack is = s.getStack();
					if (is != null) {
						if (hand == null) {
							is.stackSize--;
						}
						else if (hand.isItemEqual(is)) {
							is.stackSize = Math.min(is.getMaxStackSize(), is.stackSize + 1);
						}
						else {
							is = hand.copy();
							is.stackSize = 1;
						}

						s.putStack(is);
					}
					else if (hand != null) {
						is = hand.copy();
						is.stackSize = 1;
						s.putStack(is);
					}

					break;
				case CREATIVE_DUPLICATE:
				case MOVE_REGION:
				case SHIFT_CLICK:
				default:
					break;
				}
			}

			if (action == InventoryAction.MOVE_REGION) {
				final List<Slot> from = new LinkedList<Slot>();

				for (final Object j : inventorySlots) {
					if (j instanceof Slot && j.getClass() == s.getClass()) {
						from.add((Slot) j);
					}
				}

				for (final Slot fr : from) {
					transferStackInSlot(player, fr.slotNumber);
				}
			}

			return;
		}

		// get target item.
		final IAEItemStack slotItem = clientRequestedTargetItem;

		switch (action) {
		case SHIFT_CLICK:
			if (getPowerSource() == null || getCellInventory() == null) {
				return;
			}

			if (slotItem != null) {
				IAEItemStack ais = slotItem.copy();
				ItemStack myItem = ais.getItemStack();

				ais.setStackSize(myItem.getMaxStackSize());

				final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player, EnumFacing.UP);
				myItem.stackSize = (int) ais.getStackSize();
				myItem = adp.simulateAdd(myItem);

				if (myItem != null) {
					ais.setStackSize(ais.getStackSize() - myItem.stackSize);
				}

				ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
				if (ais != null) {
					adp.addItems(ais.getItemStack());
				}
			}
			break;
		case ROLL_DOWN:
			if (getPowerSource() == null || getCellInventory() == null) {
				return;
			}

			final int releaseQty = 1;
			final ItemStack isg = player.inventory.getItemStack();

			if (isg != null && releaseQty > 0) {
				IAEItemStack ais = AEApi.instance().storage().createItemStack(isg);
				ais.setStackSize(1);
				final IAEItemStack extracted = ais.copy();

				ais = Platform.poweredInsert(getPowerSource(), getCellInventory(), ais, getActionSource());
				if (ais == null) {
					final InventoryAdaptor ia = new AdaptorPlayerHand(player);

					final ItemStack fail = ia.removeItems(1, extracted.getItemStack(), null);
					if (fail == null) {
						getCellInventory().extractItems(extracted, Actionable.MODULATE, getActionSource());
					}

					updateHeld(player);
				}
			}

			break;
		case ROLL_UP:
		case PICKUP_SINGLE:
			if (getPowerSource() == null || getCellInventory() == null) {
				return;
			}

			if (slotItem != null) {
				int liftQty = 1;
				final ItemStack item = player.inventory.getItemStack();

				if (item != null) {
					if (item.stackSize >= item.getMaxStackSize()) {
						liftQty = 0;
					}
					if (!Platform.itemComparisons().isSameItem(slotItem.getItemStack(), item)) {
						liftQty = 0;
					}
				}

				if (liftQty > 0) {
					IAEItemStack ais = slotItem.copy();
					ais.setStackSize(1);
					ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
					if (ais != null) {
						final InventoryAdaptor ia = new AdaptorPlayerHand(player);

						final ItemStack fail = ia.addItems(ais.getItemStack());
						if (fail != null) {
							getCellInventory().injectItems(ais, Actionable.MODULATE, getActionSource());
						}

						updateHeld(player);
					}
				}
			}
			break;
		case PICKUP_OR_SET_DOWN:
			if (getPowerSource() == null || getCellInventory() == null) {
				return;
			}

			if (player.inventory.getItemStack() == null) {
				if (slotItem != null) {
					IAEItemStack ais = slotItem.copy();
					ais.setStackSize(ais.getItemStack().getMaxStackSize());
					ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
					if (ais != null) {
						player.inventory.setItemStack(ais.getItemStack());
					}
					else {
						player.inventory.setItemStack(null);
					}
					updateHeld(player);
				}
			}
			else {
				IAEItemStack ais = AEApi.instance().storage().createItemStack(player.inventory.getItemStack());
				ais = Platform.poweredInsert(getPowerSource(), getCellInventory(), ais, getActionSource());
				if (ais != null) {
					player.inventory.setItemStack(ais.getItemStack());
				}
				else {
					player.inventory.setItemStack(null);
				}
				updateHeld(player);
			}

			break;
		case SPLIT_OR_PLACE_SINGLE:
			if (getPowerSource() == null || getCellInventory() == null) {
				return;
			}

			if (player.inventory.getItemStack() == null) {
				if (slotItem != null) {
					IAEItemStack ais = slotItem.copy();
					final long maxSize = ais.getItemStack().getMaxStackSize();
					ais.setStackSize(maxSize);
					ais = getCellInventory().extractItems(ais, Actionable.SIMULATE, getActionSource());

					if (ais != null) {
						final long stackSize = Math.min(maxSize, ais.getStackSize());
						ais.setStackSize((stackSize + 1) >> 1);
						ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
					}

					if (ais != null) {
						player.inventory.setItemStack(ais.getItemStack());
					}
					else {
						player.inventory.setItemStack(null);
					}
					updateHeld(player);
				}
			}
			else {
				IAEItemStack ais = AEApi.instance().storage().createItemStack(player.inventory.getItemStack());
				ais.setStackSize(1);
				ais = Platform.poweredInsert(getPowerSource(), getCellInventory(), ais, getActionSource());
				if (ais == null) {
					final ItemStack is = player.inventory.getItemStack();
					is.stackSize--;
					if (is.stackSize <= 0) {
						player.inventory.setItemStack(null);
					}
					updateHeld(player);
				}
			}

			break;
		case CREATIVE_DUPLICATE:
			if (player.capabilities.isCreativeMode && slotItem != null) {
				final ItemStack is = slotItem.getItemStack();
				is.stackSize = is.getMaxStackSize();
				player.inventory.setItemStack(is);
				updateHeld(player);
			}
			break;
		case MOVE_REGION:

			if (getPowerSource() == null || getCellInventory() == null) {
				return;
			}

			if (slotItem != null) {
				final int playerInv = 9 * 4;
				for (int slotNum = 0; slotNum < playerInv; slotNum++) {
					IAEItemStack ais = slotItem.copy();
					ItemStack myItem = ais.getItemStack();

					ais.setStackSize(myItem.getMaxStackSize());

					final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player, EnumFacing.UP);
					myItem.stackSize = (int) ais.getStackSize();
					myItem = adp.simulateAdd(myItem);

					if (myItem != null) {
						ais.setStackSize(ais.getStackSize() - myItem.stackSize);
					}

					ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
					if (ais != null) {
						adp.addItems(ais.getItemStack());
					}
					else {
						return;
					}
				}
			}

			break;
		default:
			break;
		}
	}

	protected void updateHeld(final EntityPlayerMP p) {
		if (Platform.isServer()) {
			try {
				NetworkHandler.instance().sendTo(new PacketInventoryAction(InventoryAction.UPDATE_HAND, 0, AEItemStack.create(p.inventory.getItemStack())), p);
			}
			catch (final IOException e) {
			}
		}
	}

	private ItemStack shiftStoreItem(final ItemStack input) {
		if (getPowerSource() == null || getCellInventory() == null) {
			return input;
		}
		final IAEItemStack ais = Platform.poweredInsert(getPowerSource(), getCellInventory(), AEApi.instance().storage().createItemStack(input), getActionSource());
		if (ais == null) {
			return null;
		}
		return ais.getItemStack();
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

				if (part instanceof ICustomNameObject) {
					name = (ICustomNameObject) part;
				}

				if (tileEntity instanceof ICustomNameObject) {
					name = (ICustomNameObject) tileEntity;
				}

				if (obj instanceof ICustomNameObject) {
					name = (ICustomNameObject) obj;
				}

				if (this instanceof ICustomNameObject) {
					name = (ICustomNameObject) this;
				}

				if (name != null) {
					if (name.hasCustomName()) {
						setCustomName(name.getCustomName());
					}

					if (getCustomName() != null) {
						try {
							NetworkHandler.instance().sendTo(new PacketValueConfig("CustomName", getCustomName()), (EntityPlayerMP) getInventoryPlayer().player);
						}
						catch (final IOException e) {
						}
					}
				}
			}
		}
	}

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
		if (isA == null && isB == null) {
			return;
		}

		// can take?

		if (isA != null && !a.canTakeStack(getInventoryPlayer().player)) {
			return;
		}

		if (isB != null && !b.canTakeStack(getInventoryPlayer().player)) {
			return;
		}

		// swap valid?

		if (isB != null && !a.isItemValid(isB)) {
			return;
		}

		if (isA != null && !b.isItemValid(isA)) {
			return;
		}

		ItemStack testA = isB == null ? null : isB.copy();
		ItemStack testB = isA == null ? null : isA.copy();

		// can put some back?
		if (testA != null && testA.stackSize > a.getSlotStackLimit()) {
			if (testB != null) {
				return;
			}

			final int totalA = testA.stackSize;
			testA.stackSize = a.getSlotStackLimit();
			testB = testA.copy();

			testB.stackSize = totalA - testA.stackSize;
		}

		if (testB != null && testB.stackSize > b.getSlotStackLimit()) {
			if (testA != null) {
				return;
			}

			final int totalB = testB.stackSize;
			testB.stackSize = b.getSlotStackLimit();
			testA = testB.copy();

			testA.stackSize = totalB - testA.stackSize;
		}

		a.putStack(testA);
		b.putStack(testB);
	}

	public void onUpdate(final String field, final Object oldValue, final Object newValue) {

	}

	public void onSlotChange(final Slot s) {

	}

	public boolean isValidForSlot(final Slot s, final ItemStack i) {
		return true;
	}

	public IMEInventoryHandler<IAEItemStack> getCellInventory() {
		return cellInv;
	}

	public void setCellInventory(final IMEInventoryHandler<IAEItemStack> cellInv) {
		this.cellInv = cellInv;
	}

	public String getCustomName() {
		return customName;
	}

	public void setCustomName(final String customName) {
		this.customName = customName;
	}

	public InventoryPlayer getInventoryPlayer() {
		return invPlayer;
	}

	public boolean isValidContainer() {
		return isContainerValid;
	}

	public void setValidContainer(final boolean isContainerValid) {
		this.isContainerValid = isContainerValid;
	}

	public ContainerOpenContext getOpenContext() {
		return openContext;
	}

	public void setOpenContext(final ContainerOpenContext openContext) {
		this.openContext = openContext;
	}

	public IEnergySource getPowerSource() {
		return powerSrc;
	}

	public void setPowerSource(final IEnergySource powerSrc) {
		this.powerSrc = powerSrc;
	}
}
