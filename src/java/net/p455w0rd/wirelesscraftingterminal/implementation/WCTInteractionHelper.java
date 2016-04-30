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
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketOpenGui;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class WCTInteractionHelper implements IWCTInteractionHelper {

	@Override
	public void openWirelessCraftingTerminalGui(final EntityPlayer player) {
		if ((player == null) || (player instanceof FakePlayer) || player.worldObj.isRemote) {
			return;
		}

		InventoryPlayer inv = player.inventory;
		int invSize = inv.getSizeInventory();
		if (invSize <= 0) {
			return;
		}

		// Get the first wireless terminal
		// If one is held, it takes precidence
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
		if ((wirelessTerminal == null)) {
			return;
		}
		IWirelessCraftingTermHandler terminalInterface = (IWirelessCraftingTermHandler) wirelessTerminal.getItem();
		if (terminalInterface.getAECurrentPower(wirelessTerminal) <= 0) {
			player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
			return;
		}
		if (!isTerminalLinked(terminalInterface, wirelessTerminal)) {
			player.addChatMessage(PlayerMessages.DeviceNotLinked.get());
			return;
		}
		String encKey = terminalInterface.getEncryptionKey(wirelessTerminal);
		ArrayList<IWirelessAccessPoint> accessPoints = WirelessAELink.locateAPsInRangeOfPlayer(player, encKey);
		if (accessPoints == null) {
			player.addChatMessage(PlayerMessages.StationCanNotBeLocated.get());
		}
		else {
			NetworkHandler.instance.sendToServer(new PacketOpenGui(Reference.GUI_WCT));
		}
	}

	public static boolean isTerminalLinked(final IWirelessCraftingTermHandler wirelessTerminal, final ItemStack wirelessTerminalItemstack) {
		return (!wirelessTerminal.getEncryptionKey(wirelessTerminalItemstack).isEmpty());
	}

}
