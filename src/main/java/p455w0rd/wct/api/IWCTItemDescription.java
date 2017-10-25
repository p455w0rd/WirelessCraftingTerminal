/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.api;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IWCTItemDescription {
	/**
	 * Gets the damage or meta value of the item.
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
	ItemStack getStacks(int amount);
}
