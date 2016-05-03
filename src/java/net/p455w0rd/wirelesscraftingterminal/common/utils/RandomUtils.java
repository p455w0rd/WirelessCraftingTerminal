package net.p455w0rd.wirelesscraftingterminal.common.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;

public class RandomUtils {

	@SideOnly(Side.CLIENT)
	public static String color(String color) {
		switch (color) {
		case "white":
			return EnumChatFormatting.WHITE.toString();
		case "black":
			return EnumChatFormatting.BLACK.toString();
		case "green":
			return EnumChatFormatting.GREEN.toString();
		case "red":
			return EnumChatFormatting.RED.toString();
		case "yellow":
			return EnumChatFormatting.YELLOW.toString();
		case "aqua":
			return EnumChatFormatting.AQUA.toString();
		case "blue":
			return EnumChatFormatting.BLUE.toString();
		case "italics":
			return EnumChatFormatting.ITALIC.toString();
		case "bold":
			return EnumChatFormatting.BOLD.toString();
		default:
		case "gray":
			return EnumChatFormatting.GRAY.toString();
		}
	}

	public static ItemStack getWirelessTerm(InventoryPlayer playerInv) {
		if (playerInv.player.getHeldItem() != null && playerInv.player.getHeldItem().getItem() instanceof ItemWirelessCraftingTerminal) {
			return playerInv.player.getHeldItem();
		}
		ItemStack wirelessTerm = null;
		int invSize = playerInv.getSizeInventory();
		if (invSize <= 0) {
			return null;
		}
		for (int i = 0; i < invSize; ++i) {
			ItemStack item = playerInv.getStackInSlot(i);
			if (item == null) {
				continue;
			}
			if (item.getItem() instanceof ItemWirelessCraftingTerminal) {
				wirelessTerm = item;
				break;
			}
		}
		return wirelessTerm;
	}

	public static ItemStack getMagnet(InventoryPlayer playerInv) {
		// Is player holding a Magnet Card?
		if (playerInv.player.getHeldItem() != null && playerInv.player.getHeldItem().getItem() instanceof ItemMagnet) {
			return playerInv.player.getHeldItem();
		}
		int invSize = playerInv.getSizeInventory();
		// if not true, try to return first magnet card from first
		// wireless term that has a MagnetCard installed
		ItemStack wirelessTerm = getWirelessTerm(playerInv);
		if (wirelessTerm != null && wirelessTerm.getItem() instanceof ItemWirelessCraftingTerminal) {
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

}
