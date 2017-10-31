package p455w0rd.wct.api;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import p455w0rd.wct.implementation.WCTInteractionHelper;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModItems;

public abstract class WCTApi {

	protected static WCTApi API = null;

	@Nullable
	public static WCTApi instance() {
		if (API == null) {
			try {
				Class<?> clazz = Class.forName("p455w0rd.wct.implementation.WCTAPIImplementation");
				Method instanceAccessor = clazz.getMethod("instance");
				API = (WCTApi) instanceAccessor.invoke(null);
			}
			catch (Throwable e) {
				return null;
			}
		}

		return API;
	}

	@Nonnull
	public IWCTInteractionHelper interact() {
		return new WCTInteractionHelper();
	}

	@Nonnull
	public IWCTItems items() {
		return ModItems.instance();
	}

	public static boolean isInfinityBoosterCardEnabled() {
		return ModConfig.WCT_BOOSTER_ENABLED;
	}

}
