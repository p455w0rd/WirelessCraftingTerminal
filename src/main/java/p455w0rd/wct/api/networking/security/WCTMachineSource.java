package p455w0rd.wct.api.networking.security;

import java.util.Optional;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import net.minecraft.entity.player.EntityPlayer;

public class WCTMachineSource implements IActionSource {

	public final WCTIActionHost via;

	public WCTMachineSource(final WCTIActionHost v) {
		via = v;
	}

	@Override
	public <T> Optional<T> context(Class<T> key) {
		return Optional.empty();
	}

	@Override
	public Optional<IActionHost> machine() {
		return Optional.of(via);
	}

	@Override
	public Optional<EntityPlayer> player() {
		return Optional.empty();
	}
}
