package net.p455w0rd.wirelesscraftingterminal.common.container;

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

import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
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
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.container.ContainerNull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
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
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.items.ItemInfinityBooster;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.WCTIActionHost;
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.WCTPlayerSource;
import net.p455w0rd.wirelesscraftingterminal.common.container.guisync.GuiSync;
import net.p455w0rd.wirelesscraftingterminal.common.container.guisync.SyncData;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.AppEngCraftingSlot;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.AppEngSlot;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.NullSlot;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotArmor;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotBooster;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotCraftingMatrix;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotCraftingTerm;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotDisabled;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotFake;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotInaccessible;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotPlayerHotBar;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotPlayerInv;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotTrash;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryBooster;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryCrafting;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryTrash;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketInventoryAction;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketMEInventoryUpdate;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketPartialItem;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketValueConfig;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;

public class ContainerWirelessCraftingTerminal extends Container implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEItemStack>, IAEAppEngInventory, IContainerCraftingPacket {

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

	private final WirelessTerminalGuiObject obj;
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
	public ContainerWirelessCraftingTerminal(EntityPlayer player, InventoryPlayer inventoryPlayer) {

		this.clientCM = new ConfigManager(this);
		this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
		this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
		this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
		this.mySrc = new WCTPlayerSource(inventoryPlayer.player, this.getActionHost());
		this.customName = "WCTContainer";

		this.boosterInventory = new WCTInventoryBooster(RandomUtils.getWirelessTerm(inventoryPlayer));
		this.magnetInventory = new WCTInventoryMagnet(RandomUtils.getWirelessTerm(inventoryPlayer));
		this.trashInventory = new WCTInventoryTrash(RandomUtils.getWirelessTerm(inventoryPlayer));
		this.containerstack = RandomUtils.getWirelessTerm(inventoryPlayer);
		this.thisItem = (IWirelessCraftingTerminalItem) this.containerstack.getItem();
		this.worldObj = player.worldObj;
		this.craftingGrid = new WCTInventoryCrafting(this, 3, 3, containerstack);
		this.inventoryPlayer = inventoryPlayer;
		this.player = player;
		craftMatrixInventory = new ItemStack[9];
		hotbarSlot = new Slot[9];
		inventorySlot = new Slot[27];
		armorSlot = new SlotArmor[4];
		craftMatrixSlot = new SlotCraftingMatrix[9];

		this.obj = getGuiObject(containerstack, player, worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
		this.civ = (IPortableCell) this.obj;
		//final IGridNode node = ((IGridNode) ((IGrid) this.obj.getTargetGrid()).getPivot()).getMachine().getGridNode(ForgeDirection.UNKNOWN);
		final IGridNode node = this.obj.getActionableNode(true);
		this.networkNode = node;

		this.prepareSync();

		if (Platform.isServer()) {
			this.serverCM = civ.getConfigManager();
			this.monitor = civ.getItemInventory();
			if (this.monitor != null) {
				this.monitor.addListener(this, null);
				setCellInventory(this.monitor);
				if (civ instanceof IPortableCell) {
					setPowerSource((IEnergySource) this.civ);
				}
			}
			else {
				this.setValidContainer(false);
			}
		}
		else {
			this.monitor = null;
		}

		if (Reference.WCT_BOOSTER_ENABLED) {
			boosterSlot = new SlotBooster(this.boosterInventory, 0, 134, -20);
			this.addSlotToContainer(boosterSlot);
		}
		else {
			nullSlot = new NullSlot();
			this.addSlotToContainer(nullSlot);
		}

		// Add hotbar slots
		for (int i = 0; i < 9; ++i) {
			hotbarSlot[i] = new SlotPlayerHotBar(this.inventoryPlayer, i, i * 18 + 8, 58);
			this.addSlotToContainer(hotbarSlot[i]);
		}

		int k = 0;

		// Add player inventory slots
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				inventorySlot[k] = new SlotPlayerInv(this.inventoryPlayer, j + i * 9 + 9, j * 18 + 8, 0 + i * 18);
				this.addSlotToContainer(inventorySlot[k]);
				k++;
			}
		}

