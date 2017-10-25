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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.init.ModIntegration.Mods;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.integration.Baubles;

public class SlotPlayerHotBar extends AppEngSlot {

	public SlotPlayerHotBar(final IItemHandler inv, final int par2, final int par3, final int par4) {
		super(inv, par2, par3, par4);
	}

	@Override
	public boolean isPlayerSide() {
		return true;
	}

	@Override
	public boolean canTakeStack(final EntityPlayer player) {
		if (Mods.BAUBLES.isLoaded()) {
			if (!Baubles.getWCTBauble(player).isEmpty()) {
				return true;
			}
			if (!getStack().isEmpty() && getStack().getItem() == ModItems.WCT) {
				return super.canTakeStack(player);
			}
		}
		else {
			if (!getStack().isEmpty() && getStack().getItem() == ModItems.WCT) {
				return true;
			}
		}
		return true;
	}

}
