package net.p455w0rd.wirelesscraftingterminal.creativetab;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;

public class CreativeTabWCT extends CreativeTabs {
	
	public CreativeTabWCT(int id, String unlocalizedName) {
		super(id, unlocalizedName);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack getIconItemStack() {
		ItemStack is = new ItemStack(getTabIconItem());
		((ItemWirelessCraftingTerminal) is.getItem()).injectAEPower(is, 6400001);
		return is;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Item getTabIconItem() {
		return ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem();
	}
}
