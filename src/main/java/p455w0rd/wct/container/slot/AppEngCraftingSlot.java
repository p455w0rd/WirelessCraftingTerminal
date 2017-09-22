package p455w0rd.wct.container.slot;

import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperInvItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.util.WCTUtils;

public class AppEngCraftingSlot extends AppEngSlot {

	/**
	 * The craft matrix inventory linked to this result slot.
	 */
	private final IItemHandler craftMatrix;

	/**
	 * The player that is using the GUI where this slot resides.
	 */
	private final EntityPlayer thePlayer;

	/**
	 * The number of items that have been crafted so far. Gets passed to ItemStack.onCrafting before being reset.
	 */
	private int amountCrafted;

	public AppEngCraftingSlot(final EntityPlayer par1EntityPlayer, final IItemHandler par2IInventory, final IItemHandler par3IInventory, final int par4, final int par5, final int par6) {
		super(par3IInventory, par4, par5, par6);
		thePlayer = par1EntityPlayer;
		craftMatrix = par2IInventory;
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	 */
	@Override
	public boolean isItemValid(final ItemStack par1ItemStack) {
		return false;
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
	 * internal count then calls onCrafting(item).
	 */
	@Override
	protected void onCrafting(final ItemStack par1ItemStack, final int par2) {
		amountCrafted += par2;
		this.onCrafting(par1ItemStack);
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
	 */
	@Override
	protected void onCrafting(final ItemStack par1ItemStack) {
		par1ItemStack.onCrafting(WCTUtils.world(thePlayer), thePlayer, amountCrafted);
		amountCrafted = 0;
	}

	@Override
	public ItemStack onTake(final EntityPlayer player, final ItemStack stack) {
		net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(player, stack, new WrapperInvItemHandler(craftMatrix));
		this.onCrafting(stack);
		net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);
		final InventoryCrafting ic = new InventoryCrafting(getContainer(), 3, 3);

		for (int x = 0; x < craftMatrix.getSlots(); x++) {
			ic.setInventorySlotContents(x, craftMatrix.getStackInSlot(x));
		}

		final NonNullList<ItemStack> aitemstack = getRemainingItems(ic, player.world);

		ItemHandlerUtil.copy(ic, craftMatrix, false);

		net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

		for (int i = 0; i < aitemstack.size(); ++i) {
			final ItemStack itemstack1 = craftMatrix.getStackInSlot(i);
			final ItemStack itemstack2 = aitemstack.get(i);

			if (!itemstack1.isEmpty()) {
				craftMatrix.extractItem(i, 1, false);
			}

			if (!itemstack2.isEmpty()) {
				if (craftMatrix.getStackInSlot(i).isEmpty()) {
					ItemHandlerUtil.setStackInSlot(craftMatrix, i, itemstack2);
				}
				else if (!thePlayer.inventory.addItemStackToInventory(itemstack2)) {
					thePlayer.dropItem(itemstack2, false);
				}
			}
		}
		return stack;
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
	 * stack.
	 */
	@Override
	public ItemStack decrStackSize(final int par1) {
		if (getHasStack()) {
			amountCrafted += Math.min(par1, getStack().getCount());
		}

		return super.decrStackSize(par1);
	}

	protected NonNullList<ItemStack> getRemainingItems(InventoryCrafting ic, World world) {
		return CraftingManager.getRemainingItems(ic, world);
	}

}