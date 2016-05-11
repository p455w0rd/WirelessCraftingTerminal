package net.p455w0rd.wirelesscraftingterminal.handlers;

import java.util.ArrayList;
import java.util.Iterator;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class RecipeHandler {

	public static ArrayList<IRecipe> easyRecipeList = new ArrayList<IRecipe>();
	public static ArrayList<IRecipe> hardRecipeList = new ArrayList<IRecipe>();

	public static void init() {
		Item multiMaterial = GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial");
		Item multiPart = GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart");
		Item itemWAP = GameRegistry.findItem("appliedenergistics2", "tile.BlockWireless");
		Item ae2WirelessTerm = GameRegistry.findItem("appliedenergistics2", "item.ToolWirelessTerminal");
		Item blockSpatialIO = GameRegistry.findItem("appliedenergistics2", "tile.BlockSpatialPylon");
		Item blockCraftingStorage = GameRegistry.findItem("appliedenergistics2", "tile.BlockCraftingStorage");

		//EASY MODE RECIPES

		// Wireless Crafting Terminal
		easyRecipeList.add(new ShapedOreRecipe(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getStack(), new Object[] { "a", "b", "c", Character.valueOf('a'), new ItemStack(multiMaterial, 1, 41), Character.valueOf('b'), new ItemStack(multiPart, 1, 360), Character.valueOf('c'), new ItemStack(ae2WirelessTerm, 1, 0) }));

		// Magnet Card
		easyRecipeList.add(new ShapedOreRecipe(ItemEnum.MAGNET_CARD.getStack(), new Object[] {
				"a b", "cdc", "ccc",
				Character.valueOf('a'), "dustRedstone",
				Character.valueOf('b'), "gemLapis",
				Character.valueOf('c'), "ingotIron",
				Character.valueOf('d'), new ItemStack(multiMaterial, 1, 28) }));
						

		// Infinity Booster Card
		easyRecipeList.add(new ShapedOreRecipe(ItemEnum.BOOSTER_CARD.getStack(), new Object[] { "abc", "ded", "cba", Character.valueOf('a'), new ItemStack(blockSpatialIO, 1, 0), Character.valueOf('b'), new ItemStack(multiMaterial, 1, 42), Character.valueOf('c'), new ItemStack(blockCraftingStorage, 1, 3), Character.valueOf('d'), new ItemStack(multiMaterial, 1, 48), Character.valueOf('e'), new ItemStack(itemWAP, 1, 0) }));

		//==================
		// HARD MODE RECIPES
				
		// Wireless Crafting Terminal
		hardRecipeList.add(new ShapedOreRecipe(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getStack(), new Object[] { "aba", "cdc", "efe", Character.valueOf('a'), new ItemStack(multiMaterial, 1, 9), Character.valueOf('b'), new ItemStack(itemWAP, 1, 0), Character.valueOf('c'), new ItemStack(multiMaterial, 1, 12), Character.valueOf('d'), new ItemStack(multiPart, 1, 360), Character.valueOf('e'), new ItemStack(multiMaterial, 1, 38), Character.valueOf('f'), new ItemStack(multiMaterial, 1, 47) }));

		// Magnet Card
		hardRecipeList.add(new ShapedOreRecipe(ItemEnum.MAGNET_CARD.getStack(), new Object[] {
				"abc", "ded", "ddd",
				Character.valueOf('a'), "blockRedstone",
				Character.valueOf('b'), new ItemStack(multiPart, 1, 300),
				Character.valueOf('c'), "blockLapis",
				Character.valueOf('d'), "blockIron",
				Character.valueOf('e'), new ItemStack(multiMaterial, 1, 28) }));

	}

	@SuppressWarnings("unchecked")
	public static void loadRecipes(boolean mtChanged) {
		if (Reference.WCT_MINETWEAKER_OVERRIDE && !mtChanged) {
			return;
		}
		ArrayList<IRecipe> recipeList = (Reference.WCT_EASYMODE_ENABLED ? easyRecipeList : hardRecipeList);
		ArrayList<IRecipe> recipeList2 = (!Reference.WCT_EASYMODE_ENABLED ? easyRecipeList : hardRecipeList);

		WCTLog.info("Reinitializing Recipes");
		if (recipeList.size() <= 0 || recipeList2.size() <= 0) {
			init();
		}
		
		// remove old recipes
		Iterator<IRecipe> iterator = CraftingManager.getInstance().getRecipeList().iterator();
		while (iterator.hasNext()) {
			ItemStack r = iterator.next().getRecipeOutput();
			for (IRecipe recipe : recipeList2) {
				ItemStack recipeOutput = recipe.getRecipeOutput();
				if (r == null) {
					continue;
				}
				if (r.getItem() == recipeOutput.getItem()) {
					iterator.remove();
					WCTLog.info("Removed Recipe for " + r.getDisplayName());
					continue;
				}
			}
		}
		
		// add new recipes
		for (IRecipe recipe : recipeList) {
			if (Reference.WCT_MINETWEAKER_OVERRIDE) {
				return;
			}
			GameRegistry.addRecipe(recipe);
			WCTLog.info("Added Recipe for " + recipe.getRecipeOutput().getDisplayName());
		}
	}
}
