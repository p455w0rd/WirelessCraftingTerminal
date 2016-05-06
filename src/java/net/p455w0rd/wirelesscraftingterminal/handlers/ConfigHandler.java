package net.p455w0rd.wirelesscraftingterminal.handlers;

import java.io.File;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.p455w0rd.wirelesscraftingterminal.common.WirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationRegistry;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationType;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class ConfigHandler {
	/*
	 * A big part of what goes on here is reloading the cfg on-the-fly
	 * since Ender-Core adds the abilitty to modify config stuff while in game,
	 * I figured I'd add support for such possibilities
	 */
	public static Configuration config;
	public static boolean enableInfinityBooster;
	public static boolean enableEasyMode;
	public static int ae2wctMaxPower = Reference.WCT_MAX_POWER;
	public static int boosterDropChance = Reference.WCT_BOOSTER_DROPCHANCE;
	public static boolean boosterDropsEnabled = Reference.WCT_BOOSTERDROP_ENABLED;
	private static boolean doSave;
	public static boolean firstLoad = true;
	private static int pwrInCfgFile, boosterDropInCfgFile;

	public static void init(File configFile) {
		if (config == null) {
			config = new Configuration(configFile);
		}
		doSave = false;
		loadConfig();
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equalsIgnoreCase(Reference.MODID)) {
			loadConfig();
		}
	}

	private static void loadConfig() {
		String pwrDesc = LocaleHandler.MaxPowerDesc.getLocal() + " [default: 1600000, min: 800000, max:6400000]";
		String boosterDesc = LocaleHandler.InfinityBoosterCfgDesc.getLocal();
		String easyModeDesc = LocaleHandler.EasyModeDesc.getLocal();
		String boosterDropDesc = LocaleHandler.BoosterDropChance.getLocal();
		String boosterDropEnabledDesc = LocaleHandler.DisableBoosterDrop.getLocal();
		enableInfinityBooster = config.getBoolean("enableInfinityBooster", Configuration.CATEGORY_GENERAL, true, boosterDesc);
		enableEasyMode = config.getBoolean("enableEasyMode", Configuration.CATEGORY_GENERAL, false, easyModeDesc);
		boosterDropsEnabled = config.getBoolean("boosterDropsEnabled", Configuration.CATEGORY_GENERAL, true, boosterDropEnabledDesc);
		/*
		 * I did the max power cfg loading like this because while using
		 * Configuration#getInt did enforce the min/max values in-game, it
		 * didn't properly update the config file
		 */
		Property pwrCfgKey = config.get(Configuration.CATEGORY_GENERAL, "ae2wctMaxPower", Reference.WCT_MAX_POWER);
		pwrCfgKey.comment = pwrDesc;
		pwrCfgKey.setDefaultValue(Reference.WCT_MAX_POWER);

		pwrInCfgFile = pwrCfgKey.getInt();
		ae2wctMaxPower = pwrInCfgFile;
		if (pwrInCfgFile > 6400000) {
			ae2wctMaxPower = 6400000;
			pwrCfgKey.setValue(ae2wctMaxPower);
			pwrCfgKey.comment = pwrDesc;
			doSave = true;
		} else if (pwrInCfgFile < 800000) {
			ae2wctMaxPower = 800000;
			pwrCfgKey.setValue(ae2wctMaxPower);
			pwrCfgKey.comment = pwrDesc;
			doSave = true;
		}
		
		Property boosterDropKey = config.get(Configuration.CATEGORY_GENERAL, "boosterDropChance", Reference.WCT_BOOSTER_DROPCHANCE);
		boosterDropKey.comment = boosterDropDesc;
		boosterDropKey.setDefaultValue(Reference.WCT_BOOSTER_DROPCHANCE);

		boosterDropInCfgFile = boosterDropKey.getInt();
		boosterDropChance = boosterDropInCfgFile;
		if (boosterDropInCfgFile > 100) {
			boosterDropChance = 100;
			boosterDropKey.setValue(boosterDropChance);
			boosterDropKey.comment = boosterDropDesc;
			doSave = true;
		} else if (boosterDropInCfgFile < 1) {
			boosterDropChance = 1;
			boosterDropKey.setValue(boosterDropChance);
			boosterDropKey.comment = boosterDropDesc;
			doSave = true;
		}

		Reference.WCT_BOOSTER_ENABLED = enableInfinityBooster;
		Reference.WCT_EASYMODE_ENABLED = enableEasyMode;
		Reference.WCT_MAX_POWER = ae2wctMaxPower;
		Reference.WCT_BOOSTER_DROPCHANCE = boosterDropChance;
		Reference.WCT_BOOSTERDROP_ENABLED = boosterDropsEnabled;

		if (config.hasChanged() || doSave) {
			config.save();
			reloadRecipes();
			removeBooster();
			removeBoosterIcon();
		}
	}

	public static void reloadRecipes() {
		WCTLog.info("Reinitializing Recipes");
		RecipeHandler.removeRecipes();
		RecipeHandler.addRecipes();
	}
	
	public static void removeBoosterIcon() {
		if (IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI)) {
			if (!codechicken.nei.api.ItemInfo.hiddenItems.contains(ItemEnum.BOOSTER_ICON.getStack())) {
				codechicken.nei.api.API.hideItem(ItemEnum.BOOSTER_ICON.getStack());
			}
		}
	}

	public static void removeBooster() {
		if (Reference.WCT_BOOSTER_ENABLED) {
			if (IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI)) {
				if (codechicken.nei.api.ItemInfo.hiddenItems.contains(ItemEnum.BOOSTER_CARD.getStack())) {
					codechicken.nei.api.ItemInfo.hiddenItems.remove(ItemEnum.BOOSTER_CARD.getStack());
				}
			}
			ItemEnum.BOOSTER_CARD.getItem().setCreativeTab(WirelessCraftingTerminal.creativeTab);
		} else {
			if (IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI)) {
				codechicken.nei.api.API.hideItem(ItemEnum.BOOSTER_CARD.getStack());
			}
			ItemEnum.BOOSTER_CARD.getItem().setCreativeTab(null);
		}
		//Added here because the only item being added to creative was the Booster Card
		//even when I set the Creative Tabs in the individual item classes
		ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem().setCreativeTab(WirelessCraftingTerminal.creativeTab);
		ItemEnum.MAGNET_CARD.getItem().setCreativeTab(WirelessCraftingTerminal.creativeTab);
	}
}
