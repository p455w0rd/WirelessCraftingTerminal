package net.p455w0rd.wirelesscraftingterminal.reference;

public class Reference {
	public static final String MODID = "ae2wct";
	public static final String VERSION = "@version@";
	public static final String NAME = "AE2 Wireless Crafting Terminal";
	public static final String SERVER_PROXY_CLASS = "net.p455w0rd.wirelesscraftingterminal.proxy.CommonProxy";
	public static final String CLIENT_PROXY_CLASS = "net.p455w0rd.wirelesscraftingterminal.proxy.ClientProxy";
	public static final String GUI_FACTORY = "net.p455w0rd.wirelesscraftingterminal.client.gui.GuiFactory";
	public static final String CREDITS_LIST = "brandon3055, Techjar, AlgorythmX2, thatsIch, Nividica, squeek502, M3gaFr3ak, DrummerMC, cpw, LexManos, Pahimar, diesieben07, Wuppy, Jabelar, blay09, SirSengir, mezz, jotato";
	public static final String CONFIG_FILE = "WirelessCraftingTerminal.cfg";
	public static boolean WCT_DIDTHEDIDDLE =  false;
	public static int WCT_MAX_POWER = 1600000;
	public static boolean WCT_BOOSTER_ENABLED = true;
	public static boolean WCT_EASYMODE_ENABLED = false;
	public static int WCT_CRAFTMATRIX_Y_OFFSET = 100;
	public static int WCT_BOOSTER_DROPCHANCE = 50;
	public static boolean WCT_BOOSTERDROP_ENABLED = true;
	public static boolean WCT_MINETWEAKER_OVERRIDE = false;
	public static boolean WCT_DOVERSIONCHECK = true;
	public static boolean WCT_HASCHECKEDVERSION = false;
	
	// Gui IDs
	private static int ae2wctGuiIndex = 0;
	public static final int GUI_WCT = ++ae2wctGuiIndex;
	public static final int GUI_CRAFTING_STATUS = ++ae2wctGuiIndex;
	public static final int GUI_CRAFT_AMOUNT = ++ae2wctGuiIndex;
	public static final int GUI_CRAFT_CONFIRM = ++ae2wctGuiIndex;
	public static final int GUI_MAGNET = ++ae2wctGuiIndex;
}
