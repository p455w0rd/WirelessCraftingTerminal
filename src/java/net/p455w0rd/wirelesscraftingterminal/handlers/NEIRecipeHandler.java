package net.p455w0rd.wirelesscraftingterminal.handlers;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class NEIRecipeHandler extends TemplateRecipeHandler {
	
	public static NEIRecipeHandler INSTANCE;
	
	public NEIRecipeHandler() {
		INSTANCE = this;
	}

	@Override
	public String getRecipeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGuiTexture() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void removeCachedRecipe(IRecipe r) {
		ItemStack recipeOutput = null;
		if (r instanceof ShapedOreRecipe) {
			ShapedOreRecipe recipe = (ShapedOreRecipe) r;
			recipeOutput = recipe.getRecipeOutput();
		}
		System.out.println("test");
		if (recipeOutput == null) { return; }
		for (int i = 0; i < arecipes.size(); i++) {
			PositionedStack cachedResult = getResultStack(i);
			PositionedStack recipeResult = new PositionedStack(recipeOutput, 119, 24);
			if (cachedResult == recipeResult) {
				System.out.println("match!");
			}
		}
	}

}
