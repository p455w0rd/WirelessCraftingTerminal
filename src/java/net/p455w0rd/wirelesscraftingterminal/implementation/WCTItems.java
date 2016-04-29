package net.p455w0rd.wirelesscraftingterminal.implementation;

import net.p455w0rd.wirelesscraftingterminal.api.IWCTItems;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;

public class WCTItems extends IWCTItems {
	
	WCTItems() {
		this.InfinityBoosterCard = new WCTItemDescription(ItemEnum.BOOSTER_CARD.getStack());
		this.WirelessCraftingTerminal = new WCTItemDescription(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getStack());
		this.WCTBoosterBGIcon = new WCTItemDescription(ItemEnum.WCT_BOOSTER_BG_ICON.getStack());
		this.MagnetCard = new WCTItemDescription(ItemEnum.MAGNET_CARD.getStack());
	}
}
