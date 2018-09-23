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
package p455w0rd.wct.integration;

import javax.annotation.Nonnull;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import baubles.common.container.SlotBauble;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.IWirelessFluidTerminalItem;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.slot.SlotAEBauble;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketBaubleSync;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class Baubles {

	@Nonnull
	public static ItemStack getWCTBauble(EntityPlayer player) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
			for (int i = 0; i < baubles.getSlots(); i++) {
				if (baubles.getStackInSlot(i).isEmpty()) {
					continue;
				}
				if (baubles.getStackInSlot(i).getItem() instanceof IWirelessCraftingTerminalItem) {
					return baubles.getStackInSlot(i);
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Nonnull
	public static ItemStack getWFTBauble(EntityPlayer player) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
			for (int i = 0; i < baubles.getSlots(); i++) {
				if (baubles.getStackInSlot(i).isEmpty()) {
					continue;
				}
				if (baubles.getStackInSlot(i).getItem() instanceof IWirelessFluidTerminalItem) {
					return baubles.getStackInSlot(i);
				}
			}
		}
		return ItemStack.EMPTY;
	}

	public static void updateWCTBauble(EntityPlayer player, ItemStack wirelessTerm) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
			for (int i = 0; i < baubles.getSlots(); i++) {
				if (baubles.getStackInSlot(i).isEmpty()) {
					continue;
				}
				if (baubles.getStackInSlot(i).getItem() instanceof IWirelessCraftingTerminalItem) {
					baubles.setStackInSlot(i, wirelessTerm);
					baubles.setChanged(i, true);
				}
			}
		}
	}

	public static int getWCTBaubleSlotIndex(EntityPlayer player) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
			for (int i = 0; i < baubles.getSlots(); i++) {
				if (baubles.getStackInSlot(i) == null) {
					continue;
				}
				if (baubles.getStackInSlot(i).getItem() instanceof IWirelessCraftingTerminalItem) {
					return i;
				}
			}
		}
		return -1;
	}

	public static IBaublesItemHandler getBaubles(EntityPlayer player) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			return player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
		}
		return null;
	}

	public static void setBaublesItemStack(EntityPlayer player, int slot, ItemStack stack) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
			baubles.setStackInSlot(slot, stack);
		}
	}

	public static void addBaubleSlots(ContainerWCT container, EntityPlayer player) {
		IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
		for (int i = 0; i < 7; i++) {
			container.addSlotToContainer(new SlotAEBauble(baubles, i, 178, -62 + i * 18));
		}
	}

	public static boolean isBaubleItem(ItemStack stack) {
		return stack.getItem() instanceof IBauble;
	}

	public static boolean isAEBaubleSlot(Slot slot) {
		return slot instanceof SlotAEBauble;
	}

	public static boolean isBaubleSlot(Slot slot) {
		return slot instanceof SlotBauble;
	}

	public static void sync(EntityPlayer player, ItemStack stack) {
		if (player instanceof EntityPlayerMP) {
			IBaublesItemHandler inv = getBaubles(player);
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack currentStack = inv.getStackInSlot(i);
				if (currentStack.getItem() instanceof IBauble) {
					IBauble bauble = (IBauble) currentStack.getItem();
					BaubleType type = bauble.getBaubleType(currentStack);
					if (bauble.getBaubleType(currentStack) == BaubleType.HEAD && WCTUtils.isAnyWCT(currentStack)) {
						updateWCTBauble(player, stack);
						ModNetworking.instance().sendTo(new PacketBaubleSync(stack), (EntityPlayerMP) player);
					}
				}
			}
		}
	}

	public enum BaubleSlotType {
			NECKLACE, RING1, RING2
	}

}
