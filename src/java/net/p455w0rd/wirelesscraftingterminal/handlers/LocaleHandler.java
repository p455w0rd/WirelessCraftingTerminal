package net.p455w0rd.wirelesscraftingterminal.handlers;

import net.minecraft.util.StatCollector;

public enum LocaleHandler {

	TitleDesc, InfinityBoosterDesc, WirelessTermLabel("gui.labels.ae2wct"),
	MaxPowerDesc("config.ae2wct"), InfinityBoosterCfgDesc("config.ae2wct"), EasyModeDesc("config.ae2wct"),
	MagnetDesc, MagnetDesc2, PressShift, OnlyWorks, LinkStatus, Installed, NotInstalled, Active, Inactive,
	Status, EmptyTrash, EmptyTrashDesc, MagnetFilterTitle, NoNetworkPower("chatmessages.ae2wct"), FilterMode,
	Whitelisting, Blacklisting, MagnetMode1("chatmessages.ae2wct"), MagnetMode2("chatmessages.ae2wct"), MagnetMode3("chatmessages.ae2wct"),
	MagnetActiveDesc1, MagnetActiveDesc2;

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
