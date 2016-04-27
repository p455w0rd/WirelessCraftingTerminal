package net.p455w0rd.wirelesscraftingterminal.api;

import javax.annotation.Nonnull;

public abstract class IWCTItems {
	/**
	 * WirelessCraftingTerminal
	 */
	@Nonnull
	public IWCTItemDescription WirelessCraftingTerminal;
	
	/**
	 * Infinity Booster Card
	 */
	@Nonnull
	public IWCTItemDescription InfinityBoosterCard;
	
	/**
	 * Used for the creat6ive tab
	 * TODO: remove unneeded item and just
	 * add the icon properly
	 */
	@Nonnull
	public IWCTItemDescription WCTDummyIcon;
	
	/**
	 * Item used solely for infinity booster
	 * card slot background icon
	 */
	@Nonnull
	public IWCTItemDescription WCTBoosterBGIcon;
	
	/**
	 * Magnet Card
	 */
	@Nonnull
	public IWCTItemDescription MagnetCard;
}
