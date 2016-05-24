package net.p455w0rd.wirelesscraftingterminal.handlers;

import net.minecraft.util.StatCollector;

public enum LocaleHandler {

	TitleDesc, InfinityBoosterDesc, WirelessTermLabel("gui.labels.ae2wct"), DoVersionCheck("config.ae2wct"), NewVersionAvailable("config.ae2wct"),
	MaxPowerDesc("config.ae2wct"), InfinityBoosterCfgDesc("config.ae2wct"), EasyModeDesc("config.ae2wct"), ClickString("config.ae2wct"),
	MagnetDesc, MagnetDesc2, PressShift, OnlyWorks, LinkStatus, Installed, NotInstalled, Active, Inactive,
	Status, EmptyTrash, EmptyTrashDesc, MagnetFilterTitle, NoNetworkPower("chatmessages.ae2wct"), InitializeMagnet("chatmessages.ae2wct"), FilterMode,
	Whitelisting, Blacklisting, MagnetMode1("chatmessages.ae2wct"), MagnetMode2("chatmessages.ae2wct"), MagnetMode3("chatmessages.ae2wct"),
	MagnetActiveDesc1, MagnetActiveDesc2, BoosterDropChance("config.ae2wct"), DisableBoosterDrop("config.ae2wct"), MineTweakerOverride("config.ae2wct"),
	Not, Using, Ignore, Ignoring, NBTData, MetaData, OreDict, FilteredItems, OrPress, Press, ToSwitchMode;

	private final String root;

	LocaleHandler() {
		this.root = "gui.tooltips.ae2wct";
	}

	LocaleHandler(final String r) {
		this.root = r;
	}

	public String getLocal() {
		return StatCollector.translateToLocal(this.getUnlocalized());
	}

	public String getUnlocalized() {
		return this.root + '.' + this.toString();
	}
}
