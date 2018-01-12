package p455w0rd.wct.api;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	public abstract IWCTInteractionHelper interact();

	@Nonnull
	public abstract IWCTItems items();

	public abstract boolean isInfinityBoosterCardEnabled();

}
