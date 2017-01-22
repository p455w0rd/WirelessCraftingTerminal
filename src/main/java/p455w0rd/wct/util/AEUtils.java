/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2016, p455w0rd
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
package p455w0rd.wct.util;

import appeng.bootstrap.FeatureFactory;
import appeng.core.*;
import appeng.core.api.definitions.*;

/**
 * @author p455w0rd
 *
 */
public class AEUtils {

	public static Api getApi() {
		return Api.INSTANCE;
	}

	public static ApiDefinitions getDefinitions() {
		return getApi().definitions();
	}

	public static FeatureFactory getRegistry() {
		return getDefinitions().getRegistry();
	}

	public static ApiBlocks getBlocks() {
		return getDefinitions().blocks();
	}

	public static ApiItems getItems() {
		return getDefinitions().items();
	}

	public static ApiMaterials getMaterials() {
		return getDefinitions().materials();
	}

	public static ApiParts getParts() {
		return getDefinitions().parts();
	}

}
