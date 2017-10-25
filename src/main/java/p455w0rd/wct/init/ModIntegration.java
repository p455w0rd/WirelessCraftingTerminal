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
package p455w0rd.wct.init;

import appeng.api.AEApi;
import net.minecraftforge.fml.common.Loader;
import p455w0rd.wct.integration.ItemScroller;

/**
 * @author p455w0rd
 *
 */
public class ModIntegration {

	public static void preInit() {
		AEApi.instance().registries().wireless().registerWirelessHandler(ModItems.WCT);
		AEApi.instance().registries().charger().addChargeRate(ModItems.WCT, ModConfig.WCT_MAX_POWER);
	}

	public static void postInit() {
		if (Mods.ITEMSCROLLER.isLoaded()) {
			ItemScroller.blackListSlots();
		}
	}

	public static enum Mods {
			BAUBLES("baubles", "Baubles"),
			BAUBLESAPI("Baubles|API", "Baubles API"),
			JEI("jei", "Just Enough Items"), ITEMSCROLLER("itemscroller", "Item Scroller");

		private String modid, name;

		Mods(String modidIn, String nameIn) {
			modid = modidIn;
			name = nameIn;
		}

		public String getId() {
			return modid;
		}

		public String getName() {
			return name;
		}

		public boolean isLoaded() {
			return Loader.isModLoaded(getId());
		}
	}

}
