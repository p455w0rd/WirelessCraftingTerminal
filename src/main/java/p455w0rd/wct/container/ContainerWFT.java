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

import javax.annotation.Nonnull;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
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
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.api.IWirelessFluidTerminalItem;
import p455w0rd.wct.api.networking.security.WCTIActionHost;
import p455w0rd.wct.container.slot.NullSlot;
import p455w0rd.wct.container.slot.SlotBooster;
import p455w0rd.wct.container.slot.SlotBoosterEnergy;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModIntegration.Mods;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.integration.Baubles;
import p455w0rd.wct.inventory.WCTInventoryBooster;
import p455w0rd.wct.sync.packets.PacketMEFluidInventoryUpdate;
import p455w0rd.wct.sync.packets.PacketSetInRange;
import p455w0rd.wct.sync.packets.PacketTargetFluidStack;
import p455w0rd.wct.sync.packets.PacketValueConfig;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class ContainerWFT extends WCTBaseContainer implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEFluidStack>, IAEAppEngInventory {

	private final ItemStack fluidTerminal;
	public final WCTInventoryBooster boosterInventory = new WCTInventoryBooster(this);
	private final AppEngSlot boosterSlot;
	private final IConfigManager clientCM;
	private final IMEMonitor<IAEFluidStack> monitor;
	private final IItemList<IAEFluidStack> fluids = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class).createList();
	@GuiSync(99)
	public boolean hasPower = false;
	private ITerminalHost terminal;
	private IConfigManager serverCM;
	private IConfigManagerHost gui;
	private IGridNode networkNode;
	// Holds the fluid the client wishes to extract, or null for insert
	private IAEFluidStack clientRequestedTargetFluid = null;
	private int ticks = 0;

	public ContainerWFT(EntityPlayer player, ITerminalHost hostIn) {
		super(player.inventory, getActionHost(getGuiObject(WCTUtils.getFluidTerm(player.inventory), player, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ)));
		terminal = hostIn;
		clientCM = new ConfigManager(this);
		setCustomName("WFTContainer");
		fluidTerminal = WCTUtils.getFluidTerm(getPlayerInv());

		clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
		clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
		clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);

		if (Platform.isServer()) {
			serverCM = terminal.getConfigManager();
			monitor = terminal.getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));

			if (monitor != null) {
				monitor.addListener(this, null);
				if (terminal instanceof IEnergySource) {
					setPowerSource((IEnergySource) terminal);
				}
				else if (terminal instanceof IGridHost || terminal instanceof IActionHost) {
					final IGridNode node;
					if (terminal instanceof IGridHost) {
						node = ((IGridHost) terminal).getGridNode(AEPartLocation.INTERNAL);
					}
					else if (terminal instanceof IActionHost) {
						node = ((IActionHost) terminal).getActionableNode();
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

		for (int i = 0; i < getPlayerInv().getSizeInventory(); i++) {
			ItemStack currStack = getPlayerInv().getStackInSlot(i);
			if (!currStack.isEmpty() && currStack == fluidTerminal) {
				lockPlayerInventorySlot(i);
			}
		}
		bindPlayerInventory(player.inventory, 8, 222 - 90);

		if (ModConfig.WCT_BOOSTER_ENABLED && !WCTUtils.isWFTCreative(getFluidTerminal())) {
			if (ModConfig.USE_OLD_INFINTY_MECHANIC) {
				addSlotToContainer(boosterSlot = new SlotBooster(boosterInventory, 152, 110));
				boosterSlot.setContainer(this);
			}
			else {
				addSlotToContainer(boosterSlot = new SlotBoosterEnergy(152, 110));
				boosterSlot.setContainer(this);
			}
		}
		else {
			addSlotToContainer(boosterSlot = new NullSlot());
			boosterSlot.setContainer(this);
		}
		readNBT();
	}

	public AppEngSlot getBoosterSlot() {
		return boosterSlot instanceof SlotBooster ? boosterSlot : boosterSlot instanceof SlotBoosterEnergy ? boosterSlot : null;
	}

	public ItemStack getFluidTerminal() {
		return fluidTerminal;
	}

	@Override
	public boolean isValid(Object verificationToken) {
		return true;
	}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change, IActionSource actionSource) {
		for (final IAEFluidStack is : change) {
			fluids.add(is);
		}
	}

	@Override
	public void onListUpdate() {
		for (final IContainerListener c : listeners) {
			queueInventory(c);
		}
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);

		queueInventory(listener);
	}

	@Override
	public void onContainerClosed(final EntityPlayer player) {
		writeToNBT();
		super.onContainerClosed(player);
		if (monitor != null) {
			monitor.removeListener(this);
		}
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

	private void queueInventory(final IContainerListener c) {
		if (Platform.isServer() && c instanceof EntityPlayer && monitor != null) {
			try {
				PacketMEFluidInventoryUpdate piu = new PacketMEFluidInventoryUpdate();
				final IItemList<IAEFluidStack> monitorCache = monitor.getStorageList();

				for (final IAEFluidStack send : monitorCache) {
					try {
						piu.appendFluid(send);
					}
					catch (final BufferOverflowException boe) {
						ModNetworking.instance().sendTo(piu, (EntityPlayerMP) c);

						piu = new PacketMEFluidInventoryUpdate();
						piu.appendFluid(send);
					}
				}

				ModNetworking.instance().sendTo(piu, (EntityPlayerMP) c);
			}
			catch (final IOException e) {
			}
		}
	}

	@Override
	public IConfigManager getConfigManager() {
		if (Platform.isServer()) {
			return serverCM;
		}
		return clientCM;
	}

	public void setTargetStack(final IAEFluidStack stack) {
		if (Platform.isClient()) {
			if (stack == null && clientRequestedTargetFluid == null) {
				return;
			}
			if (stack != null && clientRequestedTargetFluid != null && stack.getFluidStack().isFluidEqual(clientRequestedTargetFluid.getFluidStack())) {
				return;
			}
			ModNetworking.instance().sendToServer(new PacketTargetFluidStack((AEFluidStack) stack));
		}

		clientRequestedTargetFluid = stack == null ? null : stack.copy();
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
		if (getGui() != null) {
			getGui().updateSetting(manager, settingName, newValue);
		}
	}

	@Override
	public void detectAndSendChanges() {
		if (Platform.isServer()) {

			if (obj != null) {
				if (getFluidTerminal() != obj.getItemStack()) {
					if (!getFluidTerminal().isEmpty()) {
						if (ItemStack.areItemsEqual(obj.getItemStack(), getFluidTerminal())) {
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
						//if (!WCTUtils.isInRange(getFluidTerminal())) {
						WCTUtils.setInRange(getFluidTerminal(), true);
						ModNetworking.instance().sendTo(new PacketSetInRange(true), (EntityPlayerMP) getPlayerInv().player);
						//}
					}
				}
				else {
					obj.extractAEPower((int) (Math.min(500.0, AEConfig.instance().wireless_getDrainRate(obj.getRange()))), Actionable.MODULATE, PowerMultiplier.CONFIG);
					if (!ModConfig.USE_OLD_INFINTY_MECHANIC) {
						//if (WCTUtils.isInRange(getFluidTerminal())) {
						WCTUtils.setInRange(getFluidTerminal(), false);
						ModNetworking.instance().sendTo(new PacketSetInRange(false), (EntityPlayerMP) getPlayerInv().player);
						//}
						WCTUtils.drainInfinityEnergy(getFluidTerminal(), getPlayerInv().player);
					}
				}
				ticks = 0;
			}

			if (monitor != terminal.getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class))) {
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
							}
						}
					}
				}
			}

			if (!fluids.isEmpty()) {
				try {
					final IItemList<IAEFluidStack> monitorCache = monitor.getStorageList();

					final PacketMEFluidInventoryUpdate piu = new PacketMEFluidInventoryUpdate();

					for (final IAEFluidStack is : fluids) {
						final IAEFluidStack send = monitorCache.findPrecise(is);
						if (send == null) {
							is.setStackSize(0);
							piu.appendFluid(is);
						}
						else {
							piu.appendFluid(send);
						}
					}

					if (!piu.isEmpty()) {
						fluids.resetStatus();

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
			updatePowerStatus();

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
			if (getFluidTerminal().getItem() instanceof IWirelessFluidTerminalItem && ((IWirelessFluidTerminalItem) getFluidTerminal().getItem()).getAECurrentPower(getFluidTerminal()) <= 0) {
				if (isValidContainer()) {
					WCTUtils.chatMessage(getPlayerInv().player, new TextComponentString("No Power"));
				}
				setValidContainer(false);
			}
		}
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

	protected boolean isInRange() {
		return obj.rangeCheck(hasInfiniteRange());
	}

	private boolean hasInfiniteRange() {
		return WCTUtils.hasInfiniteRange(getFluidTerminal());
	}

	@Override
	public void doAction(EntityPlayerMP player, InventoryAction action, int slot, long id) {
		if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
			super.doAction(player, action, slot, id);
			return;
		}
		ItemStack held = player.inventory.getItemStack();
		if (held.getCount() > 1) {
			// only support stacksize 1 for now
			return;
		}
		IFluidHandlerItem fh = FluidUtil.getFluidHandler(held);
		if (fh == null && !held.isEmpty()) {
			// only fluid handlers items
			return;
		}
		boolean isBucket = held.getItem() == Items.BUCKET || held.getItem() == Items.WATER_BUCKET || held.getItem() == Items.LAVA_BUCKET || held.getItem() == Items.MILK_BUCKET || held.getItem() == ForgeModContainer.getInstance().universalBucket;

		if (action == InventoryAction.FILL_ITEM && clientRequestedTargetFluid != null) {
			AEFluidStack stack = (AEFluidStack) clientRequestedTargetFluid.copy();

			if (held.isEmpty()) {
				IAEItemStack bucket = AEItemStack.fromItemStack(new ItemStack(Items.BUCKET));
				IAEItemStack bucketInSystem = Platform.poweredExtraction(getPowerSource(), obj.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)), bucket, getActionSource(), Actionable.SIMULATE);
				if (bucketInSystem != null) {
					Platform.poweredExtraction(getPowerSource(), obj.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)), bucket, getActionSource(), Actionable.MODULATE);
					held = bucketInSystem.createItemStack();
					fh = FluidUtil.getFluidHandler(held);
					isBucket = true;
				}
				else {
					return;
				}
			}

			if (isBucket && stack.getStackSize() < Fluid.BUCKET_VOLUME) { // Although buckets support less than a buckets worth of fluid, it does not display how much it holds
				return;
			}

			// Check how much we can store in the item
			stack.setStackSize(Integer.MAX_VALUE);
			int amountAllowed = fh.fill(stack.getFluidStack(), false);
			stack.setStackSize(amountAllowed);

			// Check if we can pull out of the system
			IAEFluidStack canPull = Platform.poweredExtraction(getPowerSource(), monitor, stack, getActionSource(), Actionable.SIMULATE);
			if (canPull == null || canPull.getStackSize() < 1 || (isBucket && canPull.getStackSize() != Fluid.BUCKET_VOLUME)) {
				// Either we couldn't pull out of the system,
				// or we are using a bucket and can only pull out less than a buckets worth of fluid
				return;
			}

			// Now actually pull out of the system
			IAEFluidStack pulled = Platform.poweredExtraction(getPowerSource(), monitor, stack, getActionSource());
			if (pulled == null || pulled.getStackSize() < 1) {
				return;
			}

			// Actually fill
			fh.fill(pulled.getFluidStack(), true);

			player.inventory.setItemStack(fh.getContainer());
			updateHeld(player);
		}
		else if (action == InventoryAction.EMPTY_ITEM && fh != null) {

			// See how much we can drain from the item
			FluidStack extract = fh.drain(Integer.MAX_VALUE, false);
			if (extract == null || extract.amount < 1) {
				return;
			}

			// Check if we can push into the system
			IAEFluidStack notPushed = Platform.poweredInsert(getPowerSource(), monitor, AEFluidStack.fromFluidStack(extract), getActionSource(), Actionable.SIMULATE);
			if (isBucket && notPushed != null && notPushed.getStackSize() > 0) {
				// We can't push enough for the bucket
				return;
			}

			IAEFluidStack notInserted = Platform.poweredInsert(getPowerSource(), monitor, AEFluidStack.fromFluidStack(extract), getActionSource());
			if (notInserted != null && notInserted.getStackSize() > 0) {
				// Only try to extract the amount we DID insert
				extract.amount -= Math.toIntExact(notInserted.getStackSize());
			}

			// Actually drain
			fh.drain(extract, true);

			player.inventory.setItemStack(fh.getContainer());
			updateHeld(player);
		}
	}

	private void readNBT() {
		if (fluidTerminal.hasTagCompound()) {
			NBTTagCompound nbt = fluidTerminal.getTagCompound();
			boosterInventory.readFromNBT(nbt, "BoosterSlot");
		}
	}

	public void writeToNBT() {
		if (!fluidTerminal.hasTagCompound()) {
			fluidTerminal.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound newNBT = fluidTerminal.getTagCompound();
		newNBT.setTag("BoosterSlot", boosterInventory.serializeNBT());
		fluidTerminal.setTagCompound(newNBT);
		if (Mods.BAUBLES.isLoaded()) {
			Baubles.sync(getPlayerInv().player, fluidTerminal);
		}
	}

	protected void updatePowerStatus() {
		try {
			if (networkNode != null) {
				setPowered(networkNode.isActive());
			}
			else if (getPowerSource() instanceof IEnergyGrid) {
				setPowered(((IEnergyGrid) getPowerSource()).isNetworkPowered());
			}
			else {
				setPowered(getPowerSource().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.8);
			}
		}
		catch (final Exception ignore) {
			// :P
		}
	}

	private IConfigManagerHost getGui() {
		return gui;
	}

	public void setGui(@Nonnull final IConfigManagerHost gui) {
		this.gui = gui;
	}

	public boolean isPowered() {
		return hasPower;
	}

	private void setPowered(final boolean isPowered) {
		hasPower = isPowered;
	}

	@Override
	public void saveChanges() {
	}

	@Override
	public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
	}

}
