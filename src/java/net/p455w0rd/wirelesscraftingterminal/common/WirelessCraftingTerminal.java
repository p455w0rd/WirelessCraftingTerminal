package net.p455w0rd.wirelesscraftingterminal.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;

import java.io.File;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.crash.IntegrationCrashEnhancement;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.creativetab.CreativeTabWCT;
import net.p455w0rd.wirelesscraftingterminal.handlers.AchievementHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.ConfigHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.KeybindHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.RecipeHandler;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationRegistry;
import net.p455w0rd.wirelesscraftingterminal.proxy.CommonProxy;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;

@Mod(modid = Reference.MODID, acceptedMinecraftVersions = "[1.7.10]", name = Reference.NAME, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY, dependencies = "required-after:Forge@[" // require
																																																		// forge.
		+ net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
		+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
		+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
		+ net.minecraftforge.common.ForgeVersion.buildVersion + ",);required-after:appliedenergistics2@[rv3-beta-1,);")

public class WirelessCraftingTerminal {

	private static LoaderState WCTState = LoaderState.NOINIT;
	public static CreativeTabs creativeTab;

	@Instance(Reference.MODID)
	public static WirelessCraftingTerminal INSTANCE;

	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;

	private static int ae2wctGuiIndex = 0;
	public static final int GUI_WCT = ++ae2wctGuiIndex;
	public static final int GUI_CRAFTING_STATUS = ++ae2wctGuiIndex;
	public static final int GUI_CRAFT_AMOUNT = ++ae2wctGuiIndex;
	public static final int GUI_CRAFT_CONFIRM = ++ae2wctGuiIndex;
	public static final int GUI_MAGNET = ++ae2wctGuiIndex;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (!Loader.isModLoaded("wct-core")) {
			proxy.missingCoreMod();
		}
		long stopwatch = WCTLog.beginSection("PreInit");
		WirelessCraftingTerminal.WCTState = LoaderState.PREINITIALIZATION;
		WirelessCraftingTerminal.INSTANCE = this;
		creativeTab = new CreativeTabWCT(CreativeTabs.getNextID(), Reference.MODID).setNoScrollbar();
		WirelessCraftingTerminal.proxy.registerItems();
		ConfigHandler.init(new File(event.getModConfigurationDirectory(), Reference.CONFIG_FILE));
		FMLCommonHandler.instance().bus().register(proxy);
		AEApi.instance().registries().wireless().registerWirelessHandler((IWirelessTermHandler) ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem());
		AchievementHandler.init();
		WCTLog.endSection("PreInit", stopwatch);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		long stopwatch = WCTLog.beginSection("Init");
		WirelessCraftingTerminal.WCTState = LoaderState.INITIALIZATION;
		RecipeHandler.addRecipes(true);
		IntegrationRegistry.INSTANCE.init();
		WCTLog.endSection("Init", stopwatch);
	}

	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		long stopwatch = WCTLog.beginSection("PostInit");
		IntegrationRegistry.INSTANCE.postInit();
		FMLCommonHandler.instance().registerCrashCallable(new IntegrationCrashEnhancement());
		NetworkRegistry.INSTANCE.registerGuiHandler(this.INSTANCE, new WCTGuiHandler());
		NetworkHandler.instance = new NetworkHandler("WCT");
		WirelessCraftingTerminal.proxy.removeItemsFromNEI();
		WCTLog.endSection("PostInit", stopwatch);
	}

	public static LoaderState getLoaderState() {
		return WirelessCraftingTerminal.WCTState;
	}
}
