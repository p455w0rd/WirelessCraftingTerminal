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

import javax.annotation.Nonnull;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.ContainerNull;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.InventoryAction;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import p455w0rd.wct.api.IWirelessCraftingTermHandler;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.networking.security.WCTIActionHost;
import p455w0rd.wct.api.networking.security.WCTPlayerSource;
import p455w0rd.wct.client.me.InternalSlotME;
import p455w0rd.wct.client.me.SlotME;
import p455w0rd.wct.container.guisync.GuiSync;
import p455w0rd.wct.container.guisync.SyncData;
import p455w0rd.wct.container.slot.AppEngCraftingSlot;
import p455w0rd.wct.container.slot.AppEngSlot;
import p455w0rd.wct.container.slot.NullSlot;
import p455w0rd.wct.container.slot.SlotArmor;
import p455w0rd.wct.container.slot.SlotBooster;
import p455w0rd.wct.container.slot.SlotCraftingMatrix;
import p455w0rd.wct.container.slot.SlotCraftingTerm;
import p455w0rd.wct.container.slot.SlotDisabled;
import p455w0rd.wct.container.slot.SlotFake;
import p455w0rd.wct.container.slot.SlotInaccessible;
import p455w0rd.wct.container.slot.SlotMagnet;
import p455w0rd.wct.container.slot.SlotPlayerHotBar;
import p455w0rd.wct.container.slot.SlotPlayerInv;
import p455w0rd.wct.container.slot.SlotTrash;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.inventory.WCTInventoryBooster;
import p455w0rd.wct.inventory.WCTInventoryCrafting;
import p455w0rd.wct.inventory.WCTInventoryMagnet;
import p455w0rd.wct.inventory.WCTInventoryTrash;
import p455w0rd.wct.items.ItemInfinityBooster;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketInventoryAction;
import p455w0rd.wct.sync.packets.PacketMEInventoryUpdate;
import p455w0rd.wct.sync.packets.PacketPartialItem;
import p455w0rd.wct.sync.packets.PacketValueConfig;
import p455w0rd.wct.util.WCTUtils;

public class ContainerWCT extends Container implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEItemStack>, IAEAppEngInventory, IContainerCraftingPacket {

	private final ItemStack containerstack;
	public WCTInventoryCrafting craftingGrid;
	public final WCTInventoryBooster boosterInventory;
	public final WCTInventoryMagnet magnetInventory;
	public final WCTInventoryTrash trashInventory;
	public final InventoryPlayer inventoryPlayer;
	public ItemStack[] craftMatrixInventory;
	public ItemStack craftItem;
	private World worldObj;
	private EntityPlayer player;
	private static final int HOTBAR_START = 1, HOTBAR_END = HOTBAR_START + 8, INV_START = HOTBAR_END + 1,
			INV_END = INV_START + 26, ARMOR_START = INV_END + 1, ARMOR_END = ARMOR_START + 3,
			CRAFT_GRID_START = ARMOR_END + 1, CRAFT_GRID_END = CRAFT_GRID_START + 8, CRAFT_RESULT = CRAFT_GRID_END + 1,
			BOOSTER_INDEX = 0, MAGNET_INDEX = CRAFT_RESULT + 1;
	public static int CRAFTING_SLOT_X_POS = 80, CRAFTING_SLOT_Y_POS = 83;
	private SlotBooster boosterSlot;
	private SlotMagnet magnetSlot;
	private NullSlot nullSlot;
	private Slot[] hotbarSlot;
	private Slot[] inventorySlot;
	private SlotArmor[] armorSlot;
	private SlotCraftingMatrix[] craftMatrixSlot;
	private SlotCraftingTerm craftingSlot;
	public SlotTrash trashSlot;
	private int firstCraftingSlotNumber = -1, lastCraftingSlotNumber = -1;

