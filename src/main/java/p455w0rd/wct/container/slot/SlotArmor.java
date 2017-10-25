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
package p455w0rd.wct.container.slot;

import appeng.container.slot.AppEngSlot;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

public class SlotArmor extends AppEngSlot {

	final EntityEquipmentSlot armorType;

	final EntityPlayer player;

	public SlotArmor(EntityPlayer player, IItemHandler inventory, int slot, int x, int y, EntityEquipmentSlot armorType) {
		super(inventory, slot, x, y);
		this.player = player;
		this.armorType = armorType;
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem().isValidArmor(stack, EntityEquipmentSlot.values()[armorType.ordinal() + 1], player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBackgroundSprite() {
		String name = ItemArmor.EMPTY_SLOT_NAMES[armorType.ordinal() - 1];
		return name == null ? null : getBackgroundMap().getAtlasSprite(name);
	}

}
