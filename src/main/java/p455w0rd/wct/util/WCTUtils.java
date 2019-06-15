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
package p455w0rd.wct.util;

import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.ICustomWirelessTerminalItem;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.WCTApi;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.init.ModKeybindings;
import p455w0rdslib.LibGlobals.Mods;

public class WCTUtils {

	public static final String SHIFTCRAFT_NBT = "ShiftCraft";

	// Parent pair contains a boolean which tells whether or not this is a bauble slot
	// Child pair gives the slot number and ItemStack
	public static Set<Pair<Boolean, Pair<Integer, ItemStack>>> getCraftingTerminals(final EntityPlayer player) {
		return WTApi.instance().getAllWirelessTerminalsByType(player, IWirelessCraftingTerminalItem.class);
	}

	public static ItemStack getWCTBySlot(final EntityPlayer player, final int slot) {
		if (slot >= 0) {
			return WTApi.instance().getWTBySlot(player, slot, IWirelessCraftingTerminalItem.class);
		}
		return ItemStack.EMPTY;
	}

	/**
	 * gets the first available Wireless Crafting Terminal
	 * the Integer of the Pair tells the slotNumber
	 * the boolean tells whether or not the Integer is a Baubles slot
	 */
	@Nonnull
	public static Pair<Boolean, Pair<Integer, ItemStack>> getFirstWirelessCraftingTerminal(final InventoryPlayer playerInv) {
		boolean isBauble = false;
		int slotID = -1;
		ItemStack wirelessTerm = ItemStack.EMPTY;
		if (!playerInv.player.getHeldItemMainhand().isEmpty() && (playerInv.player.getHeldItemMainhand().getItem() instanceof IWirelessCraftingTerminalItem || WTApi.instance().getWUTUtility().doesWUTSupportType(playerInv.player.getHeldItemMainhand(), IWirelessCraftingTerminalItem.class))) {
			slotID = playerInv.currentItem;
			wirelessTerm = playerInv.player.getHeldItemMainhand();
		}
		else {
			if (Mods.BAUBLES.isLoaded()) {
				final Pair<Integer, ItemStack> bauble = WTApi.instance().getBaublesUtility().getFirstWTBaubleByType(playerInv.player, IWirelessCraftingTerminalItem.class);
				if (!bauble.getRight().isEmpty()) {
					wirelessTerm = bauble.getRight();
					slotID = bauble.getLeft();
					if (!wirelessTerm.isEmpty()) {
						isBauble = true;
					}
				}
			}
			if (wirelessTerm.isEmpty()) {
				final int invSize = playerInv.getSizeInventory();
				if (invSize > 0) {
					for (int i = 0; i < invSize; ++i) {
						final ItemStack item = playerInv.getStackInSlot(i);
						if (item.isEmpty()) {
							continue;
						}
						if (isAnyWCT(item)) {
							wirelessTerm = item;
							slotID = i;
							break;
						}
					}
				}
			}
		}
		return Pair.of(isBauble, Pair.of(slotID, wirelessTerm));
	}

	public static boolean isAnyWCT(@Nonnull final ItemStack wirelessTerm) {
		return wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem || WTApi.instance().getWUTUtility().doesWUTSupportType(wirelessTerm, IWirelessCraftingTerminalItem.class);
	}

	@SideOnly(Side.CLIENT)
	public static void handleKeybind() {
		final EntityPlayer p = Minecraft.getMinecraft().player;
		if (p.openContainer == null) {
			return;
		}
		if (ModKeybindings.openTerminal.isPressed()) {
			final Pair<Boolean, Pair<Integer, ItemStack>> pair = WCTUtils.getFirstWirelessCraftingTerminal(p.inventory);
			final ItemStack is = pair.getRight().getRight();
			if (is.isEmpty()) {
				return;
			}
			final int slot = pair.getRight().getLeft();
			final boolean isBauble = pair.getLeft();
			final ICustomWirelessTerminalItem wirelessTerm = (ICustomWirelessTerminalItem) is.getItem();
			if (wirelessTerm != null) {
				if (!(p.openContainer instanceof ContainerWCT)) {
					if (slot >= 0) {
						WCTApi.instance().openWCTGui(p, isBauble, slot);
					}
				}
				else {
					p.closeScreen();
				}
			}
		}
	}

	public static boolean getShiftCraftMode(@Nonnull final ItemStack wirelessTerminal) {
		if (!wirelessTerminal.isEmpty() && wirelessTerminal.hasTagCompound() && wirelessTerminal.getTagCompound().hasKey(SHIFTCRAFT_NBT, NBT.TAG_BYTE)) {
			return !wirelessTerminal.getTagCompound().getBoolean(SHIFTCRAFT_NBT);
		}
		return false;
	}

}
