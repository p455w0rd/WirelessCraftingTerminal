package net.p455w0rd.wirelesscraftingterminal.integration.modules.NEIHelpers;

import java.util.LinkedList;
import java.util.List;

import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotCraftingMatrix;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotFakeCraftingMatrix;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketNEIRecipe;


public class NEICraftingHandler implements IOverlayHandler
{

	public NEICraftingHandler( final int x, final int y )
	{
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void overlayRecipe( final GuiContainer gui, final IRecipeHandler recipe, final int recipeIndex, final boolean shift )
	{
		try
		{
			final List ingredients = recipe.getIngredientStacks( recipeIndex );
			this.overlayRecipe( gui, ingredients, shift );
		}
		catch( final Exception ignored )
		{
		}
		catch( final Error ignored )
		{
		}
	}

	@SuppressWarnings("unchecked")
	private void overlayRecipe( final GuiContainer gui, final List<PositionedStack> ingredients, final boolean shift )
	{
		try
		{
			final NBTTagCompound recipe = new NBTTagCompound();

			if( gui instanceof GuiWirelessCraftingTerminal )
			{
				for( final PositionedStack positionedStack : ingredients )
				{
					final int col = ( positionedStack.relx - 25 ) / 18;
					final int row = ( positionedStack.rely - 6 ) / 18;
					if( positionedStack.items != null && positionedStack.items.length > 0 )
					{
						for( final Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots )
						{
							if( slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix )
							{
								if( slot.getSlotIndex() == col + row * 3 )
								{
									final NBTTagList tags = new NBTTagList();
									final List<ItemStack> list = new LinkedList<ItemStack>();

									// prefer pure crystals.
									for( int x = 0; x < positionedStack.items.length; x++ )
									{
										if( Platform.isRecipePrioritized( positionedStack.items[x] ) )
										{
											list.add( 0, positionedStack.items[x] );
										}
										else
										{
											list.add( positionedStack.items[x] );
										}
									}

									for( final ItemStack is : list )
									{
										final NBTTagCompound tag = new NBTTagCompound();
										is.writeToNBT( tag );
										tags.appendTag( tag );
									}

									recipe.setTag( "#" + slot.getSlotIndex(), tags );
									break;
								}
							}
						}
					}
				}

				NetworkHandler.instance.sendToServer( new PacketNEIRecipe( recipe ) );
			}
		}
		catch( final Exception ignored )
		{
		}
		catch( final Error ignored )
		{
		}
	}
}
