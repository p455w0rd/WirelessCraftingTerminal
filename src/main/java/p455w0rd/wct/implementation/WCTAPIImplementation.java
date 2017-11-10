package p455w0rd.wct.implementation;

import net.minecraftforge.fml.common.LoaderState;
import p455w0rd.wct.WCT;
import p455w0rd.wct.api.IWCTInteractionHelper;
import p455w0rd.wct.api.IWCTItems;
import p455w0rd.wct.api.WCTApi;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModItems;

/**
 * @author p455w0rd
 *
 */
public class WCTAPIImplementation extends WCTApi {

	private final WCTInteractionHelper interactionHelper;

	private static WCTAPIImplementation INSTANCE = null;

	private WCTAPIImplementation() {
		interactionHelper = new WCTInteractionHelper();
	}

	public static WCTAPIImplementation instance() {
		if (WCTAPIImplementation.INSTANCE == null) {
			if (!WCTAPIImplementation.hasFinishedPreInit()) {
				return null;
			}
			WCTAPIImplementation.INSTANCE = new WCTAPIImplementation();
		}
		return WCTAPIImplementation.INSTANCE;
	}

	protected static boolean hasFinishedPreInit() {
		if (WCT.PROXY.getLoaderState() == LoaderState.NOINIT) {
			//WCTLog.warning( "API is not available until WCT finishes the PreInit phase." );
			return false;
		}

		return true;
	}

	public IWCTInteractionHelper interact() {
		return interactionHelper;
	}

	@Override
	public IWCTItems items() {
		return ModItems.instance();
	}

	@Override
	public boolean isInfinityBoosterCardEnabled() {
		return ModConfig.WCT_BOOSTER_ENABLED;
	}

}
