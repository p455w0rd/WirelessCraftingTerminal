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
package p455w0rd.wct.container.slot;

import appeng.container.slot.AppEngSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotOutput extends AppEngSlot {

	public SlotOutput(final IItemHandler a, final int b, final int c, final int d, final int i) {
		super(a, b, c, d);
		setIIcon(i);
	}

	@Override
	public boolean isItemValid(final ItemStack i) {
		return false;
	}
}
