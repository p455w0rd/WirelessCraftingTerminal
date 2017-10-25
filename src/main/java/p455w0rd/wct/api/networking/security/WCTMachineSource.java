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
