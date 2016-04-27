package net.p455w0rd.wirelesscraftingterminal.api.gui;

import net.p455w0rd.wirelesscraftingterminal.api.grid.ICraftingIssuerHost;

import javax.annotation.Nonnull;

public interface ICraftingIssuerContainer {

	@Nonnull
	ICraftingIssuerHost getCraftingHost();
	
}
