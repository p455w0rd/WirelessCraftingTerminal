/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2016, p455w0rd
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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.integration.Baubles;
import p455w0rd.wct.items.ItemMagnet;

/**
 * @author p455w0rd
 *
 */
public class WCTUtils {

	public static ItemStack getWirelessTerm(InventoryPlayer playerInv) {
		if (playerInv.player.getHeldItemMainhand() != null && playerInv.player.getHeldItemMainhand().getItem() instanceof IWirelessCraftingTerminalItem) {
			return playerInv.player.getHeldItemMainhand();
		}
		ItemStack wirelessTerm = null;
		if (Baubles.isLoaded()) {
			wirelessTerm = Baubles.getWCTBauble(playerInv.player);
		}
		if (wirelessTerm == null) {
			int invSize = playerInv.getSizeInventory();
			if (invSize <= 0) {
				return null;
			}
			for (int i = 0; i < invSize; ++i) {
				ItemStack item = playerInv.getStackInSlot(i);
				if (item == null) {
					continue;
				}
				if (item.getItem() instanceof IWirelessCraftingTerminalItem) {
					wirelessTerm = item;
					break;
				}
			}
		}
		return wirelessTerm;
	}

	public static ItemStack getMagnet(InventoryPlayer playerInv) {
		// Is player holding a Magnet Card?
		if (playerInv.player.getHeldItemMainhand() != null && playerInv.player.getHeldItemMainhand().getItem() instanceof ItemMagnet) {
			return playerInv.player.getHeldItemMainhand();
		}
		// if not true, try to return first magnet card from first
		// wireless term that has a MagnetCard installed
		ItemStack wirelessTerm = getWirelessTerm(playerInv);
		if (wirelessTerm != null && wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem) {
			NBTTagCompound nbtTC = wirelessTerm.getTagCompound();
			if (nbtTC.hasKey("MagnetSlot")) {
				NBTTagList magnetSlot = nbtTC.getTagList("MagnetSlot", 10);
				ItemStack magnetItem = ItemStack.loadItemStackFromNBT(magnetSlot.getCompoundTagAt(0));
				if (magnetItem != null && magnetItem.getItem() instanceof ItemMagnet) {
					return magnetItem;
				}
			}
		}
		// No wireless crafting terminal with Magnet Card installed,
		// is there a Magnet Card in the player's inventory?
		int invSize = playerInv.getSizeInventory();
		ItemStack magnetItem = null;
		if (invSize <= 0) {
			return null;
		}
		for (int i = 0; i < invSize; ++i) {
			ItemStack item = playerInv.getStackInSlot(i);
			if (item == null) {
				continue;
			}
			if (item.getItem() instanceof ItemMagnet) {
				magnetItem = item;
				break;
			}
		}
		return magnetItem;
	}

	public static boolean isMagnetInitialized(ItemStack magnetItem) {
		if (magnetItem != null && magnetItem.getItem() instanceof ItemMagnet) {
			if (!magnetItem.hasTagCompound()) {
				magnetItem.setTagCompound(new NBTTagCompound());
			}
			if (!magnetItem.getTagCompound().hasKey("Initialized")) {
				magnetItem.getTagCompound().setBoolean("Initialized", true);
			}
		}
		return magnetItem.getTagCompound().getBoolean("Initialized");
	}

	public static boolean isMagnetInstalled(InventoryPlayer ip) {
		NBTTagCompound magnetNBTForm = getWirelessTerm(ip).getTagCompound().getTagList("MagnetSlot", 10).getCompoundTagAt(0);
		if (magnetNBTForm != null) {
			ItemStack magnetItem = ItemStack.loadItemStackFromNBT(magnetNBTForm);
			if (magnetItem != null && magnetItem.getItem() instanceof ItemMagnet) {
				return true;
			}
		}
		return false;
	}

	public static void removeTimerTags(ItemStack is) {
		if (is == null || is.getTagCompound() == null) {
			return;
		}
		if (is.getTagCompound().hasKey("WCTReset")) {
			is.setTagCompound(null);
		}
		if (is.getTagCompound() != null) {
			if (is.getTagCompound().hasKey("WCTPickupTimer")) {
				is.getTagCompound().removeTag("WCTPickupTimer");
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static String color(String color) {
		switch (color) {
		case "white":
			return TextFormatting.WHITE.toString();
		case "black":
			return TextFormatting.BLACK.toString();
		case "green":
			return TextFormatting.GREEN.toString();
		case "red":
			return TextFormatting.RED.toString();
		case "yellow":
			return TextFormatting.YELLOW.toString();
		case "aqua":
			return TextFormatting.AQUA.toString();
		case "blue":
			return TextFormatting.BLUE.toString();
		case "italics":
			return TextFormatting.ITALIC.toString();
		case "bold":
			return TextFormatting.BOLD.toString();
		default:
		case "gray":
			return TextFormatting.GRAY.toString();
		}
	}

	@SideOnly(Side.CLIENT)
	public static EntityPlayer player() {
		return Minecraft.getMinecraft().player;
	}

	public static EntityPlayer player(InventoryPlayer playerInv) {
		return playerInv.player;
	}

	@SideOnly(Side.CLIENT)
	public static World world() {
		return Minecraft.getMinecraft().world;
	}

	public static World world(EntityPlayer player) {
		return player.getEntityWorld();
	}

	public static void chatMessage(EntityPlayer player, ITextComponent message) {
		player.sendMessage(message);
	}

}
