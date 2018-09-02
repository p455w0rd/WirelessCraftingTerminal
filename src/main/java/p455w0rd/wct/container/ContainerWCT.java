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

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
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
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngCraftingSlot;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotInaccessible;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlacableItemType;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.inv.WrapperInvItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.networking.security.WCTIActionHost;
import p455w0rd.wct.container.slot.NullSlot;
import p455w0rd.wct.container.slot.SlotArmor;
import p455w0rd.wct.container.slot.SlotBooster;
import p455w0rd.wct.container.slot.SlotBoosterEnergy;
import p455w0rd.wct.container.slot.SlotMagnet;
import p455w0rd.wct.container.slot.SlotPlayerHotBar;
import p455w0rd.wct.container.slot.SlotPlayerInv;
import p455w0rd.wct.container.slot.SlotTrash;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModIntegration.Mods;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.integration.Baubles;
import p455w0rd.wct.inventory.WCTInventoryBooster;
import p455w0rd.wct.inventory.WCTInventoryMagnet;
import p455w0rd.wct.inventory.WCTInventoryTrash;
import p455w0rd.wct.items.ItemInfinityBooster;
import p455w0rd.wct.sync.packets.PacketMEInventoryUpdate;
import p455w0rd.wct.sync.packets.PacketSetInRange;
import p455w0rd.wct.sync.packets.PacketValueConfig;
import p455w0rd.wct.util.WCTUtils;

public class ContainerWCT extends WCTBaseContainer implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEItemStack>, IAEAppEngInventory, IContainerCraftingPacket, IViewCellStorage {

