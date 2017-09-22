package p455w0rd.wct.api.networking.security;

import appeng.api.networking.security.BaseActionSource;

public class MachineSource extends BaseActionSource {

	public final WCTIActionHost via;

	public MachineSource(final WCTIActionHost v) {
		via = v;
	}

	@Override
	public boolean isMachine() {
		return true;
	}
}
