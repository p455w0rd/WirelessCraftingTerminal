package net.p455w0rd.wirelesscraftingterminal.common.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
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
			if (wirelessTerm == null) {
				continue;
			}
		}
		if (wirelessTerm == null) {
			if (playerInv.player.getHeldItem() != null) {
				return playerInv.player.getHeldItem();
			}
			else {
				return ItemEnum.WIRELESS_CRAFTING_TERMINAL.getStack();
			}
		}
		else {
			return wirelessTerm;
		}
	}
	
	public static ItemStack getMagnet(InventoryPlayer playerInv) {
		if (playerInv.player.getHeldItem() != null && playerInv.player.getHeldItem().getItem() instanceof ItemMagnet) {
			return playerInv.player.getHeldItem();
		}
		ItemStack magnetItem = null;
		int invSize = playerInv.getSizeInventory();
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
			if (magnetItem == null) {
				continue;
			}
		}
		if (magnetItem == null) {
			if (playerInv.player.getHeldItem() != null) {
				return playerInv.player.getHeldItem();
			}
			else {
				return ItemEnum.WIRELESS_CRAFTING_TERMINAL.getStack();
			}
		}
		else {
			return magnetItem;
		}
	}
	
}
