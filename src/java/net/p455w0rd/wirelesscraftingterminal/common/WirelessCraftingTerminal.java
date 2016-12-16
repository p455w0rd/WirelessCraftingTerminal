package net.p455w0rd.wirelesscraftingterminal.common;

import java.io.File;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.creativetab.CreativeTabWCT;
import net.p455w0rd.wirelesscraftingterminal.handlers.AchievementHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.ConfigHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.RecipeHandler;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationRegistry;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.proxy.CommonProxy;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

@Mod(modid = Reference.MODID, acceptedMinecraftVersions = "[1.7.10]", name = Reference.NAME, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY, dependencies = "" + "required-after:Forge@[" + net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
		+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
		+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
		+ net.minecraftforge.common.ForgeVersion.buildVersion + ",);" + "required-after:appliedenergistics2@[rv3-beta-1,);required-after:p455w0rdslib;after:NotEnoughItems;")

public class WirelessCraftingTerminal {

	private static LoaderState WCTState = LoaderState.NOINIT;
	public static CreativeTabs creativeTab;

	@Instance(Reference.MODID)
	public static WirelessCraftingTerminal INSTANCE;

	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		long stopwatch = WCTLog.beginSection("PreInit");
		WirelessCraftingTerminal.WCTState = LoaderState.PREINITIALIZATION;
		WirelessCraftingTerminal.INSTANCE = this;
		creativeTab = new CreativeTabWCT(CreativeTabs.getNextID(), Reference.MODID).setNoScrollbar();
		WirelessCraftingTerminal.proxy.registerItems();
		ConfigHandler.init(new File(event.getModConfigurationDirectory(), Reference.CONFIG_FILE));
		FMLCommonHandler.instance().bus().register(proxy);
		AEApi.instance().registries().wireless().registerWirelessHandler((IWirelessTermHandler) ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem());
		WCTLog.endSection("PreInit", stopwatch);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		long stopwatch = WCTLog.beginSection("Init");
		IntegrationRegistry.INSTANCE.init();
		WirelessCraftingTerminal.WCTState = LoaderState.INITIALIZATION;
		RecipeHandler.loadRecipes(!Reference.WCT_MINETWEAKER_OVERRIDE);
		AchievementHandler.init();
		WCTLog.endSection("Init", stopwatch);
	}

	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		long stopwatch = WCTLog.beginSection("PostInit");
		IntegrationRegistry.INSTANCE.postInit();
		NetworkRegistry.INSTANCE.registerGuiHandler(WirelessCraftingTerminal.INSTANCE, new WCTGuiHandler());
		NetworkHandler.instance = new NetworkHandler("WCT");
		WirelessCraftingTerminal.proxy.removeItemsFromNEI();
		WCTLog.endSection("PostInit", stopwatch);
	}

	public static LoaderState getLoaderState() {
		return WirelessCraftingTerminal.WCTState;
	}
}
