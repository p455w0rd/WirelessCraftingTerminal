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
package p455w0rd.wct.recipes.factories.conditions;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;

import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import p455w0rd.wct.init.ModConfig;

/**
 * @author p455w0rd
 *
 */
public class BoosterRecipeEnabled implements IConditionFactory {

	@Override
	public BooleanSupplier parse(JsonContext jsonContext, JsonObject jsonObject) {
		return () -> !ModConfig.WCT_MINETWEAKER_OVERRIDE && ModConfig.WCT_BOOSTER_ENABLED && !ModConfig.WCT_DISABLE_BOOSTER_RECIPE;
	}

}
