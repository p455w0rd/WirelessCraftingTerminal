package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;

public class SlotMagnet extends AppEngSlot
{
	
	/**
	 * Custom slot for magnet card
	 * 
	 * @param inv
	 * @param index
	 * @param xPos
	 * @param yPos
	 */
	public SlotMagnet(IInventory inv, int xPos, int yPos)
	{
		super(inv, 0, xPos, yPos);
	}
	
	/**
	 * Ensures only the infinity booster card can be placed in the custom slot
	 * 
	 * @param is
	 */
	@Override
	public boolean isItemValid(ItemStack is)
	{
		// Everything returns false except an instance of our Item
		return ((is != null) && (is.getItem() instanceof ItemMagnet));
	}

}
