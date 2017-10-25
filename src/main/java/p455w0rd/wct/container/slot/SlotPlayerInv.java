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
import net.minecraftforge.items.IItemHandler;

// there is nothing special about this slot, its simply used to represent the
// players inventory, vs a container slot.

public class SlotPlayerInv extends AppEngSlot {

	public SlotPlayerInv(final IItemHandler par1iInventory, final int par2, final int par3, final int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isPlayerSide() {
		return true;
	}

}
