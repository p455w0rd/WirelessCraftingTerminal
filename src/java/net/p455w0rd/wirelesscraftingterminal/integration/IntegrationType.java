package net.p455w0rd.wirelesscraftingterminal.integration;

public enum IntegrationType
{

	RFItem( IntegrationSide.BOTH, "RedstoneFlux Power - Items", "CoFHAPI" ),

	//Waila( IntegrationSide.BOTH, "Waila", "Waila" ),

	//InvTweaks( IntegrationSide.CLIENT, "Inventory Tweaks", "inventorytweaks" ),

	NEI( IntegrationSide.CLIENT, "Not Enough Items", "NotEnoughItems" ),
	
	IC2( IntegrationSide.BOTH, "Industrial Craft 2", "IC2" );

	public final IntegrationSide side;
	public final String dspName;
	public final String modID;

	IntegrationType( final IntegrationSide side, final String name, final String modid )
	{
		this.side = side;
		this.dspName = name;
		this.modID = modid;
	}

}
