package net.p455w0rd.wirelesscraftingterminal.integration.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.integration.modules.NEIHelpers.NEIAEShapelessRecipeHandler;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotCraftingMatrix;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotFakeCraftingMatrix;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketNEIRecipe;
import net.p455w0rd.wirelesscraftingterminal.helpers.Reflected;
import net.p455w0rd.wirelesscraftingterminal.integration.IIntegrationModule;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationHelper;
import net.p455w0rd.wirelesscraftingterminal.integration.abstraction.INEI;
import net.p455w0rd.wirelesscraftingterminal.integration.modules.NEIHelpers.NEIAEShapedRecipeHandler;

public class NEI implements INEI, IContainerTooltipHandler, IIntegrationModule {
	@Reflected
	public static NEI instance;

	@SuppressWarnings("unused")
	private final Class<?> apiClass;

	@Reflected
	public NEI() throws ClassNotFoundException {
		IntegrationHelper.testClassExistence(this, codechicken.nei.api.API.class);
		IntegrationHelper.testClassExistence(this, codechicken.nei.api.IStackPositioner.class);
		IntegrationHelper.testClassExistence(this, codechicken.nei.guihook.GuiContainerManager.class);
		IntegrationHelper.testClassExistence(this, codechicken.nei.PositionedStack.class);
		IntegrationHelper.testClassExistence(this, codechicken.nei.recipe.IRecipeHandler.class);
		this.apiClass = Class.forName("codechicken.nei.api.API");
	}

	@Override
	public void init() throws Throwable {
		// large stack tooltips
		GuiContainerManager.addTooltipHandler( this );

		// wireless crafting terminal...
		
		API.registerGuiOverlay(net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal.class, "crafting", new WCTSlotPositioner());
		API.registerGuiOverlayHandler(net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal.class, new WCTOverlayHandler(), "crafting");
		API.registerRecipeHandler(new NEIAEShapedRecipeHandler());
		API.registerRecipeHandler( new NEIAEShapelessRecipeHandler() );
		
		/*
		final Method registerGuiOverlay = this.apiClass.getDeclaredMethod("registerGuiOverlay", Class.class, String.class, IStackPositioner.class);
		registerGuiOverlay.invoke(this.apiClass, net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal.class, "crafting",
				new WCTSlotPositioner());
		
		final Class overlayHandler = Class.forName("codechicken.nei.api.IOverlayHandler");
		WCTOverlayHandler craftingOverlayHandler = new WCTOverlayHandler();
		final Method registrar = this.apiClass.getDeclaredMethod("registerGuiOverlayHandler", Class.class, overlayHandler, String.class);
		registrar.invoke(this.apiClass, net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal.class,
				craftingOverlayHandler, "crafting");
		*/
		

		// final Class<NEICraftingHandler> defaultHandler =
		// NEICraftingHandler.class;
		// final Constructor defaultConstructor = defaultHandler.getConstructor(
		// int.class, int.class );
		
	}

	@Override
	public void postInit() {

	}

	@Override
	public void drawSlot(final Slot s) {
		if (s == null) {
			return;
		}

		final ItemStack stack = s.getStack();

		if (stack == null) {
			return;
		}

		final Minecraft mc = Minecraft.getMinecraft();
		final FontRenderer fontRenderer = mc.fontRenderer;
		final int x = s.xDisplayPosition;
		final int y = s.yDisplayPosition;

		GuiContainerManager.drawItems.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), stack, x, y);
		GuiContainerManager.drawItems.renderItemOverlayIntoGUI(fontRenderer, mc.getTextureManager(), stack, x, y,
				String.valueOf(stack.stackSize));
	}

	@Override
	public RenderItem setItemRender(final RenderItem renderItem) {
		try {
			final RenderItem ri = GuiContainerManager.drawItems;
			GuiContainerManager.drawItems = renderItem;
			return ri;
		} catch (final Throwable t) {
			throw new IllegalStateException("Invalid version of NEI, please update", t);
		}
	}

	@Override
	public List<String> handleTooltip(final GuiContainer arg0, final int arg1, final int arg2,
			final List<String> current) {
		return current;
	}

	@Override
	public List<String> handleItemDisplayName(final GuiContainer arg0, final ItemStack arg1,
			final List<String> current) {
		return current;
	}

	@Override
	public List<String> handleItemTooltip(final GuiContainer guiScreen, final ItemStack stack, final int mouseX,
			final int mouseY, final List<String> currentToolTip) {
		if (guiScreen instanceof GuiWirelessCraftingTerminal) {
			return ((GuiWirelessCraftingTerminal) guiScreen).handleItemTooltip(stack, mouseX, mouseY, currentToolTip);
		}

		return currentToolTip;
	}

	static final int NEI_REGULAR_SLOT_OFFSET_X = 25;
	static final int NEI_REGULAR_SLOT_OFFSET_Y = 6;

	public class WCTSlotPositioner implements IStackPositioner {

		@Override
		public ArrayList<PositionedStack> positionStacks(final ArrayList<PositionedStack> stacks) {
			// Adjust the position of the ghost stacks to match the crafting
			// grid slots

			for (final PositionedStack positionedStack : stacks) {
				if (positionedStack.items != null && positionedStack.items.length > 0) {
					positionedStack.relx += ContainerWirelessCraftingTerminal.CRAFTING_SLOT_X_POS
							- NEI.NEI_REGULAR_SLOT_OFFSET_X;
					positionedStack.rely += ContainerWirelessCraftingTerminal.CRAFTING_SLOT_Y_POS
							- NEI.NEI_REGULAR_SLOT_OFFSET_Y;
				}
			}
			return stacks;
		}
	}

	public class WCTOverlayHandler implements IOverlayHandler {
		/**
		 * Reduces regular slot offsets to 0, 1, or 2
		 */
		private static final int REGULAR_SLOT_INDEX_DIVISOR = 18;

		/**
		 * Adds NEI ingredients to the WCT crafting grid.
		 * 
		 * @param ingredient
		 * @param overlayItems
		 * @return
		 */
		private boolean addCraftingItems(final PositionedStack ingredient, final IAEItemStack[] overlayItems) {
			// Calculate the slot positions
			int slotX = (ingredient.relx - NEI.NEI_REGULAR_SLOT_OFFSET_X)
					/ WCTOverlayHandler.REGULAR_SLOT_INDEX_DIVISOR;
			int slotY = (ingredient.rely - NEI.NEI_REGULAR_SLOT_OFFSET_Y)
					/ WCTOverlayHandler.REGULAR_SLOT_INDEX_DIVISOR;
			// Calculate the slot index
			int slotIndex = slotX + (slotY * 3);
			// Add the item to the list
			overlayItems[slotIndex] = AEApi.instance().storage().createItemStack(ingredient.item);
			return true;
		}

		protected boolean addIngredientToItems(final PositionedStack ingredient, final IAEItemStack[] overlayItems) {
			// Pass to regular handler
			return this.addCraftingItems(ingredient, overlayItems);
		}

		protected void addItemsToGUI(final IAEItemStack[] overlayItems) {
			// Send the list to the server
			// Packet_S_WCT.sendSetCrafting_NEI(Minecraft.getMinecraft().thePlayer,
			// overlayItems);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
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

										// prefer regular crystals.
										for( int x = 0; x < positionedStack.items.length; x++ )
										{
											if( !Platform.isRecipePrioritized( positionedStack.items[x] ) )
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
}
