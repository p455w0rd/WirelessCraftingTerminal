/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.api.networking.security;

import java.util.Optional;

import com.google.common.base.Preconditions;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import net.minecraft.entity.player.EntityPlayer;

public class WCTPlayerSource implements IActionSource {

	public final EntityPlayer player;
	public final WCTIActionHost via;

	public WCTPlayerSource(final EntityPlayer p, final WCTIActionHost v) {
		Preconditions.checkNotNull(p);
		player = p;
		via = v;
	}

	@Override
	public Optional<EntityPlayer> player() {
		return Optional.of(player);
	}

	@Override
	public Optional<IActionHost> machine() {
		return Optional.ofNullable(via);
	}

	@Override
	public <T> Optional<T> context(Class<T> key) {
		return Optional.empty();
	}
}
