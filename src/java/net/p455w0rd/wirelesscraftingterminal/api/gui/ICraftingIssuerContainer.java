package net.p455w0rd.wirelesscraftingterminal.api.gui;

import javax.annotation.Nonnull;

import net.p455w0rd.wirelesscraftingterminal.api.grid.ICraftingIssuerHost;

public interface ICraftingIssuerContainer {

	@Nonnull
	ICraftingIssuerHost getCraftingHost();
	
}
