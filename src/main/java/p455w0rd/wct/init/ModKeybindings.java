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
package p455w0rd.wct.init;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import p455w0rd.ae2wtlib.api.WTApi;

/**
 * @author p455w0rd
 *
 */
public class ModKeybindings {

	public static KeyBinding openTerminal;
	//public static KeyBinding openMagnetFilter;
	public static KeyBinding cycleMagnetMode;

	public static void preInit() {
		openTerminal = new KeyBinding("key.open_wct.desc", Keyboard.CHAR_NONE, WTApi.instance().getConstants().getItemGroup());
		//openMagnetFilter = new KeyBinding("key.open_magnet.desc", Keyboard.CHAR_NONE, "itemGroup.ae2wtlib");
		cycleMagnetMode = new KeyBinding("key.cycle_magnet_mode.desc", Keyboard.CHAR_NONE, WTApi.instance().getConstants().getItemGroup());
		ClientRegistry.registerKeyBinding(openTerminal);
		//ClientRegistry.registerKeyBinding(openMagnetFilter);
		ClientRegistry.registerKeyBinding(cycleMagnetMode);
	}

}
