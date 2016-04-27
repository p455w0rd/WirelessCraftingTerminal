package net.p455w0rd.wirelesscraftingterminal.api.recipes.game;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.p455w0rd.wirelesscraftingterminal.api.exceptions.MissingIngredientError;
import net.p455w0rd.wirelesscraftingterminal.api.exceptions.RegistrationError;
import net.p455w0rd.wirelesscraftingterminal.api.recipes.IIngredient;

import java.util.ArrayList;

public class ShapelessRecipe implements IRecipe, IRecipeBakeable
{

	private final ArrayList<Object> input = new ArrayList<Object>();
	private ItemStack output = null;
	private boolean disable = false;

	public ShapelessRecipe( final ItemStack result, final Object... recipe )
	{
		this.output = result.copy();
		for( final Object in : recipe )
		{
			if( in instanceof IIngredient )
			{
				this.input.add( in );
			}
			else
			{
				final StringBuilder ret = new StringBuilder( "Invalid shapeless ore recipe: " );
				for( final Object tmp : recipe )
				{
					ret.append( tmp ).append( ", " );
				}
				ret.append( this.output );
				throw new IllegalArgumentException( ret.toString() );
			}
		}
	}

	public boolean isEnabled()
	{
		return !this.disable;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean matches( final InventoryCrafting var1, final World world )
	{
		if( this.disable )
		{
			return false;
		}

		final ArrayList<Object> required = new ArrayList<Object>( this.input );

		for( int x = 0; x < var1.getSizeInventory(); x++ )
		{
			final ItemStack slot = var1.getStackInSlot( x );

			if( slot != null )
			{
				boolean inRecipe = false;

				for( final Object next : required )
				{
					boolean match = false;

					if( next instanceof IIngredient )
					{
						try
						{
							for( final ItemStack item : ( (IIngredient) next ).getItemStackSet() )
							{
								match = match || this.checkItemEquals( item, slot );
							}
						}
						catch( final RegistrationError e )
						{
							// :P
						}
						catch( final MissingIngredientError e )
						{
							// :P
						}
					}

					if( match )
					{
						inRecipe = true;
						required.remove( next );
						break;
					}
				}

				if( !inRecipe )
				{
					return false;
				}
			}
		}

		return required.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult( final InventoryCrafting var1 )
	{
		return this.output.copy();
	}

	@Override
	public int getRecipeSize()
	{
		return this.input.size();
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return this.output;
	}

	private boolean checkItemEquals( final ItemStack target, final ItemStack input )
	{
		return( target.getItem() == input.getItem() && ( target.getItemDamage() == OreDictionary.WILDCARD_VALUE || target.getItemDamage() == input.getItemDamage() ) );
	}

	/**
	 * Returns the input for this recipe, any mod accessing this value should never manipulate the values in this array
	 * as it will effect the recipe itself.
	 *
	 * @return The recipes input vales.
	 */
	public ArrayList<Object> getInput()
	{
		return this.input;
	}

	@Override
	public void bake() throws RegistrationError
	{
		try
		{
			this.disable = false;
			for( final Object o : this.input )
			{
				if( o instanceof IIngredient )
				{
					( (IIngredient) o ).bake();
				}
			}
		}
		catch( final MissingIngredientError e )
		{
			this.disable = true;
		}
	}
}