package net.p455w0rd.wirelesscraftingterminal.api;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IWCTItemDescription {
	/**
	 * Gets the damage, or meta, value of the item.
	 * 
	 * @return
	 */
	int getDamage();

	/**
	 * Gets the item.
	 * 
	 * @return
	 */
	@Nonnull
	Item getItem();

	/**
	 * Gets a stack of size 1.
	 * 
	 * @return
	 */
	@Nonnull
	ItemStack getStack();

	/**
	 * Gets a stack of the specified size.
	 * 
	 * @param amount
	 * @return
	 */
	@Nonnull
	ItemStack getStacks( int amount );
}
