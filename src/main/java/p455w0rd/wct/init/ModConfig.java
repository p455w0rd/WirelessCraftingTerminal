/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2016, p455w0rd
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

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import p455w0rd.wct.Globals;

/**
 * @author p455w0rd
 *
 */
public class ModConfig {

	public static Configuration CONFIG;
	private static final String DEF_CAT = "General";

	public static boolean WCT_BOOSTER_ENABLED = true;
	public static boolean WCT_MINETWEAKER_OVERRIDE = false;
	public static boolean WCT_ENABLE_CONTROLLER_CHUNKLOADER = true;
	//public static boolean WCT_WITHER_DROPS_BOOSTER = true;
	public static boolean WCT_DRAGON_DROPS_BOOSTER = true;
	public static int WCT_BOOSTER_DROPCHANCE = 5;
	public static int WCT_MAX_POWER = 16000000;

	@SubscribeEvent
	public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent e) {
		if (e.getModID().equals(Globals.MODID)) {
			init();
		}
	}

	public static void init() {
		if (CONFIG == null) {
			CONFIG = new Configuration(new File(Globals.CONFIG_FILE));
			MinecraftForge.EVENT_BUS.register(new ModConfig());
		}
		WCT_BOOSTER_ENABLED = CONFIG.getBoolean("EnableBooster", DEF_CAT, true, "Enable Infinity Booster Card");
		WCT_MINETWEAKER_OVERRIDE = CONFIG.getBoolean("DisableRecipes", DEF_CAT, false, "TRUE=all recipes disabled-For CraftTweaker compat");
		WCT_ENABLE_CONTROLLER_CHUNKLOADER = CONFIG.getBoolean("EnableControllerChunkLoading", DEF_CAT, true, "If true, AE2 controller will chunk load itself");
		WCT_BOOSTER_DROPCHANCE = CONFIG.getInt("BoosterDropChance", DEF_CAT, 5, 1, 100, "Chance in percent (1-100) that booster card will drop upon killing a wither");
		WCT_MAX_POWER = CONFIG.getInt("PowerCapacity", DEF_CAT, 16000000, 100000, 64000000, "How much energy the Wireless Crafting Terminal can store");
		//WCT_WITHER_DROPS_BOOSTER = CONFIG.getBoolean("WitherDropsBooster", DEF_CAT, true, "Should Withers drop Infinity Booster Card?");
		WCT_DRAGON_DROPS_BOOSTER = CONFIG.getBoolean("DragonDropsBooster", DEF_CAT, true, "Should Dragons drop Infinity Booster Card?");

		if (CONFIG.hasChanged()) {
			CONFIG.save();
		}
	}

}