	private final WCTGuiObject obj;
	private boolean isContainerValid = true;
	private double powerMultiplier = 0.5;
	private final IPortableCell civ;
	private int ticks = 0;
	private final IMEMonitor<IAEItemStack> monitor;
	private final IItemList<IAEItemStack> items = AEApi.instance().storage().createItemList();
	private final IConfigManager clientCM;
	private IConfigManager serverCM;
	private IEnergySource powerSrc;
	@GuiSync(98)
	public static boolean hasPower = false;
	private IConfigManagerHost gui;
	private final BaseActionSource mySrc;
	private final HashMap<Integer, SyncData> syncData = new HashMap<Integer, SyncData>();
	private final List<PacketPartialItem> dataChunks = new LinkedList<PacketPartialItem>();
	private IAEItemStack clientRequestedTargetItem = null;
	private int ticksSinceCheck = 900;
	private String customName;
	private boolean sentCustomName;
	private ContainerOpenContext openContext;
	private final AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
	private final HashSet<Integer> locked = new HashSet<Integer>();
	private IWirelessCraftingTerminalItem thisItem;
	private IGridNode networkNode;
	private IMEInventoryHandler<IAEItemStack> cellInv;

	/**
	 * Constructor for our custom container
	 *
	 * @author p455w0rd
	 */
	public ContainerWCT(EntityPlayer player, InventoryPlayer inventoryPlayer) {

		clientCM = new ConfigManager(this);
		clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
		clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
		clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
		mySrc = new WCTPlayerSource(inventoryPlayer.player, getActionHost());
		customName = "WCTContainer";

		boosterInventory = new WCTInventoryBooster(WCTUtils.getWirelessTerm(inventoryPlayer));
		magnetInventory = new WCTInventoryMagnet(WCTUtils.getWirelessTerm(inventoryPlayer));
		trashInventory = new WCTInventoryTrash(WCTUtils.getWirelessTerm(inventoryPlayer));
		containerstack = WCTUtils.getWirelessTerm(inventoryPlayer);
		thisItem = (IWirelessCraftingTerminalItem) containerstack.getItem();
		worldObj = player.worldObj;
		craftingGrid = new WCTInventoryCrafting(this, 3, 3, containerstack);
		this.inventoryPlayer = inventoryPlayer;
		this.player = player;
		craftMatrixInventory = new ItemStack[9];
		hotbarSlot = new Slot[9];
		inventorySlot = new Slot[27];
		armorSlot = new SlotArmor[4];
		craftMatrixSlot = new SlotCraftingMatrix[9];

		obj = getGuiObject(containerstack, player, worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
		civ = obj;
		final IGridNode node = ((IGridHost) obj).getGridNode(AEPartLocation.INTERNAL);
		networkNode = node;

		prepareSync();

		if (Platform.isServer()) {
			serverCM = civ.getConfigManager();
			monitor = civ.getItemInventory();
			if (monitor != null) {
				monitor.addListener(this, null);
				setCellInventory(monitor);
				if (civ instanceof IPortableCell) {
					setPowerSource(civ);
				}
			}
			else {
				setValidContainer(false);
			}
		}
		else {
			monitor = null;
		}

		if (ModConfig.WCT_BOOSTER_ENABLED) {
			boosterSlot = new SlotBooster(boosterInventory, 0, 134, -20);
			addSlotToContainer(boosterSlot);
		}
		else {
			nullSlot = new NullSlot();
			addSlotToContainer(nullSlot);
		}

		// Add hotbar slots
		for (int i = 0; i < 9; ++i) {
			hotbarSlot[i] = new SlotPlayerHotBar(this.inventoryPlayer, i, i * 18 + 8, 58);
			addSlotToContainer(hotbarSlot[i]);
		}

		int k = 0;

		// Add player inventory slots
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				inventorySlot[k] = new SlotPlayerInv(this.inventoryPlayer, j + i * 9 + 9, j * 18 + 8, 0 + i * 18);
				addSlotToContainer(inventorySlot[k]);
				k++;
			}
		}

