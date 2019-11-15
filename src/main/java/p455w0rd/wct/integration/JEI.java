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
package p455w0rd.wct.integration;

import java.io.IOException;
import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.util.Platform;
import mezz.jei.api.*;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketJEIRecipe;
import p455w0rdslib.LibGlobals.Mods;

/**
 * @author p455w0rd
 *
 */
@JEIPlugin
public class JEI implements IModPlugin {

	@Override
	public void register(@Nonnull final IModRegistry registry) {
		final IJeiHelpers helpers = registry.getJeiHelpers();
		registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RecipeTransferHandler<>(ContainerWCT.class), VanillaRecipeCategoryUid.CRAFTING);
		final String wctBaublesDescKey = Mods.BAUBLES.isLoaded() ? "jei.wt_bauble.desc" : "";
		registry.addIngredientInfo(Lists.newArrayList(new ItemStack(ModItems.WCT)), VanillaTypes.ITEM, "jei.wct.desc", wctBaublesDescKey);
		registry.addIngredientInfo(Lists.newArrayList(new ItemStack(ModItems.MAGNET_CARD)), VanillaTypes.ITEM, "jei.magnet_card.desc");
	}

	@Override
	public void onRuntimeAvailable(final IJeiRuntime jeiRuntime) {
	}

	@Override
	public void registerIngredients(final IModIngredientRegistration registry) {
	}

	@Override
	public void registerItemSubtypes(final ISubtypeRegistry subtypeRegistry) {
	}

	public class RecipeTransferHandler<T extends Container> implements IRecipeTransferHandler<T> {

		private final Class<T> containerClass;

		RecipeTransferHandler(final Class<T> containerClass) {
			this.containerClass = containerClass;
		}

		@Override
		public Class<T> getContainerClass() {
			return containerClass;
		}

		@Nullable
		@Override
		public IRecipeTransferError transferRecipe(final T container, final IRecipeLayout recipeLayout, final EntityPlayer player, final boolean maxTransfer, final boolean doTransfer) {

			if (!doTransfer) {
				return null;
			}

			final Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();

			final NBTTagCompound recipe = new NBTTagCompound();

			int slotIndex = 0;
			for (final Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingredientEntry : ingredients.entrySet()) {
				final IGuiIngredient<ItemStack> ingredient = ingredientEntry.getValue();
				if (!ingredient.isInput()) {
					continue;
				}

				for (final Slot slot : container.inventorySlots) {
					if (slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix) {
						if (slot.getSlotIndex() == slotIndex) {
							final NBTTagList tags = new NBTTagList();
							final List<ItemStack> list = new LinkedList<>();
							final ItemStack displayed = ingredient.getDisplayedIngredient();

							// prefer currently displayed item
							if (displayed != null && !displayed.isEmpty()) {
								list.add(displayed);
							}

							for (final ItemStack stack : ingredient.getAllIngredients()) {
								if (Platform.isRecipePrioritized(stack)) {
									list.add(0, stack);
								}
								else {
									list.add(stack);
								}
							}

							for (final ItemStack is : list) {
								final NBTTagCompound tag = new NBTTagCompound();
								is.writeToNBT(tag);
								tags.appendTag(tag);
							}

							recipe.setTag("#" + slot.getSlotIndex(), tags);
							break;
						}
					}
				}

				slotIndex++;

			}

			try {
				ModNetworking.instance().sendToServer(new PacketJEIRecipe(recipe));
			}
			catch (final IOException e) {

			}

			return null;
		}
	}

}
