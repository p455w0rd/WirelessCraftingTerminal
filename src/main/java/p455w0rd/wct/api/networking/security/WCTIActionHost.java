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

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;

public interface WCTIActionHost extends IActionHost {

	/**
	 * Used to for calculating security rules, you must supply a node from your
	 * IGridHost for the security test, this should be the primary node for the
	 * machine, unless the action is preformed by a non primary node.
	 *
	 * @return the the gridnode that actions from this IGridHost are preformed
	 * by.
	 */
	@Override
	IGridNode getActionableNode();

	IGridNode getActionableNode(boolean ignoreRange);
}
