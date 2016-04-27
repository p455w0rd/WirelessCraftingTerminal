package net.p455w0rd.wirelesscraftingterminal.implementation;

import cpw.mods.fml.common.LoaderState;
import net.p455w0rd.wirelesscraftingterminal.api.IWCTInteractionHelper;
import net.p455w0rd.wirelesscraftingterminal.api.IWCTItems;
import net.p455w0rd.wirelesscraftingterminal.api.WCTApi;
import net.p455w0rd.wirelesscraftingterminal.common.WirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;

public class WCTAPIImplementation extends WCTApi {
	
	private final WCTItems items;
	private final WCTInteractionHelper interactionHelper;
	
	private static WCTAPIImplementation INSTANCE = null;
	
	private WCTAPIImplementation()
	{
		this.items = new WCTItems();
		this.interactionHelper = new WCTInteractionHelper();
	}
	
	public static WCTAPIImplementation instance()
	{
		// Has the singleton been created?
		if( WCTAPIImplementation.INSTANCE == null )
		{
			// Ensure that preinit has finished.
			if( !WCTAPIImplementation.hasFinishedPreInit() )
			{
				return null;
			}
			// Create the singleton.
			WCTAPIImplementation.INSTANCE = new WCTAPIImplementation();
		}
		return WCTAPIImplementation.INSTANCE;
	}
	
	protected static boolean hasFinishedPreInit()
	{
		// Ensure that ThE has finished the PreInit phase
		if( WirelessCraftingTerminal.getLoaderState() == LoaderState.NOINIT )
		{
			// Invalid state, API can not yet load.
			WCTLog.warning( "API is not available until WCT finishes the PreInit phase." );
			return false;
		}

		return true;
	}
	
	@Override
	public IWCTInteractionHelper interact()
	{
		return this.interactionHelper;
	}

	@Override
	public IWCTItems items()
	{
		return this.items;
	}
	
	

}
