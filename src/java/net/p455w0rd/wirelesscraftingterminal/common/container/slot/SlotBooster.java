package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.p455w0rd.wirelesscraftingterminal.items.ItemBoosterBGIcon;
import net.p455w0rd.wirelesscraftingterminal.items.ItemInfinityBooster;

public class SlotBooster extends AppEngSlot
{
	
	/**
	 * Custom slot for infinity booster card
	 * 
	 * @param inv
	 * @param index
	 * @param xPos
	 * @param yPos
	 */
	public SlotBooster(IInventory inv, int index, int xPos, int yPos)
	{
		super(inv, index, xPos, yPos);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			this.backgroundIcon = ItemBoosterBGIcon.getIcon();
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getBackgroundIconIndex() {
		return this.backgroundIcon;
	}
	
	/**
	 * Ensures only the infinity booster card can be placed in the custom slot
	 * 
	 * @param is
	 */
	@Override
	public boolean isItemValid(ItemStack is)
	{
		if (is != null) {
			if (is.getItem() instanceof ItemInfinityBooster)
			{
				return true;
			}
		}
		// Everything returns false except an instance of our Item
		return false;
	}
	
}
