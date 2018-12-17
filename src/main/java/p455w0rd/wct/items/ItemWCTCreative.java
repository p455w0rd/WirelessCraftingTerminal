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

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.init.LibConfig;
import p455w0rd.wct.init.ModGlobals;
import p455w0rd.wct.init.ModItems;

/**
 * @author p455w0rd
 *
 */
public class ItemWCTCreative extends ItemWCT {

	public ItemWCTCreative() {
		super(new ResourceLocation(ModGlobals.MODID, "wct_creative"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, getModelResource());
	}

	@Override
	public ModelResourceLocation getModelResource() {
		return new ModelResourceLocation(ModItems.WCT.getRegistryName(), "inventory");
	}

	@Override
	public double getAECurrentPower(final ItemStack wirelessTerm) {
		return LibConfig.WT_MAX_POWER;
	}

	@Override
	public EnumRarity getRarity(ItemStack wirelessTerm) {
		return EnumRarity.RARE;
	}

	@Override
	protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
		itemStacks.add(new ItemStack(this));
	}

	@Override
	public boolean isCreative() {
		return true;
	}

}
