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
package p455w0rd.wct.container.slot;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author p455w0rd
 *
 */
public class SlotSingleItem extends Slot {

	private final Slot delegate;

	public SlotSingleItem(Slot delegate) {
		super(delegate.inventory, delegate.getSlotIndex(), delegate.xPos, delegate.yPos);
		this.delegate = delegate;
	}

	@Nullable
	@Override
	public ItemStack getStack() {
		ItemStack orgStack = delegate.getStack();
		if (orgStack != null) {
			ItemStack modifiedStack = orgStack.copy();
			modifiedStack.stackSize = 1;
			return modifiedStack;
		}

		return null;
	}

	@Override
	public boolean getHasStack() {
		return delegate.getHasStack();
	}

	@Override
	public boolean isHere(IInventory inv, int slotIn) {
		return delegate.isHere(inv, slotIn);
	}

	@Override
	public int getSlotStackLimit() {
		return delegate.getSlotStackLimit();
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return delegate.getItemStackLimit(stack);
	}

	@Override
	@Nullable
	@SideOnly(Side.CLIENT)
	public String getSlotTexture() {
		return delegate.getSlotTexture();
	}

	@Override
	public boolean canTakeStack(EntityPlayer playerIn) {
		return delegate.canTakeStack(playerIn);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canBeHovered() {
		return delegate.canBeHovered();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getBackgroundLocation() {
		return delegate.getBackgroundLocation();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBackgroundSprite() {
		return delegate.getBackgroundSprite();
	}

	@Override
	public int getSlotIndex() {
		return delegate.getSlotIndex();
	}

	@Override
	public boolean isSameInventory(Slot other) {
		return delegate.isSameInventory(other);
	}
}