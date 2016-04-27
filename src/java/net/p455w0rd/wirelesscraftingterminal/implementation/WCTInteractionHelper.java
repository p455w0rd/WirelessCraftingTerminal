package net.p455w0rd.wirelesscraftingterminal.implementation;

import java.util.ArrayList;

import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.core.localization.PlayerMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.p455w0rd.wirelesscraftingterminal.api.IWCTInteractionHelper;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class WCTInteractionHelper implements IWCTInteractionHelper {

	@Override
	public void openWirelessCraftingTerminalGui(final EntityPlayer player) {
		// Valid player?
		if ((player == null) || (player instanceof FakePlayer)) {
			return;
		}

		if (player.worldObj.isRemote) {
			return;
		}

		InventoryPlayer inv = player.inventory;
		int invSize = inv.getSizeInventory();
		if (invSize <= 0) {
			return;
		}

		// Get the item the first wireless terminal
		ItemStack wirelessTerminal = null;

		for (int i = 0; i < invSize; ++i) {
			ItemStack item = inv.getStackInSlot(i);
			if (item == null) {
				continue;
			}
			if (item.getItem() instanceof ItemWirelessCraftingTerminal) {
				wirelessTerminal = item;
				break;
			}
		}
		// Ensure the stack is valid
		if ((wirelessTerminal == null)) {
			// Invalid terminal
			return;
		}

		// Get the interface
		IWirelessCraftingTermHandler terminalInterface = (IWirelessCraftingTermHandler) wirelessTerminal.getItem();

		// Ensure the terminal has power
		if (terminalInterface.getAECurrentPower(wirelessTerminal) == 0) {
			// Terminal is dead
			player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
			return;
		}

		// Ensure the terminal is linked
		if (!isTerminalLinked(terminalInterface, wirelessTerminal)) {
			// Unlinked terminal
			player.addChatMessage(PlayerMessages.DeviceNotLinked.get());
			return;
		}

		// Get the encryption key
		String encKey = terminalInterface.getEncryptionKey(wirelessTerminal);

		// Are any AP's in range?
		ArrayList<IWirelessAccessPoint> accessPoints = WirelessAELink.locateAPsInRangeOfPlayer(player, encKey);

		// Error occured
		if (accessPoints == null) {
			player.addChatMessage(PlayerMessages.StationCanNotBeLocated.get());
		}
		// None in range
		/*
		 * else if( accessPoints.isEmpty() ) { player.addChatMessage(
		 * PlayerMessages.OutOfRange.get() ); player.addChatMessage(new
		 * ChatComponentText("XX")); }
		 */
		// Attempt to launch the gui
		// NOTE: Range check done in here
		else {
			WCTGuiHandler.launchGui(Reference.GUI_WCT, player, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
		}

	}

	public static boolean isTerminalLinked(final IWirelessCraftingTermHandler wirelessTerminal, final ItemStack wirelessTerminalItemstack) {
		return (!wirelessTerminal.getEncryptionKey(wirelessTerminalItemstack).isEmpty());
	}

}