		// Add armor slots
		for (int i = 0; i < 4; ++i) {
			armorSlot[i] = new SlotArmor(this.player, this.inventoryPlayer, 39 - i, (int) 8.5, (i * 18) - 76, i);
			this.addSlotToContainer(armorSlot[i]);
		}
		k = 0;
		// Add crafting grid slots
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				craftMatrixSlot[k] = new SlotCraftingMatrix(this, this.craftingGrid, j + i * 3, 80 + j * 18, (i * 18) - 76);
				this.addSlotToContainer(craftMatrixSlot[k]);
				if (k == 0) {
					this.firstCraftingSlotNumber = craftMatrixSlot[k].slotNumber;
				}
				k++;
			}
		}
		this.lastCraftingSlotNumber = craftMatrixSlot[8].slotNumber;

		craftingSlot = new SlotCraftingTerm(this.getPlayerInv().player, this.mySrc, this.getPowerSource(), this.obj, this.craftingGrid, this.craftingGrid, this.output, 174, -58, this);
		// Add crafting result slot
		this.addSlotToContainer(craftingSlot);

		magnetSlot = new SlotMagnet(this.magnetInventory, 152, -20);
		this.addSlotToContainer(magnetSlot);

		trashSlot = new SlotTrash(this.trashInventory, 80, -20, player);
		trashSlot.setContainer(this);
		this.addSlotToContainer(trashSlot);

		updateCraftingMatrix();

		this.onCraftMatrixChanged(this.craftingGrid);
		thisItem.checkForBooster(containerstack);
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
			ic.setInventorySlotContents(x, this.craftMatrixSlot[x].getStack());
		}
		this.craftingSlot.putStack(CraftingManager.getInstance().findMatchingRecipe(ic, this.worldObj));
		writeToNBT("crafting");
	}

	public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
		if (slot >= 0 && slot < this.inventorySlots.size()) {
			final Slot s = this.getSlot(slot);

			if (s instanceof SlotCraftingTerm) {
				switch (action) {
				case CRAFT_SHIFT:
				case CRAFT_ITEM:
				case CRAFT_STACK:
					((SlotCraftingTerm) s).doClick(action, player);
					this.updateHeld(player);
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

				for (final Object j : this.inventorySlots) {
					if (j instanceof Slot && j.getClass() == s.getClass()) {
						from.add((Slot) j);
					}
				}

				for (final Slot fr : from) {
					this.transferStackInSlot(player, fr.slotNumber);
				}
			}

			return;
		}

		// get target item.
		final IAEItemStack slotItem = this.clientRequestedTargetItem;

		switch (action) {
		case SHIFT_CLICK:
			if (this.getPowerSource() == null || this.getCellInventory() == null) {
				return;
			}

			if (slotItem != null) {
				IAEItemStack ais = slotItem.copy();
				ItemStack myItem = ais.getItemStack();

				ais.setStackSize(myItem.getMaxStackSize());

				final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
				myItem.stackSize = (int) ais.getStackSize();
				myItem = adp.simulateAdd(myItem);

				if (myItem != null) {
					ais.setStackSize(ais.getStackSize() - myItem.stackSize);
				}

				ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
				if (ais != null) {
					adp.addItems(ais.getItemStack());
				}
			}
			break;
		case ROLL_DOWN:
			if (this.getPowerSource() == null || this.getCellInventory() == null) {
				return;
			}

			final int releaseQty = 1;
			final ItemStack isg = player.inventory.getItemStack();

			if (isg != null && releaseQty > 0) {
				IAEItemStack ais = AEApi.instance().storage().createItemStack(isg);
				ais.setStackSize(1);
				final IAEItemStack extracted = ais.copy();

				ais = Platform.poweredInsert(this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
				if (ais == null) {
					final InventoryAdaptor ia = new AdaptorPlayerHand(player);

					final ItemStack fail = ia.removeItems(1, extracted.getItemStack(), null);
					if (fail == null) {
						this.getCellInventory().extractItems(extracted, Actionable.MODULATE, this.getActionSource());
					}

					this.updateHeld(player);
				}
			}

			break;
		case ROLL_UP:
		case PICKUP_SINGLE:
			if (this.getPowerSource() == null || this.getCellInventory() == null) {
				return;
			}

			if (slotItem != null) {
				int liftQty = 1;
				final ItemStack item = player.inventory.getItemStack();

				if (item != null) {
					if (item.stackSize >= item.getMaxStackSize()) {
						liftQty = 0;
					}
					if (!Platform.isSameItemPrecise(slotItem.getItemStack(), item)) {
						liftQty = 0;
					}
				}

				if (liftQty > 0) {
					IAEItemStack ais = slotItem.copy();
					ais.setStackSize(1);
					ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
					if (ais != null) {
						final InventoryAdaptor ia = new AdaptorPlayerHand(player);

						final ItemStack fail = ia.addItems(ais.getItemStack());
						if (fail != null) {
							this.getCellInventory().injectItems(ais, Actionable.MODULATE, this.getActionSource());
						}

						this.updateHeld(player);
					}
				}
			}
			break;
		case PICKUP_OR_SET_DOWN:
			if (getPowerSource() == null || this.getCellInventory() == null) {
				return;
			}

			if (player.inventory.getItemStack() == null) {
				if (slotItem != null) {
					IAEItemStack ais = slotItem.copy();
					ais.setStackSize(ais.getItemStack().getMaxStackSize());
					ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
					if (ais != null) {
						player.inventory.setItemStack(ais.getItemStack());
					}
					else {
						player.inventory.setItemStack(null);
					}
					this.updateHeld(player);
				}
			}
			else {
				IAEItemStack ais = AEApi.instance().storage().createItemStack(player.inventory.getItemStack());
				ais = Platform.poweredInsert(this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
				if (ais != null) {
					player.inventory.setItemStack(ais.getItemStack());
				}
				else {
					player.inventory.setItemStack(null);
				}
				this.updateHeld(player);
			}

			break;
		case SPLIT_OR_PLACE_SINGLE:
			if (this.getPowerSource() == null || this.getCellInventory() == null) {
				return;
			}

			if (player.inventory.getItemStack() == null) {
				if (slotItem != null) {
					IAEItemStack ais = slotItem.copy();
					final long maxSize = ais.getItemStack().getMaxStackSize();
					ais.setStackSize(maxSize);
					ais = this.getCellInventory().extractItems(ais, Actionable.SIMULATE, this.getActionSource());

					if (ais != null) {
						final long stackSize = Math.min(maxSize, ais.getStackSize());
						ais.setStackSize((stackSize + 1) >> 1);
						ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
					}

					if (ais != null) {
						player.inventory.setItemStack(ais.getItemStack());
					}
					else {
						player.inventory.setItemStack(null);
					}
					this.updateHeld(player);
				}
			}
			else {
				IAEItemStack ais = AEApi.instance().storage().createItemStack(player.inventory.getItemStack());
				ais.setStackSize(1);
				ais = Platform.poweredInsert(this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
				if (ais == null) {
					final ItemStack is = player.inventory.getItemStack();
					is.stackSize--;
					if (is.stackSize <= 0) {
						player.inventory.setItemStack(null);
					}
					this.updateHeld(player);
				}
			}

			break;
		case CREATIVE_DUPLICATE:
			if (player.capabilities.isCreativeMode && slotItem != null) {
				final ItemStack is = slotItem.getItemStack();
				is.stackSize = is.getMaxStackSize();
				player.inventory.setItemStack(is);
				this.updateHeld(player);
			}
			break;
		case MOVE_REGION:

			if (this.getPowerSource() == null || this.getCellInventory() == null) {
				return;
			}

			if (slotItem != null) {
				final int playerInv = 9 * 4;
				for (int slotNum = 0; slotNum < playerInv; slotNum++) {
					IAEItemStack ais = slotItem.copy();
					ItemStack myItem = ais.getItemStack();

					ais.setStackSize(myItem.getMaxStackSize());

					final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
					myItem.stackSize = (int) ais.getStackSize();
					myItem = adp.simulateAdd(myItem);

					if (myItem != null) {
						ais.setStackSize(ais.getStackSize() - myItem.stackSize);
					}

					ais = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource());
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
		return this.cellInv;
	}

	protected void updateHeld(final EntityPlayerMP p) {
		if (Platform.isServer()) {
			try {
				NetworkHandler.instance.sendTo(new PacketInventoryAction(InventoryAction.UPDATE_HAND, 0, AEItemStack.create(p.inventory.getItemStack())), p);
			}
			catch (final IOException e) {
				WCTLog.debug(e.getMessage());
			}
		}
	}

	private void updateCraftingMatrix() {
		if (!this.containerstack.hasTagCompound()) {
			this.containerstack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound stack = this.containerstack.getTagCompound();
		readMatrixNBT(stack);
		for (int i = 0; i < 9; i++) {
			this.craftingGrid.setInventorySlotContents(i, craftMatrixInventory[i]);
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
		return this.networkNode;
	}

	@Override
	public boolean useRealItems() {
		return true;
	}

	@Override
	public IInventory getInventoryByName(final String name) {
		if (name.equals("player") || name.equals("container.inventory")) {
			return this.getInventoryPlayer();
		}
		if (name.equals("crafting")) {
			return this.craftingGrid;
		}
		return null;
	}

	@Override
	public ItemStack[] getViewCells() {
		return null;
	}

	private WirelessTerminalGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (it != null) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WirelessTerminalGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	@Override
	public void detectAndSendChanges() {

		this.sendCustomName();

		//if (Platform.isSameItem(this.civ.getItemStack(), this.getPlayerInv().getCurrentItem())) {
		//	this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, this.civ.getItemStack());
		//}
		//else {
		//this.setValidContainer(false);
		//}

		// drain 1 ae t
		this.ticks++;
		if (this.ticks > 10) {
			if (!isBoosterInstalled() || !Reference.WCT_BOOSTER_ENABLED) {
				this.civ.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
			}
			else {
				this.civ.extractAEPower((int)(0.5 * this.ticks), Actionable.MODULATE, PowerMultiplier.CONFIG);
			}
			this.ticks = 0;
		}

		if (Platform.isServer()) {
			if (this.monitor != this.civ.getItemInventory()) {
				this.setValidContainer(false);
			}

			for (final Settings set : this.serverCM.getSettings()) {
				final Enum<?> sideLocal = this.serverCM.getSetting(set);
				final Enum<?> sideRemote = this.clientCM.getSetting(set);

				if (sideLocal != sideRemote) {
					this.clientCM.putSetting(set, sideLocal);
					for (final Object crafter : this.crafters) {
						try {
							NetworkHandler.instance.sendTo(new PacketValueConfig(set.name(), sideLocal.name()), (EntityPlayerMP) crafter);
						}
						catch (final IOException e) {
							WCTLog.debug(e.getMessage());
						}
					}
				}
			}

			if (!this.items.isEmpty()) {
				try {
					final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

					final PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();

					for (final IAEItemStack is : this.items) {
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
						this.items.resetStatus();

						for (final Object c : this.crafters) {
							if (c instanceof EntityPlayer) {
								NetworkHandler.instance.sendTo(piu, (EntityPlayerMP) c);
							}
						}
					}
				}
				catch (final IOException e) {
					WCTLog.debug(e.getMessage());
				}
			}

			this.sendCustomName();
			if (Platform.isServer()) {
				for (final Object crafter : this.crafters) {
					final ICrafting icrafting = (ICrafting) crafter;
					for (final SyncData sd : this.syncData.values()) {
						sd.tick(icrafting);
					}
				}
			}
		}
		//if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			if (!isInRange()) {
				if (!isBoosterInstalled() || !Reference.WCT_BOOSTER_ENABLED) {
					if (this.isValidContainer()) {
						this.getPlayerInv().player.addChatMessage(PlayerMessages.OutOfRange.get());
					}
					this.setValidContainer(false);
				}
				if (!networkIsPowered()) {
					if (this.isValidContainer()) {
						this.getPlayerInv().player.addChatMessage(new ChatComponentText(LocaleHandler.NoNetworkPower.getLocal()));
					}
					this.setValidContainer(false);
				}
			}
			else if (!hasAccess(SecurityPermissions.CRAFT, true) || !hasAccess(SecurityPermissions.EXTRACT, true) || !hasAccess(SecurityPermissions.INJECT, true)) {
				if (this.isValidContainer()) {
					this.getPlayerInv().player.addChatMessage(PlayerMessages.CommunicationError.get());
				}
				this.setValidContainer(false);
			}
			else {
				this.setPowerMultiplier(AEConfig.instance.wireless_getDrainRate(this.obj.getRange()));
			}
		//}
		super.detectAndSendChanges();
	}

	public boolean isBoosterInstalled() {
		Slot slot = getSlotFromInventory(this.boosterInventory, BOOSTER_INDEX);
		if (slot == null) {
			return false;
		}
		boolean hasStack = getSlotFromInventory(this.boosterInventory, BOOSTER_INDEX).getHasStack();
		if (hasStack) {
			Item boosterSlotContents = getSlotFromInventory(this.boosterInventory, BOOSTER_INDEX).getStack().getItem();
			if (boosterSlotContents instanceof ItemInfinityBooster) {
				return true;
			}
		}
		return false;
	}

	public boolean isMagnetInstalled() {
		Slot slot = getSlotFromInventory(this.magnetInventory, MAGNET_INDEX);
		if (slot == null) {
			return false;
		}
		boolean hasStack = getSlotFromInventory(this.magnetInventory, MAGNET_INDEX).getHasStack();
		if (hasStack) {
			Item magnetSlotContents = getSlotFromInventory(this.magnetInventory, MAGNET_INDEX).getStack().getItem();
			if (magnetSlotContents instanceof ItemMagnet) {
				return true;
			}
		}
		return false;
	}

	public BaseActionSource getActionSource() {
		return this.mySrc;
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		if (this.isValidContainer()) {
			return true;
		}
		return false;
	}

	private void sendCustomName() {
		if (!this.sentCustomName) {
			this.sentCustomName = true;
			if (Platform.isServer()) {
				ICustomNameObject name = null;
				if (this.obj instanceof ICustomNameObject) {
					name = (ICustomNameObject) this.obj;
				}

				if (this instanceof ICustomNameObject) {
					name = (ICustomNameObject) this;
				}

				if (name != null) {
					if (name.hasCustomName()) {
						this.setCustomName(name.getCustomName());
					}

					if (this.getCustomName() != null) {
						try {
							NetworkHandler.instance.sendTo(new PacketValueConfig("CustomName", this.getCustomName()), (EntityPlayerMP) this.getInventoryPlayer().player);
						}
						catch (final IOException e) {
							WCTLog.debug(e.getMessage());
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
		return this.customName;
	}

	public void stringSync(final int idx, final String value) {
		if (this.syncData.containsKey(idx)) {
			this.syncData.get(idx).update(value);
		}
	}

	public void verifyPermissions(final SecurityPermissions security, final boolean requirePower) {
		if (Platform.isClient()) {
			return;
		}

		this.ticksSinceCheck++;
		if (this.ticksSinceCheck < 20) {
			return;
		}

		this.ticksSinceCheck = 0;
		this.setValidContainer(this.isValidContainer() && this.hasAccess(security, requirePower));
	}

	protected boolean isInRange() {
		return this.obj.rangeCheck(Reference.WCT_BOOSTER_ENABLED && this.isBoosterInstalled());
	}

	protected boolean networkIsPowered() {
		final WCTIActionHost host = this.getActionHost();
		if (host != null) {
			final IGrid grid = this.obj.getTargetGrid();
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
		final IGrid grid = this.obj.getTargetGrid();
		if (grid != null) {
			final IEnergyGrid eg = grid.getCache(IEnergyGrid.class);
			if (!eg.isNetworkPowered()) {
				return false;
			}
		}
		final ISecurityGrid sg = grid.getCache(ISecurityGrid.class);
		if (sg.hasPermission(this.getInventoryPlayer().player, perm)) {
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
		this.locked.add(idx);
	}

	public boolean isValidForSlot(final Slot s, final ItemStack i) {
		return true;
	}

	public void postPartial(final PacketPartialItem packetPartialItem) {
		this.dataChunks.add(packetPartialItem);
		if (packetPartialItem.getPageCount() == this.dataChunks.size()) {
			this.parsePartials();
		}
	}

	private void parsePartials() {
		int total = 0;
		for (final PacketPartialItem ppi : this.dataChunks) {
			total += ppi.getSize();
		}

		final byte[] buffer = new byte[total];
		int cursor = 0;

		for (final PacketPartialItem ppi : this.dataChunks) {
			cursor = ppi.write(buffer, cursor);
		}

		try {
			final NBTTagCompound data = CompressedStreamTools.readCompressed(new ByteArrayInputStream(buffer));
			if (data != null) {
				this.setTargetStack(AEApi.instance().storage().createItemStack(ItemStack.loadItemStackFromNBT(data)));
			}
		}
		catch (final IOException e) {
			WCTLog.debug(e.getMessage());
		}

		this.dataChunks.clear();
	}

	public IAEItemStack getTargetStack() {
		return this.clientRequestedTargetItem;
	}

	public void setTargetStack(final IAEItemStack stack) {
		// client doesn't need to re-send, makes for lower overhead rapid
		// packets.
		if (Platform.isClient()) {
			final ItemStack a = stack == null ? null : stack.getItemStack();
			final ItemStack b = this.clientRequestedTargetItem == null ? null : this.clientRequestedTargetItem.getItemStack();

			if (Platform.isSameItemPrecise(a, b)) {
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
					NetworkHandler.instance.sendToServer(ppi);
				}
			}
			catch (final IOException e) {
				WCTLog.debug(e.getMessage());
				return;
			}
		}

		this.clientRequestedTargetItem = stack == null ? null : stack.copy();
	}

	private void prepareSync() {
		for (final Field f : this.getClass().getFields()) {
			if (f.isAnnotationPresent(GuiSync.class)) {
				final GuiSync annotation = f.getAnnotation(GuiSync.class);
				if (this.syncData.containsKey(annotation.value())) {
					WCTLog.warning("Channel already in use: " + annotation.value() + " for " + f.getName());
				}
				else {
					this.syncData.put(annotation.value(), new SyncData(this, f, annotation));
				}
			}
		}
	}

	protected WCTIActionHost getActionHost() {
		if (this.obj instanceof WCTIActionHost) {
			return (WCTIActionHost) this.obj;
		}
		return null;
	}

	@Override
	public IConfigManager getConfigManager() {
		if (Platform.isServer()) {
			return this.serverCM;
		}
		return this.clientCM;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
		if (this.getGui() != null) {
			this.getGui().updateSetting(manager, settingName, newValue);
		}
	}

	private IConfigManagerHost getGui() {
		return this.gui;
	}

	public void setGui(@Nonnull final IConfigManagerHost gui) {
		this.gui = gui;
	}

	@Override
	public void onListUpdate() {
		for (final Object c : this.crafters) {
			if (c instanceof ICrafting) {
				final ICrafting cr = (ICrafting) c;
				this.queueInventory(cr);
			}
		}
	}

	@Override
	public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final BaseActionSource source) {
		for (final IAEItemStack is : change) {
			this.items.add(is);
		}
	}

	@Override
	public boolean isValid(final Object verificationToken) {
		return true;
	}

	@Override
	public void onContainerClosed(final EntityPlayer player) {
		super.onContainerClosed(player);
		if (this.monitor != null) {
			this.monitor.removeListener(this);
		}
	}

	@Override
	public void addCraftingToCrafters(final ICrafting c) {
		super.addCraftingToCrafters(c);
		this.queueInventory(c);
	}

	@Override
	public void removeCraftingFromCrafters(final ICrafting c) {
		super.removeCraftingFromCrafters(c);

		if (this.crafters.isEmpty() && this.monitor != null) {
			this.monitor.removeListener(this);
		}
	}

	private void queueInventory(final ICrafting c) {
		if (Platform.isServer() && c instanceof EntityPlayer && this.monitor != null) {
			try {
				PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();
				final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

				for (final IAEItemStack send : monitorCache) {
					try {
						piu.appendItem(send);
					}
					catch (final BufferOverflowException boe) {
						NetworkHandler.instance.sendTo(piu, (EntityPlayerMP) c);

						piu = new PacketMEInventoryUpdate();
						piu.appendItem(send);
					}
				}

				NetworkHandler.instance.sendTo(piu, (EntityPlayerMP) c);
			}
			catch (final IOException e) {
				WCTLog.debug(e.getMessage());
			}
		}
	}

	public void onUpdate(final String field, final Object oldValue, final Object newValue) {
		// =]
	}

	public IEnergySource getPowerSource() {
		return this.powerSrc;
	}

	public boolean isPowered() {
		NBTTagCompound testnbt = this.containerstack.getTagCompound();
		double pwr = testnbt.getDouble("internalCurrentPower");
		return (pwr > 0.0);
	}

	public void setPowerSource(final IEnergySource powerSrc) {
		this.powerSrc = powerSrc;
	}

	private double getPowerMultiplier() {
		return this.powerMultiplier;
	}

	void setPowerMultiplier(final double powerMultiplier) {
		this.powerMultiplier = powerMultiplier;
	}

	public InventoryPlayer getPlayerInv() {
		return this.getInventoryPlayer();
	}

	public InventoryPlayer getInventoryPlayer() {
		return this.inventoryPlayer;
	}

	public boolean isValidContainer() {
		return this.isContainerValid;
	}

	public void setValidContainer(final boolean isContainerValid) {
		this.isContainerValid = isContainerValid;
	}

	public Object getTarget() {
		if (this.obj != null) {
			return this.obj;
		}
		return null;
	}

	public ContainerOpenContext getOpenContext() {
		return this.openContext;
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
			NBTTagCompound tagCompound = (NBTTagCompound) tagList.getCompoundTagAt(i);
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
		if (!this.containerstack.hasTagCompound()) {
			this.containerstack.setTagCompound(new NBTTagCompound());
		}
		switch (which) {
		case "booster":
			((WCTInventoryBooster) boosterInventory).writeNBT(this.containerstack.getTagCompound());
			break;
		case "crafting":
			((WCTInventoryCrafting) craftingGrid).writeNBT(this.containerstack.getTagCompound());
			break;
		case "magnet":
			((WCTInventoryMagnet) magnetInventory).writeNBT(this.containerstack.getTagCompound());
			break;
		case "trash":
			((WCTInventoryTrash) trashInventory).writeNBT(this.containerstack.getTagCompound());
			break;
		case "all":
		default:
			((WCTInventoryBooster) boosterInventory).writeNBT(this.containerstack.getTagCompound());
			((WCTInventoryCrafting) craftingGrid).writeNBT(this.containerstack.getTagCompound());
			((WCTInventoryMagnet) magnetInventory).writeNBT(this.containerstack.getTagCompound());
			((WCTInventoryTrash) trashInventory).writeNBT(this.containerstack.getTagCompound());
			break;
		}
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
		final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get(idx); // require AE SLots!
		ItemStack tis = clickSlot.getStack();
		if (tis == null) {
			return null;
		}
		// Try to place armor in armor slot/booster in booster lost first
		if (isInInventory(idx) || isInHotbar(idx)) {
			if (tis.getItem() instanceof ItemArmor) {
				int type = ((ItemArmor) tis.getItem()).armorType;
				if (this.mergeItemStack(tis, ARMOR_START + type, ARMOR_START + type + 1, false)) {
					clickSlot.clearStack();
					return null;
				}
			}
			else if (tis.getItem() instanceof ItemInfinityBooster) {
				if (this.mergeItemStack(tis, BOOSTER_INDEX, BOOSTER_INDEX + 1, false)) {
					clickSlot.clearStack();
					return null;
				}
			}
			else {
				if (tis.getItem() instanceof ItemMagnet) {
					if (this.mergeItemStack(tis, MAGNET_INDEX, MAGNET_INDEX + 1, false)) {
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
		for (final Object is : this.inventorySlots) {
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
				tis = this.shiftStoreItem(tis);

				// target slots in the container...
				for (final Object inventorySlot : this.inventorySlots) {
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
				for (final Object inventorySlot : this.inventorySlots) {
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
					for (final Object inventorySlot : this.inventorySlots) {
						final AppEngSlot cs = (AppEngSlot) inventorySlot;
						final ItemStack destination = cs.getStack();

						if (!(cs.isPlayerSide()) && cs instanceof SlotFake) {
							if (Platform.isSameItemPrecise(destination, tis)) {
								return null;
							}
							else if (destination == null) {
								cs.putStack(tis.copy());
								cs.onSlotChanged();
								this.updateSlot(cs);
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

							if (Platform.isSameItemPrecise(tis, t)) // t.isItemEqual(tis))
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

									this.updateSlot(clickSlot);
									this.updateSlot(d);
									return null;
								}
								else {
									this.updateSlot(d);
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

							if (Platform.isSameItemPrecise(t, tis)) {
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

									this.updateSlot(clickSlot);
									this.updateSlot(d);
									return null;
								}
								else {
									this.updateSlot(d);
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

								this.updateSlot(clickSlot);
								this.updateSlot(d);
								return null;
							}
							else {
								this.updateSlot(d);
							}
						}
					}
				}
			}

			clickSlot.putStack(tis != null ? tis.copy() : null);
		}

		this.updateSlot(clickSlot);
		return null;
	}

	public void swapSlotContents(final int slotA, final int slotB) {
		final Slot a = this.getSlot(slotA);
		final Slot b = this.getSlot(slotB);

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

		if (isA != null && !a.canTakeStack(this.getInventoryPlayer().player)) {
			return;
		}

		if (isB != null && !b.canTakeStack(this.getInventoryPlayer().player)) {
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
		return (cs.getSlotIndex() >= this.firstCraftingSlotNumber) && !(cs.getSlotIndex() <= this.lastCraftingSlotNumber);
	}

	private void updateSlot(final Slot clickSlot) {
		this.detectAndSendChanges();
	}

	private ItemStack shiftStoreItem(final ItemStack input) {
		if (this.getPowerSource() == null || this.civ == null) {
			return input;
		}
		final IAEItemStack ais = Platform.poweredInsert(this.getPowerSource(), this.civ, AEApi.instance().storage().createItemStack(input), this.getActionSource());
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
	public ItemStack slotClick(int slot, int button, int flag, EntityPlayer player) {
		try {
			if (slot >= 0 && getSlot(slot) != null && getSlot(slot).getStack() == RandomUtils.getWirelessTerm(player.inventory)) {
				return null;
			}
			return super.slotClick(slot, button, flag, player);
		}
		catch (IndexOutOfBoundsException e) {
			//When clicking super fast, for some reason, MC tried to access this inv size (max index + 1)
		}
		return null;
	}

	public final void updateFullProgressBar(final int idx, final long value) {
		if (this.syncData.containsKey(idx)) {
			this.syncData.get(idx).update(value);
			return;
		}

		this.updateProgressBar(idx, (int) value);
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
				slot = (Slot) inventorySlots.get(k);
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
				slot = (Slot) inventorySlots.get(k);
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
