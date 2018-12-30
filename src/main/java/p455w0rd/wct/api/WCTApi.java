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

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class WCTApi {

	protected static WCTApi api = null;

	@Nullable
	public static WCTApi instance() {
		if (WCTApi.api == null) {
			try {
				Class<?> clazz = Class.forName("p455w0rd.wct.init.ModAPIImpl");
				Method instanceAccessor = clazz.getMethod("instance");
				WCTApi.api = (WCTApi) instanceAccessor.invoke(null);
			}
			catch (Throwable e) {
				return null;
			}
		}

		return WCTApi.api;
	}

	public abstract void openWCTGui(EntityPlayer player, boolean isBauble, int wctSlot);

	// use False and -1 for isWCTaBauble and wctSlot for held magnet item
	public abstract void openMagnetGui(EntityPlayer player, boolean isWCTaBauble, int wctSlot);

	public abstract boolean isTerminalLinked(final ItemStack wirelessTerminalItemstack);

}