		// Add armor slots
		for (int i = 0; i < 4; ++i) {
			armorSlot[i] = new SlotArmor(this.player, this.inventoryPlayer, 39 - i, (int) 8.5, (i * 18) - 76, EntityEquipmentSlot.values()[6 - (i + 2)]);
			addSlotToContainer(armorSlot[i]);
		}
		k = 0;
		// Add crafting grid slots
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				craftMatrixSlot[k] = new SlotCraftingMatrix(this, craftingGrid, j + i * 3, 80 + j * 18, (i * 18) - 76);
				addSlotToContainer(craftMatrixSlot[k]);
				if (k == 0) {
					firstCraftingSlotNumber = craftMatrixSlot[k].slotNumber;
				}
				k++;
			}
		}
		lastCraftingSlotNumber = craftMatrixSlot[8].slotNumber;

		craftingSlot = new SlotCraftingTerm(getPlayerInv().player, mySrc, getPowerSource(), obj, craftingGrid, craftingGrid, output, 174, -58, this);
		// Add crafting result slot
		addSlotToContainer(craftingSlot);

		magnetSlot = new SlotMagnet(magnetInventory, 152, -20);
		addSlotToContainer(magnetSlot);

		trashSlot = new SlotTrash(trashInventory, 80, -20, player);
		trashSlot.setContainer(this);
		addSlotToContainer(trashSlot);

		updateCraftingMatrix();

		onCraftMatrixChanged(craftingGrid);
		thisItem.checkForBooster(containerstack);
	}

	public SlotTrash getTrashSlot() {
		return trashSlot;
	}

	public void setCellInventory(final IMEInventoryHandler<IAEItemStack> cellInv) {
		this.cellInv = cellInv;
	}

	public void onSlotChange(final Slot s) {

	}

	@Override
	protected Slot addSlotToContainer(final Slot newSlot) {
		if (newSlot instanceof AppEngSlot) {
			final AppEngSlot s = (AppEngSlot) newSlot;
			s.setContainer(this);
			return super.addSlotToContainer(newSlot);
		}
		else {
			throw new IllegalArgumentException("Invalid Slot [" + newSlot + "] for WCT Container instead of AppEngSlot.");
		}
	}

	@Override
	public boolean canDragIntoSlot(final Slot s) {
		return ((AppEngSlot) s).isDraggable();
	}

	@Override
	public void onCraftMatrixChanged(final IInventory iinv) {
		final ContainerNull cn = new ContainerNull();
		final InventoryCrafting ic = new InventoryCrafting(cn, 3, 3);

		for (int x = 0; x < 9; x++) {
			ic.setInventorySlotContents(x, craftMatrixSlot[x].getStack());
		}

		craftingSlot.putStack(CraftingManager.getInstance().findMatchingRecipe(ic, getPlayerInv().player.worldObj));
		writeToNBT("crafting");

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
				return;
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

	public IMEInventoryHandler<IAEItemStack> getCellInventory() {
		return cellInv;
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

	private void updateCraftingMatrix() {
		if (!containerstack.hasTagCompound()) {
			containerstack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound stack = containerstack.getTagCompound();
		readMatrixNBT(stack);
		for (int i = 0; i < 9; i++) {
			craftingGrid.setInventorySlotContents(i, craftMatrixInventory[i]);
		}
	}

	@Override
	public void saveChanges() {
		// :P
	}

	@Override
	public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
		// <3
	}

	@Override
	public IGridNode getNetworkNode() {
		return networkNode;
	}

	@Override
	public boolean useRealItems() {
		return true;
	}

	@Override
	public IInventory getInventoryByName(final String name) {
		if (name.equals("player") || name.equals("container.inventory")) {
			return getInventoryPlayer();
		}
		if (name.equals("crafting")) {
			return craftingGrid;
		}
		return null;
	}

	@Override
	public ItemStack[] getViewCells() {
		return null;
	}

	private WCTGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (it != null) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WCTGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	@Override
	public void detectAndSendChanges() {

		sendCustomName();

		//if (Platform.isSameItem(this.civ.getItemStack(), this.getPlayerInv().getCurrentItem())) {
		//	this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, this.civ.getItemStack());
		//}
		//else {
		//this.setValidContainer(false);
		//}

		// drain 1 ae t
		ticks++;
		if (ticks > 10) {
			if (!isBoosterInstalled() || !ModConfig.WCT_BOOSTER_ENABLED) {
				civ.extractAEPower(getPowerMultiplier() * ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
			}
			else {
				civ.extractAEPower((int) (0.5 * ticks), Actionable.MODULATE, PowerMultiplier.CONFIG);
			}
			ticks = 0;
		}

		if (Platform.isServer()) {
			if (monitor != civ.getItemInventory()) {
				setValidContainer(false);
			}

			for (final Settings set : serverCM.getSettings()) {
				final Enum<?> sideLocal = serverCM.getSetting(set);
				final Enum<?> sideRemote = clientCM.getSetting(set);

				if (sideLocal != sideRemote) {
					clientCM.putSetting(set, sideLocal);
					for (final IContainerListener crafter : listeners) {
						if (crafter instanceof EntityPlayer) {
							try {
								NetworkHandler.instance().sendTo(new PacketValueConfig(set.name(), sideLocal.name()), (EntityPlayerMP) crafter);
							}
							catch (final IOException e) {
							}
						}
					}
				}
			}

			if (!items.isEmpty()) {
				try {
					final IItemList<IAEItemStack> monitorCache = monitor.getStorageList();

					final PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();

					for (final IAEItemStack is : items) {
						final IAEItemStack send = monitorCache.findPrecise(is);
						if (send == null) {
							is.setStackSize(0);
							piu.appendItem(is);
						}
						else {
							piu.appendItem(send);
						}
					}

					if (!piu.isEmpty()) {
						items.resetStatus();

						for (final IContainerListener c : listeners) {
							if (c instanceof EntityPlayer) {
								NetworkHandler.instance().sendTo(piu, (EntityPlayerMP) c);
							}
						}
					}
				}
				catch (final IOException e) {
				}
			}

			sendCustomName();
			if (Platform.isServer()) {
				for (final IContainerListener crafter : listeners) {
					for (final SyncData sd : syncData.values()) {
						sd.tick(crafter);
					}
				}
			}
		}
		//if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
		if (!isInRange()) {
			if (!isBoosterInstalled() || !ModConfig.WCT_BOOSTER_ENABLED) {
				if (isValidContainer()) {
					getPlayerInv().player.addChatMessage(PlayerMessages.OutOfRange.get());
				}
				setValidContainer(false);
			}
			if (!networkIsPowered()) {
				if (isValidContainer()) {
					getPlayerInv().player.addChatMessage(new TextComponentString("No Power"));
				}
				setValidContainer(false);
			}
		}
		else if (!hasAccess(SecurityPermissions.CRAFT, true) || !hasAccess(SecurityPermissions.EXTRACT, true) || !hasAccess(SecurityPermissions.INJECT, true)) {
			if (isValidContainer()) {
				getPlayerInv().player.addChatMessage(PlayerMessages.CommunicationError.get());
			}
			setValidContainer(false);
		}
		else {
			setPowerMultiplier(AEConfig.instance().wireless_getDrainRate(obj.getRange()));
		}
		//}
		super.detectAndSendChanges();
	}

	public boolean isBoosterInstalled() {
		Slot slot = getSlotFromInventory(boosterInventory, BOOSTER_INDEX);
		if (slot == null) {
			return false;
		}
		boolean hasStack = getSlotFromInventory(boosterInventory, BOOSTER_INDEX).getHasStack();
		if (hasStack) {
			Item boosterSlotContents = getSlotFromInventory(boosterInventory, BOOSTER_INDEX).getStack().getItem();
			if (boosterSlotContents instanceof ItemInfinityBooster) {
				return true;
			}
		}
		return false;
	}

	public boolean isMagnetInstalled() {
		Slot slot = getSlotFromInventory(magnetInventory, MAGNET_INDEX);
		if (slot == null) {
			return false;
		}
		boolean hasStack = getSlotFromInventory(magnetInventory, MAGNET_INDEX).getHasStack();
		if (hasStack) {
			Item magnetSlotContents = getSlotFromInventory(magnetInventory, MAGNET_INDEX).getStack().getItem();
			//if (magnetSlotContents instanceof ItemMagnet) {
			//	return true;
			//}
		}
		return false;
	}

	@Override
	public BaseActionSource getActionSource() {
		return mySrc;
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		if (isValidContainer()) {
			return true;
		}
		return false;
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

	public void setCustomName(final String customName) {
		this.customName = customName;
	}

	public String getCustomName() {
		return customName;
	}

	public void stringSync(final int idx, final String value) {
		if (syncData.containsKey(idx)) {
			syncData.get(idx).update(value);
		}
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

	protected boolean isInRange() {
		//return obj.rangeCheck(ModConfig.WCT_BOOSTER_ENABLED && isBoosterInstalled());
		return obj.rangeCheck();
	}

	protected boolean networkIsPowered() {
		final WCTIActionHost host = getActionHost();
		if (host != null) {
			final IGrid grid = obj.getTargetGrid();
			if (grid != null) {
				final IEnergyGrid eg = grid.getCache(IEnergyGrid.class);
				if (eg.isNetworkPowered()) {
					return true;
				}
			}
		}
		return false;
	}

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

	/*
	protected boolean hasAccess(final SecurityPermissions perm, final boolean requirePower) {
		final WCTIActionHost host = this.getActionHost();
	
		if (host != null) {
			final IGridNode gn = host.getActionableNode();
			if (gn != null) {
				final IGrid g = gn.getGrid();
				if (g != null) {
					if (requirePower) {
						final IEnergyGrid eg = g.getCache(IEnergyGrid.class);
						if (!eg.isNetworkPowered() && Platform.isServer()) {
							return false;
						}
					}
	
					final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
					if (sg.hasPermission(this.player, perm) && Platform.isServer()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	*/
	public void lockPlayerInventorySlot(final int idx) {
		locked.add(idx);
	}

	public boolean isValidForSlot(final Slot s, final ItemStack i) {
		return true;
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
		// client doesn't need to re-send, makes for lower overhead rapid
		// packets.
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

	private void prepareSync() {
		for (final Field f : this.getClass().getFields()) {
			if (f.isAnnotationPresent(GuiSync.class)) {
				final GuiSync annotation = f.getAnnotation(GuiSync.class);
				if (syncData.containsKey(annotation.value())) {
				}
				else {
					syncData.put(annotation.value(), new SyncData(this, f, annotation));
				}
			}
		}
	}

	protected WCTIActionHost getActionHost() {
		if (obj instanceof WCTIActionHost) {
			return obj;
		}
		return null;
	}

	@Override
	public IConfigManager getConfigManager() {
		if (Platform.isServer()) {
			return serverCM;
		}
		return clientCM;
	}

	@Override
	public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
		if (getGui() != null) {
			getGui().updateSetting(manager, settingName, newValue);
		}
	}

	private IConfigManagerHost getGui() {
		return gui;
	}

	public void setGui(@Nonnull final IConfigManagerHost gui) {
		this.gui = gui;
	}

	@Override
	public void onListUpdate() {
		for (final IContainerListener c : listeners) {
			queueInventory(c);
		}
	}

	@Override
	public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final BaseActionSource source) {
		for (final IAEItemStack is : change) {
			items.add(is);
		}
	}

	@Override
	public boolean isValid(final Object verificationToken) {
		return true;
	}

	@Override
	public void onContainerClosed(final EntityPlayer player) {
		super.onContainerClosed(player);
		if (monitor != null) {
			monitor.removeListener(this);
		}
	}

	@Override
	public void addListener(final IContainerListener c) {
		super.addListener(c);

		queueInventory(c);
	}

	private void queueInventory(final IContainerListener c) {
		if (Platform.isServer() && c instanceof EntityPlayerMP && monitor != null) {
			try {
				PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();
				final IItemList<IAEItemStack> monitorCache = monitor.getStorageList();

				for (final IAEItemStack send : monitorCache) {
					try {
						piu.appendItem(send);
					}
					catch (final BufferOverflowException boe) {
						NetworkHandler.instance().sendTo(piu, (EntityPlayerMP) c);

						piu = new PacketMEInventoryUpdate();
						piu.appendItem(send);
					}
				}
				if (piu != null && c != null) {
					NetworkHandler.instance().sendTo(piu, (EntityPlayerMP) c);
				}
			}
			catch (final IOException e) {
			}
		}
	}

	@Override
	public void removeListener(final IContainerListener c) {
		super.removeListener(c);

		if (listeners.isEmpty() && monitor != null) {
			monitor.removeListener(this);
		}
	}

	public void onUpdate(final String field, final Object oldValue, final Object newValue) {
		// =]
	}

	public IEnergySource getPowerSource() {
		return powerSrc;
	}

	public boolean isPowered() {
		double pwr = thisItem.getAECurrentPower(containerstack);
		return (pwr > 0.0);
	}

	public void setPowerSource(final IEnergySource powerSrc) {
		this.powerSrc = powerSrc;
	}

	private double getPowerMultiplier() {
		return powerMultiplier;
	}

	void setPowerMultiplier(final double powerMultiplier) {
		this.powerMultiplier = powerMultiplier;
	}

	public InventoryPlayer getPlayerInv() {
		return getInventoryPlayer();
	}

	public InventoryPlayer getInventoryPlayer() {
		return inventoryPlayer;
	}

	public boolean isValidContainer() {
		return isContainerValid;
	}

	public void setValidContainer(final boolean isContainerValid) {
		this.isContainerValid = isContainerValid;
	}

	public Object getTarget() {
		if (obj != null) {
			return obj;
		}
		return null;
	}

	public ContainerOpenContext getOpenContext() {
		return openContext;
	}

	public void setOpenContext(final ContainerOpenContext openContext) {
		this.openContext = openContext;
	}

	/**
	 * Fetches crafting matrix ItemStacks
	 *
	 * @param nbtTagCompound
	 * @author p455w0rd
	 */
	private void readMatrixNBT(NBTTagCompound nbtTagCompound) {
		// Read in the ItemStacks in the inventory from NBT
		NBTTagList tagList = nbtTagCompound.getTagList("CraftingMatrix", 10);
		craftMatrixInventory = new ItemStack[9];
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
			int slot = tagCompound.getByte("Slot");
			if (slot >= 0 && slot < craftMatrixInventory.length) {
				craftMatrixInventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
			}
		}
	}

	/**
	 * Initiates external NBT saving methods that store inventory contents
	 */
	public void writeToNBT(String which) {
		if (!containerstack.hasTagCompound()) {
			containerstack.setTagCompound(new NBTTagCompound());
		}
		switch (which) {
		case "booster":
			boosterInventory.writeNBT(containerstack.getTagCompound());
			break;
		case "crafting":
			craftingGrid.writeNBT(containerstack.getTagCompound());
			break;
		case "magnet":
			magnetInventory.writeNBT(containerstack.getTagCompound());
			break;
		case "trash":
			trashInventory.writeNBT(containerstack.getTagCompound());
			break;
		case "all":
		default:
			boosterInventory.writeNBT(containerstack.getTagCompound());
			craftingGrid.writeNBT(containerstack.getTagCompound());
			magnetInventory.writeNBT(containerstack.getTagCompound());
			trashInventory.writeNBT(containerstack.getTagCompound());
			break;
		}
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
		final AppEngSlot clickSlot = (AppEngSlot) inventorySlots.get(idx); // require AE SLots!
		ItemStack tis = clickSlot.getStack();
		if (tis == null) {
			return null;
		}
		// Try to place armor in armor slot/booster in booster lost first
		if (isInInventory(idx) || isInHotbar(idx)) {
			if (tis.getItem() instanceof ItemArmor) {
				int type = ((ItemArmor) tis.getItem()).armorType.getIndex();
				if (mergeItemStack(tis, ARMOR_START + type, ARMOR_START + type + 1, false)) {
					clickSlot.clearStack();
					return null;
				}
			}
			else if (tis.getItem() instanceof ItemInfinityBooster) {
				if (mergeItemStack(tis, BOOSTER_INDEX, BOOSTER_INDEX + 1, false)) {
					clickSlot.clearStack();
					return null;
				}
			}
			else {
				if (tis.getItem() instanceof ItemMagnet) {
					if (mergeItemStack(tis, MAGNET_INDEX, MAGNET_INDEX + 1, false)) {
						clickSlot.clearStack();
						return null;
					}
				}
			}
		}

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

		if (clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessible) {
			return null;
		}
		if (clickSlot != null && clickSlot.getHasStack()) {

			final List<Slot> selectedSlots = new ArrayList<Slot>();

			/**
			 * Gather a list of valid destinations.
			 */
			if (clickSlot.isPlayerSide()) {
				tis = shiftStoreItem(tis);

				// target slots in the container...
				for (final Object inventorySlot : inventorySlots) {
					final AppEngSlot cs = (AppEngSlot) inventorySlot;

					if (!(cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof AppEngCraftingSlot)) {
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

					if ((cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof AppEngCraftingSlot)) {
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

	@SuppressWarnings("unused")
	private boolean isCraftMatrixSlot(AppEngSlot cs) {
		return (cs.getSlotIndex() >= firstCraftingSlotNumber) && !(cs.getSlotIndex() <= lastCraftingSlotNumber);
	}

	private void updateSlot(final Slot clickSlot) {
		detectAndSendChanges();
	}

	private ItemStack shiftStoreItem(final ItemStack input) {
		if (getPowerSource() == null || civ == null) {
			return input;
		}
		final IAEItemStack ais = Platform.poweredInsert(getPowerSource(), civ, AEApi.instance().storage().createItemStack(input), getActionSource());
		if (ais == null) {
			return null;
		}
		return ais.getItemStack();
	}

	private boolean isInHotbar(@Nonnull int index) {
		return (index >= HOTBAR_START && index <= HOTBAR_END);
	}

	private boolean isInInventory(@Nonnull int index) {
		return (index >= INV_START && index <= INV_END);
	}

	@SuppressWarnings("unused")
	private boolean isInArmorSlot(@Nonnull int index) {
		return (index >= ARMOR_START && index <= ARMOR_END);
	}

	@SuppressWarnings("unused")
	private boolean isInBoosterSlot(@Nonnull int index) {
		return (index == BOOSTER_INDEX);
	}

	@SuppressWarnings("unused")
	private boolean isCraftResult(@Nonnull int index) {
		return (index == CRAFT_RESULT);
	}

	@SuppressWarnings("unused")
	private boolean isInCraftMatrix(@Nonnull int index) {
		return (index >= CRAFT_GRID_START && index <= CRAFT_GRID_END);
	}

	@SuppressWarnings("unused")
	private boolean notArmorOrBooster(ItemStack is) {
		if ((is.getItem() instanceof ItemInfinityBooster) || (is.getItem() instanceof ItemArmor)) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		try {
			if (slot >= 0 && getSlot(slot) != null && getSlot(slot).getStack() == WCTUtils.getWirelessTerm(player.inventory)) {
				return null;
			}
			return super.slotClick(slot, dragType, clickTypeIn, player);
		}
		catch (IndexOutOfBoundsException e) {
			//When clicking super fast, for some reason, MC tried to access this inv size (max index + 1)
		}
		return null;
	}

	public final void updateFullProgressBar(final int idx, final long value) {
		if (syncData.containsKey(idx)) {
			syncData.get(idx).update(value);
			return;
		}

		updateProgressBar(idx, (int) value);
	}

	/**
	 * Handles shift-clicking of items whose maxStackSize is 1
	 */

	@Override
	protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean backwards) {
		boolean flag1 = false;
		int k = (backwards ? end - 1 : start);
		Slot slot;
		ItemStack itemstack1;

		if (stack.isStackable()) {
			while (stack.stackSize > 0 && (!backwards && k < end || backwards && k >= start)) {
				slot = inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (!slot.isItemValid(stack)) {
					k += (backwards ? -1 : 1);
					continue;
				}

				if (itemstack1 != null && itemstack1.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, itemstack1)) {
					int l = itemstack1.stackSize + stack.stackSize;

					if (l <= stack.getMaxStackSize() && l <= slot.getSlotStackLimit()) {
						stack.stackSize = 0;
						itemstack1.stackSize = l;
						boosterInventory.markDirty();
						flag1 = true;
					}
					else if (itemstack1.stackSize < stack.getMaxStackSize() && l < slot.getSlotStackLimit()) {
						stack.stackSize -= stack.getMaxStackSize() - itemstack1.stackSize;
						itemstack1.stackSize = stack.getMaxStackSize();
						boosterInventory.markDirty();
						flag1 = true;
					}
				}

				k += (backwards ? -1 : 1);
			}
		}
		if (stack.stackSize > 0) {
			k = (backwards ? end - 1 : start);
			while (!backwards && k < end || backwards && k >= start) {
				slot = inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (!slot.isItemValid(stack)) {
					k += (backwards ? -1 : 1);
					continue;
				}

				if (itemstack1 == null) {
					int l = stack.stackSize;
					if (l <= slot.getSlotStackLimit()) {
						slot.putStack(stack.copy());
						stack.stackSize = 0;
						boosterInventory.markDirty();
						flag1 = true;
						break;
					}
					else {
						putStackInSlot(k, new ItemStack(stack.getItem(), slot.getSlotStackLimit(), stack.getItemDamage()));
						stack.stackSize -= slot.getSlotStackLimit();
						boosterInventory.markDirty();
						flag1 = true;
					}
				}

				k += (backwards ? -1 : 1);
			}
		}

		return flag1;
	}

}
