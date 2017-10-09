package p455w0rd.wct.api;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import p455w0rd.wct.init.ModConfig;

public abstract class WCTApi {

	protected static WCTApi API = null;

	@Nullable
	public static WCTApi instance() {
		if (API == null) {
			try {
				Class<?> clazz = Class.forName("net.p455w0rd.wct.implementation.WCTAPIImplementation");
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
	public abstract IWCTInteractionHelper interact();

	@Nonnull
	public abstract IWCTItems items();

	public static boolean isInfinityBoosterCardEnabled() {
		return ModConfig.WCT_BOOSTER_ENABLED;
	}

}
