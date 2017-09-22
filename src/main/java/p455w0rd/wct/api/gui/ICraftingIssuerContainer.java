package p455w0rd.wct.api.gui;

import javax.annotation.Nonnull;

import p455w0rd.wct.api.grid.ICraftingIssuerHost;

public interface ICraftingIssuerContainer {

	@Nonnull
	ICraftingIssuerHost getCraftingHost();

}
