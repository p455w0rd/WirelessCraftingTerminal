package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class SlotArmor extends AppEngSlot {

	/*
	 * The armor type that can be placed on that slot, it uses the same values
	 * of armorType field on ItemArmor. Probably could've done just as well
	 * with vanilla armor slot class. if so, this will be removed in the future.
	 */
	final int armorType;

	final EntityPlayer player;

	/**
	 * Custom armor slot
	 * 
	 * @param player
	 * @param inventory
	 * @param slot
	 * @param x
	 * @param y
	 * @param armorType
	 */
	public SlotArmor(EntityPlayer player, IInventory inventory, int slot, int x, int y, int armorType) {
		super(inventory, slot, x, y);
		this.player = player;
		this.armorType = armorType;
	}
	
	@Override
	public int getSlotStackLimit() {
		return 1;
	}

	/**
	 * Checks to see if the item being put into the slot is a valid armor type
	 * 
	 * @param stack
	 */
	@Override
	public boolean isItemValid(ItemStack stack) {
		Item item = (stack == null ? null : stack.getItem());
		return item != null && item.isValidArmor(stack, armorType, player);
	}
	
	/**
	 * Sets the icon of the slot when no armor item is present
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBackgroundIconIndex()
	{
		return ItemArmor.func_94602_b( this.armorType );
	}

}
