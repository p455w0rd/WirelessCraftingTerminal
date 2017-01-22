package p455w0rd.wct.api.networking.security;

import appeng.api.networking.security.*;
import net.minecraft.entity.player.EntityPlayer;

public class WCTPlayerSource extends PlayerSource {

	public final EntityPlayer player;
	public final WCTIActionHost via;

	public WCTPlayerSource(final EntityPlayer p, final WCTIActionHost v) {
		super(p, (IActionHost) v);
		player = p;
		via = v;
	}

	@Override
	public boolean isPlayer() {
		return true;
	}
}
