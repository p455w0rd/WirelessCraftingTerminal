package net.p455w0rd.wirelesscraftingterminal.implementation;

import java.util.ArrayList;

import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.core.localization.PlayerMessages;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.p455w0rd.wirelesscraftingterminal.api.IWCTInteractionHelper;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketOpenGui;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class WCTInteractionHelper implements IWCTInteractionHelper {

	@Override
	public void openWirelessCraftingTerminalGui(final EntityPlayer player) {
		if ((player == null) || (player instanceof FakePlayer) || (player instanceof EntityPlayerMP) || FMLCommonHandler.instance().getSide() == Side.SERVER) {
			return;
		}

		// Get the first wireless terminal
		// If one is held, it takes precidence
		ItemStack is = RandomUtils.getWirelessTerm(player.inventory);
		if (is == null) {
			return;
		}
		ItemWirelessCraftingTerminal wirelessTerminal = (ItemWirelessCraftingTerminal) is.getItem();
		IWirelessCraftingTermHandler terminalInterface = (IWirelessCraftingTermHandler) wirelessTerminal;
		if (terminalInterface.getAECurrentPower(is) <= 0) {
			player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
			return;
		}
		if (!isTerminalLinked(terminalInterface, is)) {
			player.addChatMessage(PlayerMessages.DeviceNotLinked.get());
			return;
		}
		String encKey = terminalInterface.getEncryptionKey(is);
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
