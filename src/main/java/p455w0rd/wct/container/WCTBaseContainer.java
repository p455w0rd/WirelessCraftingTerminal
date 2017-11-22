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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.SyncData;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotDisabled;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import p455w0rd.wct.api.IWirelessCraftingTermHandler;
import p455w0rd.wct.api.networking.security.WCTIActionHost;
import p455w0rd.wct.api.networking.security.WCTPlayerSource;
import p455w0rd.wct.container.slot.SlotPlayerHotBar;
import p455w0rd.wct.container.slot.SlotPlayerInv;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketInventoryAction;
import p455w0rd.wct.sync.packets.PacketTargetItemStack;
import p455w0rd.wct.sync.packets.PacketValueConfig;

public class WCTBaseContainer extends AEBaseContainer {

	public WCTGuiObject obj;
	protected IMEMonitor<IAEItemStack> monitor;

	public WCTBaseContainer(final InventoryPlayer ip, final Object anchor) {
		super(ip, null, null);
		obj = anchor instanceof WCTGuiObject ? (WCTGuiObject) anchor : null;

		if (obj == null) {
			setValidContainer(false);
		}
		else {
			ReflectionHelper.setPrivateValue(AEBaseContainer.class, this, new WCTPlayerSource(ip.player, getActionHost(obj)), "mySrc");
		}
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

	@Override
	public void setTargetStack(final IAEItemStack stack) {
		if (Platform.isClient()) {

			if (stack == null && getTargetStack() == null) {
				return;
			}
			if (stack != null && stack.isSameType(getTargetStack())) {
				return;
			}
			ModNetworking.instance().sendToServer(new PacketTargetItemStack((AEItemStack) stack));
		}
		ReflectionHelper.setPrivateValue(AEBaseContainer.class, this, stack == null ? null : stack.copy(), "clientRequestedTargetItem");
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
	public Object getTarget() {
		if (obj != null) {
			return obj;
		}
		return null;
	}

	@Override
	protected void bindPlayerInventory(final InventoryPlayer inventoryPlayer, final int offsetX, final int offsetY) {
		IItemHandler ih = new PlayerInvWrapper(inventoryPlayer);
		HashSet<Integer> locked = ReflectionHelper.getPrivateValue(AEBaseContainer.class, this, "locked");
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
			HashMap<Integer, SyncData> syncData = ReflectionHelper.getPrivateValue(AEBaseContainer.class, this, "syncData");
			for (final SyncData sd : syncData.values()) {
				sd.tick(listener);
			}
		}
		for (int i = 0; i < inventorySlots.size(); ++i) {
			ItemStack itemstack = inventorySlots.get(i).getStack();
			ItemStack itemstack1 = inventoryItemStacks.get(i);

			if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
				boolean clientStackChanged = !ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack1, itemstack);
				itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
				inventoryItemStacks.set(i, itemstack1);

				if (clientStackChanged) {
					for (int j = 0; j < listeners.size(); ++j) {
						listeners.get(j).sendSlotContents(this, i, itemstack1);
					}
				}
			}
		}
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		if (isValidContainer()) {
			return true;
		}
		return false;
	}

	@Override
	public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
	}

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

	private void sendCustomName() {
		boolean hasSent = ReflectionHelper.getPrivateValue(AEBaseContainer.class, this, "sentCustomName");
		if (!hasSent) {
			ReflectionHelper.setPrivateValue(AEBaseContainer.class, this, true, "sentCustomName");
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
	public void onUpdate(final String field, final Object oldValue, final Object newValue) {
	}

	public WCTGuiObject getObject() {
		return obj;
	}
}
