package net.p455w0rd.wirelesscraftingterminal.creativetab;

import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class CreativeTabWCT extends CreativeTabs
{
	public CreativeTabWCT(int id, String unlocalizedName) {
		super(id, unlocalizedName);
	}

	@Override
	public Item getTabIconItem() {
		return ItemEnum.WCT_DUMMY_ICON.getItem();
	}
}
