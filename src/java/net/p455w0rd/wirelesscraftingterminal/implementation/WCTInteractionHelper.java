package net.p455w0rd.wirelesscraftingterminal.implementation;

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
		// If one is held, it takes precedence
		ItemStack is = RandomUtils.getWirelessTerm(player.inventory);
		if (is == null) {
			return;
		}
		if (is.hasTagCompound()) {
			// Get the security terminal source key
			String sourceKey = is.getTagCompound().getString(ItemWirelessCraftingTerminal.LINK_KEY_STRING);

			// Ensure the source is not empty nor null
			if ((sourceKey != null) && (!sourceKey.isEmpty())) {
				// The terminal is linked.
				NetworkHandler.instance.sendToServer(new PacketOpenGui(Reference.GUI_WCT));
			}
		}
		
	}

	public static boolean isTerminalLinked(final IWirelessCraftingTermHandler wirelessTerminal, final ItemStack wirelessTerminalItemstack) {
		return (!wirelessTerminal.getEncryptionKey(wirelessTerminalItemstack).isEmpty());
	}

}
