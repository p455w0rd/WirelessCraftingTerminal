package net.p455w0rd.wirelesscraftingterminal.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class ItemBoosterBGIcon extends Item {
	
	public static IIcon[] boosterIcon;

	public ItemBoosterBGIcon() {
		super();
		setUnlocalizedName("boosterBG");
		setTextureName(Reference.MODID + ":boosterBG");
		setMaxStackSize(0);
		boosterIcon = new IIcon[1];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister register)
	{
		boosterIcon[0] = register.registerIcon(Reference.MODID + ":" + this.getUnlocalizedName().substring(5));
	}
	
	@SideOnly(Side.CLIENT)
	//@Override
	public static IIcon getIcon()
	{
		return boosterIcon[0];
	}
	
}