	private final ItemStack containerstack;
	private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);
	public final WCTInventoryBooster boosterInventory = new WCTInventoryBooster(this);
	public final WCTInventoryMagnet magnetInventory = new WCTInventoryMagnet(this);
	public final WCTInventoryTrash trashInventory = new WCTInventoryTrash(this);
	private final AppEngSlot boosterSlot;
	private final SlotMagnet magnetSlot;
	private final SlotCraftingMatrix[] craftingSlots = new SlotCraftingMatrix[9];
	private final SlotRestrictedInput[] viewCellSlots = new SlotRestrictedInput[4];
	private final SlotCraftingTerm outputSlot;
	public ItemStack craftItem = ItemStack.EMPTY;
	private int ticks = 0;
	private final IItemList<IAEItemStack> items = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
	private final IConfigManager clientCM;
	private IConfigManager serverCM;
	@GuiSync(200)
	public static boolean hasPower = false;
	private IConfigManagerHost gui;
	private final AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
	private final ITerminalHost host;
	private IRecipe currentRecipe;
	private IGridNode networkNode;
	private final AppEngInternalInventory viewCell = new AppEngInternalInventory(this, 4);
	final ContainerNull matrixContainer = new ContainerNull();
	final InventoryCrafting craftingInv;

	public ContainerWCT(EntityPlayer player, ITerminalHost hostIn) {
		super(player.inventory, getActionHost(getGuiObject(WCTUtils.getWirelessTerm(player.inventory), player, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ)));
		initConfig(clientCM = new ConfigManager(this));
		craftingInv = new InventoryCrafting(matrixContainer, 3, 3);
		containerstack = WCTUtils.getWirelessTerm(getPlayerInv());
		setCustomName("WCTContainer");
		host = hostIn;
		if (Platform.isServer()) {
			serverCM = obj.getConfigManager();
			monitor = obj.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
			if (monitor != null) {
				monitor.addListener(this, null);
				setCellInventory(monitor);
				if (obj instanceof IEnergySource) {
					setPowerSource(obj);
				}
				else if (obj instanceof IGridHost || obj instanceof IActionHost) {
					final IGridNode node;
					if (obj instanceof IGridHost) {
						node = ((IGridHost) obj).getGridNode(AEPartLocation.INTERNAL);
					}
					else if (obj instanceof IActionHost) {
						node = ((IActionHost) obj).getActionableNode();
					}
					else {
						node = null;
					}
					if (node != null) {
						networkNode = node;
						final IGrid g = node.getGrid();
						if (g != null) {
							setPowerSource(new ChannelPowerSrc(networkNode, (IEnergySource) g.getCache(IEnergyGrid.class)));
						}
					}
				}
			}
			else {
				WCTUtils.chatMessage(getPlayerInv().player, PlayerMessages.CommunicationError.get());
				setValidContainer(false);
			}
		}
		else {
			monitor = null;
		}

		if (ModConfig.WCT_BOOSTER_ENABLED && !WCTUtils.isWCTCreative(getWirelessTerminal())) {
			if (ModConfig.USE_OLD_INFINTY_MECHANIC) {
				addSlotToContainer(boosterSlot = new SlotBooster(boosterInventory, 134, -20));
				boosterSlot.setContainer(this);
			}
			else {
				addSlotToContainer(boosterSlot = new SlotBoosterEnergy(134, -20));
				boosterSlot.setContainer(this);
			}
		}
		else {
			addSlotToContainer(boosterSlot = new NullSlot());
			boosterSlot.setContainer(this);
		}
		for (int i = 0; i < getPlayerInv().getSizeInventory(); i++) {
			ItemStack currStack = getPlayerInv().getStackInSlot(i);
			if (!currStack.isEmpty() && currStack == containerstack) {
				lockPlayerInventorySlot(i);
			}
		}
		bindPlayerInventory(getPlayerInv(), 8, 0);

		for (int i = 0; i < 4; ++i) {
			addSlotToContainer(new SlotArmor(player, new InvWrapper(getPlayerInv()), 39 - i, (int) 8.5, (i * 18) - 76, EntityEquipmentSlot.values()[6 - (i + 2)]));
		}

		final IItemHandler crafting = getInventoryByName("crafting");

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				addSlotToContainer(craftingSlots[j + i * 3] = new SlotCraftingMatrix(this, crafting, j + i * 3, 80 + j * 18, (i * 18) - 76));
			}
		}
		IActionSource actionSource = ReflectionHelper.getPrivateValue(AEBaseContainer.class, this, "mySrc");
		addSlotToContainer(outputSlot = new SlotCraftingTerm(getPlayerInv().player, actionSource, getPowerSource(), obj, crafting, crafting, output, Mods.BAUBLES.isLoaded() ? 142 : 174, -58, this));
		addSlotToContainer(magnetSlot = new SlotMagnet(magnetInventory, 152, -20));
		addSlotToContainer(new SlotTrash(trashInventory, 98, -22));
		addSlotToContainer(new AppEngSlot(new InvWrapper(getPlayerInv()), 40, 80, -22) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack);
			}

			@Override
			@SideOnly(Side.CLIENT)
			public String getSlotTexture() {
				return "minecraft:items/empty_armor_slot_shield";
			}
		});

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				viewCellSlots[j + i * 2] = new SlotRestrictedInput(PlacableItemType.VIEW_CELL, getViewCellStorage(), j + i * 2, i * 18 - 32, j * 18 + 40, getInventoryPlayer());
				viewCellSlots[j + i * 2].setAllowEdit(true);
				addSlotToContainer(viewCellSlots[j + i * 2]);
			}
		}

		if (Mods.BAUBLES.isLoaded()) {
			Baubles.addBaubleSlots(this, player);
		}
		readNBT();
		onCraftMatrixChanged(new WrapperInvItemHandler(getInventoryByName("crafting")));
		((IWirelessCraftingTerminalItem) containerstack.getItem()).checkForBooster(containerstack);
	}

	public IRecipe getCurrentRecipe() {
		return currentRecipe;
	}

	private void initConfig(IConfigManager cm) {
		cm.registerSetting(Settings.SORT_BY, SortOrder.NAME);
		cm.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
		cm.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
	}

	@Override
	public IItemHandler getViewCellStorage() {
		return viewCell;
	}

	@Override
	public ItemStack[] getViewCells() {
		final ItemStack[] list = new ItemStack[viewCellSlots.length];

		for (int x = 0; x < viewCellSlots.length; x++) {
			list[x] = viewCellSlots[x].getStack();
		}

		return list;
	}

	public SlotRestrictedInput getCellViewSlot(final int index) {
		return viewCellSlots[index];
	}

	@Override
	public boolean canDragIntoSlot(final Slot s) {
		return ((AppEngSlot) s).isDraggable();
	}

	@Override
	public void onCraftMatrixChanged(final IInventory inv) {
		for (int x = 0; x < 9; x++) {
			craftingInv.setInventorySlotContents(x, craftingSlots[x].getStack());
		}
		if (currentRecipe == null || !currentRecipe.matches(craftingInv, getPlayerInv().player.world)) {
			currentRecipe = CraftingManager.findMatchingRecipe(craftingInv, getPlayerInv().player.world);
		}
		if (currentRecipe == null) {
			getResultSlot().putStack(ItemStack.EMPTY);
		}
		else {
			final ItemStack craftingResult = currentRecipe.getCraftingResult(craftingInv);
			getResultSlot().putStack(craftingResult);
		}
		writeToNBT();
	}

	public SlotCraftingTerm getResultSlot() {
		return outputSlot;
	}

	@Override
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
						s.putStack(ItemStack.EMPTY);
					}
					else {
						s.putStack(hand.copy());
					}
					break;
				case PLACE_SINGLE:
					if (!hand.isEmpty()) {
						final ItemStack is = hand.copy();
						is.setCount(1);
						s.putStack(is);
					}
					break;
				case SPLIT_OR_PLACE_SINGLE:
					ItemStack is = s.getStack();
					if (!is.isEmpty()) {
						if (hand.isEmpty()) {
							is.setCount(Math.max(1, is.getCount() - 1));
						}
						else if (hand.isItemEqual(is)) {
							is.setCount(Math.min(is.getMaxStackSize(), is.getCount() + 1));
						}
						else {
							is = hand.copy();
							is.setCount(1);
						}
						s.putStack(is);
					}
					else if (!hand.isEmpty()) {
						is = hand.copy();
						is.setCount(1);
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
						Slot sl = (Slot) j;
						if (!sl.getHasStack() || (sl.getHasStack() && !WCTUtils.isAnyWCT(sl.getStack()))) {
							from.add(sl);
						}
					}
				}

				for (final Slot fr : from) {
					transferStackInSlot(player, fr.slotNumber);
				}
			}

			return;
		}

		// get target item.
		final IAEItemStack slotItem = getTargetStack();

		switch (action) {
		case SHIFT_CLICK:
			if (getPowerSource() == null || getCellInventory() == null) {
				return;
			}

			if (slotItem != null) {
				IAEItemStack ais = slotItem.copy();
				ItemStack myItem = ais.createItemStack();

				ais.setStackSize(myItem.getMaxStackSize());

				final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player);
				myItem.setCount((int) ais.getStackSize());
				myItem = adp.simulateAdd(myItem);

				if (!myItem.isEmpty()) {
					ais.setStackSize(ais.getStackSize() - myItem.getCount());
				}

				ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
				if (ais != null) {
					adp.addItems(ais.createItemStack());
				}
			}
			break;
		case ROLL_DOWN:
			if (getPowerSource() == null || getCellInventory() == null) {
				return;
			}

			final int releaseQty = 1;
			final ItemStack isg = player.inventory.getItemStack();

			if (!isg.isEmpty() && releaseQty > 0) {
				IAEItemStack ais = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(isg);
				ais.setStackSize(1);
				final IAEItemStack extracted = ais.copy();

				ais = Platform.poweredInsert(getPowerSource(), getCellInventory(), ais, getActionSource());
				if (ais == null) {
					final InventoryAdaptor ia = new AdaptorItemHandler(new WrapperCursorItemHandler(player.inventory));

					final ItemStack fail = ia.removeItems(1, extracted.createItemStack(), null);
					if (fail.isEmpty()) {
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

				if (!item.isEmpty()) {
					if (item.getCount() >= item.getMaxStackSize()) {
						liftQty = 0;
					}
					if (!Platform.itemComparisons().isSameItem(slotItem.createItemStack(), item)) {
						liftQty = 0;
					}
				}

				if (liftQty > 0) {
					IAEItemStack ais = slotItem.copy();
					ais.setStackSize(1);
					ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
					if (ais != null) {
						final InventoryAdaptor ia = new AdaptorItemHandler(new WrapperCursorItemHandler(player.inventory));

						final ItemStack fail = ia.addItems(ais.createItemStack());
						if (!fail.isEmpty()) {
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

			if (player.inventory.getItemStack().isEmpty()) {
				if (slotItem != null) {
					IAEItemStack ais = slotItem.copy();
					ais.setStackSize(ais.createItemStack().getMaxStackSize());
					ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
					if (ais != null) {
						player.inventory.setItemStack(ais.createItemStack());
					}
					else {
						player.inventory.setItemStack(ItemStack.EMPTY);
					}
					updateHeld(player);
				}
				return;
			}
			else {
				IAEItemStack ais = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(player.inventory.getItemStack());
				ais = Platform.poweredInsert(getPowerSource(), getCellInventory(), ais, getActionSource());
				if (ais != null) {
					player.inventory.setItemStack(ais.createItemStack());
				}
				else {
					player.inventory.setItemStack(ItemStack.EMPTY);
				}
				updateHeld(player);
			}

			break;
		case SPLIT_OR_PLACE_SINGLE:
			if (getPowerSource() == null || getCellInventory() == null) {
				return;
			}

			if (player.inventory.getItemStack().isEmpty()) {
				if (slotItem != null) {
					IAEItemStack ais = slotItem.copy();
					final long maxSize = ais.getDefinition().getMaxStackSize();
					ais.setStackSize(maxSize);
					ais = getCellInventory().extractItems(ais, Actionable.SIMULATE, getActionSource());

					if (ais != null) {
						final long stackSize = Math.min(maxSize, ais.getStackSize());
						ais.setStackSize((stackSize + 1) >> 1);
						ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
					}

					if (ais != null) {
						player.inventory.setItemStack(ais.createItemStack());
					}
					else {
						player.inventory.setItemStack(ItemStack.EMPTY);
					}
					updateHeld(player);
				}
			}
			else {
				IAEItemStack ais = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(player.inventory.getItemStack());
				ais.setStackSize(1);
				ais = Platform.poweredInsert(getPowerSource(), getCellInventory(), ais, getActionSource());
				if (ais == null) {
					final ItemStack is = player.inventory.getItemStack();
					is.setCount(is.getCount() - 1);
					if (is.getCount() <= 0) {
						player.inventory.setItemStack(ItemStack.EMPTY);
					}
					updateHeld(player);
				}
			}

			break;
		case CREATIVE_DUPLICATE:
			if (player.capabilities.isCreativeMode && slotItem != null) {
				ItemStack is = slotItem.createItemStack();
				is.setCount(is.getMaxStackSize());
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
					ItemStack myItem = ais.createItemStack();

					ais.setStackSize(myItem.getMaxStackSize());

					final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player);
					myItem.setCount((int) ais.getStackSize());
					myItem = adp.simulateAdd(myItem);

					if (!myItem.isEmpty()) {
						ais.setStackSize(ais.getStackSize() - myItem.getCount());
					}

					ais = Platform.poweredExtraction(getPowerSource(), getCellInventory(), ais, getActionSource());
					if (ais != null) {
						adp.addItems(ais.createItemStack());
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

	@Override
	public void saveChanges() {
	}

	@Override
	public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
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
	public IItemHandler getInventoryByName(final String name) {
		if (name.equals("player")) {
			return new PlayerInvWrapper(getInventoryPlayer());
		}
		if (name.equals("crafting")) {
			return craftingGrid;
		}
		return super.getInventoryByName(name);
	}

	@Override
	public void detectAndSendChanges() {
		if (Platform.isClient()) {
			return;
		}
		if (obj != null) {
			if (containerstack != obj.getItemStack()) {
				if (!containerstack.isEmpty()) {
					if (ItemStack.areItemsEqual(obj.getItemStack(), containerstack)) {
						getPlayerInv().setInventorySlotContents(getPlayerInv().currentItem, obj.getItemStack());
					}
					else {
						setValidContainer(false);
					}
				}
				else {
					setValidContainer(false);
				}
			}
		}
		else {
			setValidContainer(false);
		}

		ticks++;
		if (ticks > 10) {
			if (isInRange()) {
				obj.extractAEPower(AEConfig.instance().wireless_getDrainRate(obj.getRange()), Actionable.MODULATE, PowerMultiplier.CONFIG);
				if (!ModConfig.USE_OLD_INFINTY_MECHANIC) {
					//if (!WCTUtils.isInRange(getWirelessTerminal())) {
					WCTUtils.setInRange(getWirelessTerminal(), true);
					ModNetworking.instance().sendTo(new PacketSetInRange(true), (EntityPlayerMP) getPlayerInv().player);
					//}
				}
			}
			else {
				obj.extractAEPower((int) (Math.min(500.0, AEConfig.instance().wireless_getDrainRate(obj.getRange()))), Actionable.MODULATE, PowerMultiplier.CONFIG);
				if (!ModConfig.USE_OLD_INFINTY_MECHANIC) {
					//if (WCTUtils.isInRange(getWirelessTerminal())) {
					WCTUtils.setInRange(getWirelessTerminal(), false);
					ModNetworking.instance().sendTo(new PacketSetInRange(false), (EntityPlayerMP) getPlayerInv().player);
					//}
					WCTUtils.drainInfinityEnergy(getWirelessTerminal(), getPlayerInv().player);
				}
			}
			ticks = 0;
		}

		if (Platform.isServer()) {
			if (monitor != host.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class))) {
				setValidContainer(false);
			}

			for (final Settings set : serverCM.getSettings()) {
				final Enum<?> sideLocal = serverCM.getSetting(set);
				final Enum<?> sideRemote = clientCM.getSetting(set);

				if (sideLocal != sideRemote) {
					clientCM.putSetting(set, sideLocal);
					for (final IContainerListener crafter : listeners) {
						if (crafter instanceof EntityPlayerMP) {
							try {
								ModNetworking.instance().sendTo(new PacketValueConfig(set.name(), sideLocal.name()), (EntityPlayerMP) crafter);
							}
							catch (final IOException e) {
								//
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

						for (final Object c : listeners) {
							if (c instanceof EntityPlayer) {
								ModNetworking.instance().sendTo(piu, (EntityPlayerMP) c);
							}
						}
					}
				}
				catch (final IOException e) {
				}
			}

			super.detectAndSendChanges();

			if (!isInRange()) {
				if (!hasInfiniteRange()) {
					if (isValidContainer()) {
						WCTUtils.chatMessage(getPlayerInv().player, PlayerMessages.OutOfRange.get());
					}
					setValidContainer(false);
				}
				if (!networkIsPowered()) {
					if (isValidContainer()) {
						WCTUtils.chatMessage(getPlayerInv().player, new TextComponentString("No Network Power"));
					}
					setValidContainer(false);
				}
			}
			else if (!hasAccess(SecurityPermissions.CRAFT, true) || !hasAccess(SecurityPermissions.EXTRACT, true) || !hasAccess(SecurityPermissions.INJECT, true)) {
				if (isValidContainer()) {
					WCTUtils.chatMessage(getPlayerInv().player, PlayerMessages.CommunicationError.get());
				}
				setValidContainer(false);
			}
			if (getWirelessTerminal().getItem() instanceof IWirelessCraftingTerminalItem && ((IWirelessCraftingTerminalItem) getWirelessTerminal().getItem()).getAECurrentPower(getWirelessTerminal()) <= 0) {
				if (isValidContainer()) {
					WCTUtils.chatMessage(getPlayerInv().player, new TextComponentString("No Power"));
				}
				setValidContainer(false);
			}
		}
	}

	protected boolean isInRange() {
		return obj.rangeCheck(hasInfiniteRange());
	}

	private boolean hasInfiniteRange() {
		return WCTUtils.hasInfiniteRange(containerstack);
	}

	public int getBoosterIndex() {
		if (getBoosterSlot() != null) {
			for (int i = 0; i < inventorySlots.size(); i++) {
				if ((inventorySlots.get(i) instanceof SlotBooster) || (inventorySlots.get(i) instanceof SlotBoosterEnergy)) {
					return i;
				}
			}
		}
		return -1;
	}

	public AppEngSlot getBoosterSlot() {
		return boosterSlot instanceof SlotBooster ? boosterSlot : boosterSlot instanceof SlotBoosterEnergy ? boosterSlot : null;
	}

	public boolean isMagnetInstalled() {
		for (Slot slot : inventorySlots) {
			if (slot instanceof SlotMagnet && slot.getHasStack() && slot.getStack().getItem() == ModItems.MAGNET_CARD) {
				return true;
			}
		}
		return false;
	}

	public int getMagnetIndex() {
		for (int i = 0; i < inventorySlots.size(); i++) {
			if (inventorySlots.get(i) == getMagnetSlot()) {
				return i;
			}
		}
		return -1;
	}

	public int getBaublesIndex() {
		for (int i = 0; i < inventorySlots.size(); i++) {
			if (Baubles.isAEBaubleSlot(inventorySlots.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public SlotMagnet getMagnetSlot() {
		return magnetSlot;
	}

	public SlotTrash getTrashSlot() {
		for (int i = 0; i < inventorySlots.size(); i++) {
			if (inventorySlots.get(i) instanceof SlotTrash) {
				return (SlotTrash) inventorySlots.get(i);
			}
		}
		return null;
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		return isValidContainer();
	}

	public boolean isInVanillaWAPRange() {
		if (obj == null) {
			BlockPos playerPos = getPlayerInv().player.getPosition();
			obj = new WCTGuiObject((IWirelessTermHandler) containerstack.getItem(), containerstack, getPlayerInv().player, getPlayerInv().player.getEntityWorld(), playerPos.getX(), playerPos.getY(), playerPos.getZ());
		}
		if (obj.getWAP() != null) {
			double wapRange = obj.getWAP().getRange();
			BlockPos wapPos = obj.getWAP().getLocation().getPos();
			BlockPos playerPos = getPlayerInv().player.getPosition();
			double distanceToWap = Math.sqrt(playerPos.distanceSq(wapPos));
			return distanceToWap <= wapRange;
		}
		return false;
	}

	protected boolean networkIsPowered() {
		final WCTIActionHost host = getActionHost(obj);
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
	public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final IActionSource source) {
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
		writeToNBT();
		//super.onContainerClosed(player);
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

	@Override
	public void removeListener(final IContainerListener c) {
		super.removeListener(c);
		if (listeners.isEmpty() && monitor != null) {
			monitor.removeListener(this);
		}
	}

	@Override
	public void onUpdate(final String field, final Object oldValue, final Object newValue) {
	}

	public boolean isPowered() {
		double pwr = ((IWirelessCraftingTerminalItem) containerstack.getItem()).getAECurrentPower(containerstack);
		return (pwr > 0.0);
	}

	private void readNBT() {
		if (containerstack.hasTagCompound()) {
			NBTTagCompound nbt = containerstack.getTagCompound();
			boosterInventory.readFromNBT(nbt, "BoosterSlot");
			magnetInventory.readFromNBT(nbt, "MagnetSlot");
			trashInventory.readFromNBT(nbt, "TrashSlot");
			craftingGrid.readFromNBT(nbt, "CraftingMatrix");
			viewCell.readFromNBT(nbt, "ViewCells");
		}
	}

	public void writeToNBT() {
		if (!containerstack.hasTagCompound()) {
			containerstack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound newNBT = containerstack.getTagCompound();
		newNBT.setTag("BoosterSlot", boosterInventory.serializeNBT());
		newNBT.setTag("MagnetSlot", magnetInventory.serializeNBT());
		newNBT.setTag("TrashSlot", trashInventory.serializeNBT());
		newNBT.setTag("CraftingMatrix", craftingGrid.serializeNBT());
		//newNBT.setTag("ViewCells", viewCell.serializeNBT());
		viewCell.writeToNBT(newNBT, "ViewCells");
		containerstack.setTagCompound(newNBT);
		if (Mods.BAUBLES.isLoaded()) {
			Baubles.sync(getPlayerInv().player, containerstack);
		}
	}

	public ItemStack getWirelessTerminal() {
		return containerstack;
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
		AppEngSlot appEngSlot = null;
		ItemStack tis = ItemStack.EMPTY;
		boolean isAppengSlot = false;
		if (inventorySlots.get(idx) instanceof AppEngSlot) {
			isAppengSlot = true;
			appEngSlot = (AppEngSlot) inventorySlots.get(idx);
			tis = appEngSlot.getStack();
		}
		if (tis.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if (isAppengSlot && appEngSlot != null) {

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

			if (hasMETiles) {
				return ItemStack.EMPTY;
			}

			if (appEngSlot instanceof SlotDisabled || appEngSlot instanceof SlotInaccessible) {
				return ItemStack.EMPTY;
			}
			if (appEngSlot != null && appEngSlot.getHasStack()) {

				if (isInInventory(appEngSlot) || isInHotbar(appEngSlot)) {
					if (tis.getItem() instanceof ItemArmor) {
						int type = ((ItemArmor) tis.getItem()).armorType.getIndex();
						if (mergeItemStack(tis, 40 - type, 40 - type + 1, false)) {
							appEngSlot.clearStack();
							return ItemStack.EMPTY;
						}
					}
					else if (tis.getItem() instanceof ItemInfinityBooster) {
						if (mergeItemStack(tis.copy(), getBoosterIndex(), getBoosterIndex() + 1, false)) {
							if (tis.getCount() > 1 && ModConfig.USE_OLD_INFINTY_MECHANIC) {
								tis.shrink(1);
							}
							else {
								appEngSlot.clearStack();
							}
							return ItemStack.EMPTY;
						}
					}
					else if (tis.getItem() == ModItems.MAGNET_CARD) {
						if (super.mergeItemStack(tis.copy(), getMagnetIndex(), getMagnetIndex() + 1, false)) {
							if (tis.getCount() > 1) {
								tis.shrink(1);
							}
							else {
								appEngSlot.clearStack();
							}
							return ItemStack.EMPTY;
						}
					}
					else if (Mods.BAUBLES.isLoaded() && Baubles.isBaubleItem(tis) && ModConfig.SHIFT_CLICK_BAUBLES) {
						ItemStack tisCopy = tis.copy();
						tisCopy.setCount(1);
						if (mergeItemStack(tisCopy, getBaublesIndex(), getBaublesIndex() + 7, false)) {
							if (tis.getCount() > 1) {
								tis.shrink(1);
							}
							else {
								appEngSlot.clearStack();
							}
							return ItemStack.EMPTY;
						}
					}
					else if (tis.getItem() instanceof ItemShield) {
						if (mergeItemStack(tis.copy(), 53, 54, false)) {
							if (tis.getCount() > 1) {
								tis.shrink(1);
							}
							else {
								appEngSlot.clearStack();
							}
							return ItemStack.EMPTY;
						}
					}
					else if (tis.getItem() == AEApi.instance().definitions().items().viewCell().maybeItem().get()) {
						if (mergeItemStack(tis.copy(), 54, 58, false)) {
							if (tis.getCount() > 1) {
								tis.shrink(1);
							}
							else {
								appEngSlot.clearStack();
							}
							return ItemStack.EMPTY;
						}
					}
				}

				final List<Slot> selectedSlots = new ArrayList<Slot>();

				if (appEngSlot.isPlayerSide()) {
					tis = shiftStoreItem(tis);
					for (final Object inventorySlot : inventorySlots) {
						if (inventorySlot instanceof AppEngSlot) {
							final AppEngSlot cs = (AppEngSlot) inventorySlot;
							if (!(cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof AppEngCraftingSlot)) {
								if (cs.isItemValid(tis)) {
									selectedSlots.add(cs);
								}
							}
						}
					}
				}
				else {
					for (final Object inventorySlot : inventorySlots) {
						if (inventorySlot instanceof AppEngSlot) {
							final AppEngSlot cs = (AppEngSlot) inventorySlot;

							if ((cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof AppEngCraftingSlot)) {
								if (cs.isItemValid(tis)) {
									selectedSlots.add(cs);
								}
							}
						}
					}
				}

				if (selectedSlots.isEmpty() && appEngSlot.isPlayerSide()) {
					if (!tis.isEmpty()) {
						for (final Object inventorySlot : inventorySlots) {
							if (inventorySlot instanceof AppEngSlot) {
								final AppEngSlot cs = (AppEngSlot) inventorySlot;
								final ItemStack destination = cs.getStack();

								if (!(cs.isPlayerSide()) && cs instanceof SlotFake) {
									if (Platform.itemComparisons().isSameItem(destination, tis)) {
										return ItemStack.EMPTY;
									}
									else if (destination.isEmpty()) {
										cs.putStack(tis.copy());
										cs.onSlotChanged();
										return ItemStack.EMPTY;
									}
								}
							}
						}
					}
				}

				if (!tis.isEmpty()) {
					for (final Slot d : selectedSlots) {
						if (d instanceof SlotDisabled || d instanceof SlotME) {
							continue;
						}

						if (d.isItemValid(tis)) {
							if (d.getHasStack()) {
								final ItemStack t = d.getStack();

								if (Platform.itemComparisons().isSameItem(tis, t)) {
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
										appEngSlot.putStack(ItemStack.EMPTY);
										d.onSlotChanged();
										return ItemStack.EMPTY;
									}
								}
							}
						}
					}

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
										appEngSlot.putStack(ItemStack.EMPTY);
										d.onSlotChanged();
										return ItemStack.EMPTY;
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
									appEngSlot.putStack(ItemStack.EMPTY);
									d.onSlotChanged();
									return ItemStack.EMPTY;
								}
							}
						}
					}
				}
				appEngSlot.putStack(!tis.isEmpty() ? tis.copy() : ItemStack.EMPTY);
			}
		}
		return ItemStack.EMPTY;
	}

	@SuppressWarnings("unchecked")
	private ItemStack shiftStoreItem(final ItemStack input) {
		if (getPowerSource() == null || obj == null) {
			return input;
		}
		final IAEItemStack ais = Platform.poweredInsert(getPowerSource(), (IMEMonitor<IAEItemStack>) obj, AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(input), getActionSource());
		if (ais == null) {
			return ItemStack.EMPTY;
		}
		return ais.createItemStack();
	}

	private boolean isInHotbar(@Nonnull AppEngSlot slot) {
		return slot instanceof SlotPlayerHotBar && InventoryPlayer.isHotbar(slot.getSlotIndex());
	}

	private boolean isInInventory(@Nonnull AppEngSlot slot) {
		if (slot instanceof SlotPlayerInv) {
			return slot.slotNumber >= 0 && slot.slotNumber <= 27;
		}
		return false;
	}

	@Override
	public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		ItemStack returnStack = ItemStack.EMPTY;
		try {
			returnStack = super.slotClick(slot, dragType, clickTypeIn, player);
		}
		catch (IndexOutOfBoundsException e) {
		}
		writeToNBT();
		detectAndSendChanges();
		return returnStack;
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean backwards) {
		boolean flag1 = false;
		int k = (backwards ? end - 1 : start);
		Slot slot;
		ItemStack itemstack1;

		if (stack.isStackable()) {
			while (stack.getCount() > 0 && (!backwards && k < end || backwards && k >= start)) {
				slot = inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (!slot.isItemValid(stack)) {
					k += (backwards ? -1 : 1);
					continue;
				}

				if (!itemstack1.isEmpty() && itemstack1.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, itemstack1)) {
					int l = itemstack1.getCount() + stack.getCount();

					if (l <= stack.getMaxStackSize() && l <= slot.getSlotStackLimit()) {
						stack.setCount(0);
						itemstack1.setCount(l);
						flag1 = true;
					}
					else if (itemstack1.getCount() < stack.getMaxStackSize() && l < slot.getSlotStackLimit()) {
						stack.shrink(stack.getMaxStackSize() - itemstack1.getCount());
						itemstack1.setCount(stack.getMaxStackSize());
						flag1 = true;
					}
				}

				k += (backwards ? -1 : 1);
			}
		}
		if (stack.getCount() > 0) {
			k = (backwards ? end - 1 : start);
			while (!backwards && k < end || backwards && k >= start) {
				slot = inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (!slot.isItemValid(stack)) {
					k += (backwards ? -1 : 1);
					continue;
				}

				if (itemstack1.isEmpty()) {
					int l = stack.getCount();
					if (l <= slot.getSlotStackLimit()) {
						slot.putStack(stack.copy());
						stack.setCount(0);
						flag1 = true;
						break;
					}
					else {
						putStackInSlot(k, new ItemStack(stack.getItem(), slot.getSlotStackLimit(), stack.getItemDamage()));
						stack.shrink(slot.getSlotStackLimit());
						flag1 = true;
					}
				}
				k += (backwards ? -1 : 1);
			}
		}
		writeToNBT();
		detectAndSendChanges();
		return flag1;
	}

}
