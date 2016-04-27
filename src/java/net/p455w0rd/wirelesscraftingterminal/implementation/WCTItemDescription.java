package net.p455w0rd.wirelesscraftingterminal.implementation;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.api.IWCTItemDescription;

public class WCTItemDescription implements IWCTItemDescription {

	private Item myItem = null;
	private int itemMeta = 0;
	
	WCTItemDescription( final Item item, final int meta )
	{
		this.myItem = item;
		this.itemMeta = meta;
	}
	
	WCTItemDescription( final ItemStack stack )
	{
		this( stack.getItem(), stack.getItemDamage() );
	}
	
	@Override
	public int getDamage()
	{
		return this.itemMeta;
	}

	@Override
	public Item getItem()
	{
		return this.myItem;
	}

	@Override
	public ItemStack getStack()
	{
		return this.getStacks( 1 );
	}

	@Override
	public ItemStack getStacks( final int amount )
	{
		return new ItemStack( this.myItem, amount, this.itemMeta );
	}
	
}
