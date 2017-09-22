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
package p455w0rd.wct.integration;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.util.Platform;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.Loader;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.slot.SlotCraftingMatrix;
import p455w0rd.wct.container.slot.SlotFakeCraftingMatrix;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketJEIRecipe;

/**
 * @author p455w0rd
 *
 */
@JEIPlugin
public class JEI implements IModPlugin {

	public static final String MODID = "jei";

	@Override
	public void register(@Nonnull IModRegistry registry) {
		IJeiHelpers helpers = registry.getJeiHelpers();
		IIngredientBlacklist blackList = helpers.getIngredientBlacklist();
		if (!ModConfig.WCT_BOOSTER_ENABLED) {
			blackList.addIngredientToBlacklist(new ItemStack(ModItems.BOOSTER_CARD));
		}
		registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RecipeTransferHandler<ContainerWCT>(ContainerWCT.class), VanillaRecipeCategoryUid.CRAFTING);
		registry.addDescription(new ItemStack(ModItems.WCT), "jei.test.desc");
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
	}

	public static boolean isLoaded() {
		return Loader.isModLoaded(MODID);
	}

	public class RecipeTransferHandler<T extends Container> implements IRecipeTransferHandler<T> {

		private final Class<T> containerClass;

		RecipeTransferHandler(Class<T> containerClass) {
			this.containerClass = containerClass;
		}

		@Override
		public Class<T> getContainerClass() {
			return containerClass;
		}

		@Nullable
		@Override
		public IRecipeTransferError transferRecipe(T container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {

			if (!doTransfer) {
				return null;
			}

			Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();

			final NBTTagCompound recipe = new NBTTagCompound();

			int slotIndex = 0;
			for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingredientEntry : ingredients.entrySet()) {
				IGuiIngredient<ItemStack> ingredient = ingredientEntry.getValue();
				if (!ingredient.isInput()) {
					continue;
				}

				for (final Slot slot : container.inventorySlots) {
					if (slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix || slot.inventory instanceof InventoryCrafting) {
						if (slot.getSlotIndex() == slotIndex) {
							final NBTTagList tags = new NBTTagList();
							final List<ItemStack> list = new LinkedList<ItemStack>();

							// prefer pure crystals.
							for (ItemStack stack : ingredient.getAllIngredients()) {
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
				NetworkHandler.instance().sendToServer(new PacketJEIRecipe(recipe));
			}
			catch (IOException e) {

			}

			return null;
		}
	}

}
