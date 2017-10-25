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
package p455w0rd.wct.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.wct.init.ModConfig;

/**
 * @author p455w0rd
 *
 */
public class ItemInfinityBooster extends ItemBase {

	public static final String name = "infinity_booster_card";

	public ItemInfinityBooster() {
		super(name);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(final ItemStack is, final World world, final List<String> list, final ITooltipFlag advancedTooltips) {
		if (!ModConfig.USE_OLD_INFINTY_MECHANIC) {
			String shift = I18n.format("tooltip.press_shift.desc").replace("Shift", TextFormatting.YELLOW + "" + TextFormatting.BOLD + "" + TextFormatting.ITALIC + "Shift" + TextFormatting.GRAY);
			if (isShiftKeyDown() || advancedTooltips == TooltipFlags.ADVANCED) {
				list.addAll(Lists.<String>newArrayList(TextFormatting.AQUA + "==============================", "Place up to 2.14B of these into", "the Infinity Booster Card slot", "of a Wireless Crafting Terminal", "to add to the amount of Infinity", "Energy stored. Infinity Energy allows", "one to use their Wireless Crafting Terminal", "Beyond the range limit of a WAP and", "across dimensions."));
			}
			else {
				list.add(shift);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static boolean isShiftKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}

}
