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

	public static ArrayList<IRecipe> recipeList = new ArrayList<IRecipe>();

	public static void init() {
		Item multiMaterial = GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial");
		Item multiPart = GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart");
		Item itemWAP = GameRegistry.findItem("appliedenergistics2", "tile.BlockWireless");
		Item ae2WirelessTerm = GameRegistry.findItem("appliedenergistics2", "item.ToolWirelessTerminal");
		Item blockSpatialIO = GameRegistry.findItem("appliedenergistics2", "tile.BlockSpatialPylon");
		Item blockCraftingStorage = GameRegistry.findItem("appliedenergistics2", "tile.BlockCraftingStorage");

		// Is easy mode enabled?
		if (Reference.WCT_EASYMODE_ENABLED) {

			// Wireless Crafting Terminal
			recipeList.add(new ShapedOreRecipe(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getStack(), new Object[] { "a", "b", "c", Character.valueOf('a'), new ItemStack(multiMaterial, 1, 41), Character.valueOf('b'), new ItemStack(multiPart, 1, 360), Character.valueOf('c'), new ItemStack(ae2WirelessTerm, 1, 0) }));

			// Magnet Card
			recipeList.add(new ShapedOreRecipe(ItemEnum.MAGNET_CARD.getStack(), new Object[] {
					"a b", "cdc", "ccc",
					Character.valueOf('a'), "dustRedstone",
					Character.valueOf('b'), "gemLapis",
					Character.valueOf('c'), "ingotIron",
					Character.valueOf('d'), new ItemStack(multiMaterial, 1, 28) }));
						
			// Is Booster Card Enabled?
			if (Reference.WCT_BOOSTER_ENABLED) {
				// Infinity Booster Card
				recipeList.add(new ShapedOreRecipe(ItemEnum.BOOSTER_CARD.getStack(), new Object[] { "abc", "ded", "cba", Character.valueOf('a'), new ItemStack(blockSpatialIO, 1, 0), Character.valueOf('b'), new ItemStack(multiMaterial, 1, 42), Character.valueOf('c'), new ItemStack(blockCraftingStorage, 1, 3), Character.valueOf('d'), new ItemStack(multiMaterial, 1, 48), Character.valueOf('e'), new ItemStack(itemWAP, 1, 0) }));
			}
		}
		else {
			// Wireless Crafting Terminal
			recipeList.add(new ShapedOreRecipe(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getStack(), new Object[] { "aba", "cdc", "efe", Character.valueOf('a'), new ItemStack(multiMaterial, 1, 9), Character.valueOf('b'), new ItemStack(itemWAP, 1, 0), Character.valueOf('c'), new ItemStack(multiMaterial, 1, 12), Character.valueOf('d'), new ItemStack(multiPart, 1, 360), Character.valueOf('e'), new ItemStack(multiMaterial, 1, 38), Character.valueOf('f'), new ItemStack(multiMaterial, 1, 47) }));

			// Magnet Card
			recipeList.add(new ShapedOreRecipe(ItemEnum.MAGNET_CARD.getStack(), new Object[] {
					"abc", "ded", "ddd",
					Character.valueOf('a'), "blockRedstone",
					Character.valueOf('b'), new ItemStack(multiPart, 1, 301),
					Character.valueOf('c'), "blockLapis",
					Character.valueOf('d'), "blockIron",
					Character.valueOf('e'), new ItemStack(multiMaterial, 1, 28) }));
		}

	}

	public static void addRecipes() {
		addRecipes(false);
	}

	public static void addRecipes(boolean init) {
		if (init) {
			init();
		}
		if (recipeList.size() == 0) {
			return;
		}
		for (IRecipe recipe : recipeList) {
			GameRegistry.addRecipe(recipe);
			WCTLog.info("Added Recipe for " + recipe.getRecipeOutput().getDisplayName());
		}
		recipeList.clear();
		init();
	}

	@SuppressWarnings("unchecked")
	public static void removeRecipes() {
		if (recipeList.size() == 0) {
			return;
		}
		Iterator<IRecipe> iterator = CraftingManager.getInstance().getRecipeList().iterator();
		while (iterator.hasNext()) {
			ItemStack r = iterator.next().getRecipeOutput();
			for (IRecipe recipe : recipeList) {
				ItemStack recipeOutput = recipe.getRecipeOutput();
				if (r != null && r.getItem() == recipeOutput.getItem()) {
					iterator.remove();
					WCTLog.info("Removed Recipe for " + r.getDisplayName());
				}
			}
		}
		recipeList.clear();
		init();
	}
}
