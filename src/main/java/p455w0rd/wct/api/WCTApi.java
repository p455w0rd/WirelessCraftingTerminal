package p455w0rd.wct.api;

import java.lang.reflect.Method;

import javax.annotation.*;

public abstract class WCTApi {

	protected static WCTApi api = null;

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	@Nullable
	public static WCTApi instance() {
		// Have we already retrieved the api?
		if (WCTApi.api == null) {
			try {
				// Attempt to locate the API implementation
				Class clazz = Class.forName("net.p455w0rd.wirelesscraftingterminal.implementation.WCTAPIImplementation");

				// Get the instance method
				Method instanceAccessor = clazz.getMethod("instance");

				// Attempt to get the API instance
				WCTApi.api = (WCTApi) instanceAccessor.invoke(null);
			}
			catch (Throwable e) {
				// Unable to locate the API, return null
				return null;
			}
		}

		return WCTApi.api;
	}

	@Nonnull
	public abstract IWCTInteractionHelper interact();

}
