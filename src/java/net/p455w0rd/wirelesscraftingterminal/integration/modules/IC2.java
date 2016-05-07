/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package net.p455w0rd.wirelesscraftingterminal.integration.modules;

import net.p455w0rd.wirelesscraftingterminal.helpers.Reflected;
import net.p455w0rd.wirelesscraftingterminal.integration.IIntegrationModule;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationHelper;

public class IC2 implements IIntegrationModule
{
	@Reflected
	public static IC2 instance;

	@Reflected
	public IC2()
	{
		IntegrationHelper.testClassExistence( this, ic2.api.item.IElectricItem.class );
		IntegrationHelper.testClassExistence( this, ic2.api.item.ISpecialElectricItem.class );
		IntegrationHelper.testClassExistence( this, ic2.api.item.IElectricItemManager.class );
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
	}
}
