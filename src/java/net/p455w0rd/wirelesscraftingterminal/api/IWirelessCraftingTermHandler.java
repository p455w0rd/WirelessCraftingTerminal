package net.p455w0rd.wirelesscraftingterminal.api;

import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.util.IConfigManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Provides the required functionality of a wireless terminal.
 * Presumably this interface would be implemented on an Item, but that is
 * not a requirement.
 * 
 * @author Nividica
 * 
 */
public interface IWirelessCraftingTermHandler extends IWirelessTermHandler, IAEItemPowerStorage {
//public interface IWirelessCraftingTermHandler extends INetworkEncodable, IAEItemPowerStorage {
	/**
	 * Gets the tag used to store the terminal data.
	 * 
	 * @param terminalItemstack
	 * @return
	 */
	//@Nonnull
	//NBTTagCompound getWTerminalTag( @Nonnull ItemStack terminalItemstack );
	
	boolean canHandle( ItemStack is );

	boolean usePower( EntityPlayer player, double amount, ItemStack is );

	boolean hasPower( EntityPlayer player, double amount, ItemStack is );
	
	IConfigManager getConfigManager( ItemStack is );
}
