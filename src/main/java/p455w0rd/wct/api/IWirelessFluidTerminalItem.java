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

import net.minecraft.item.ItemStack;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public interface IWirelessFluidTerminalItem extends IWirelessFluidTermHandler {

	// checks if an Infinity Booster Caqrd is installed on the WFT
	public default boolean checkForBooster(final ItemStack fluidTerminal) {
		return ModConfig.USE_OLD_INFINTY_MECHANIC ? WCTUtils.isBoosterInstalled(fluidTerminal) : (WCTUtils.hasInfiniteRange(fluidTerminal) && WCTUtils.hasInfinityEnergy(fluidTerminal));
	}

	// checks if the Wireless Fluid Terminal is enabled on your item (need for ExtraCells Universal Terminal)
	public boolean isWirelessFluidEnabled(final ItemStack fluidTerminal);

}
