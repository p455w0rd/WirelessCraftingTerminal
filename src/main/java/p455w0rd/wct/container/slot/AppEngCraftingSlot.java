package p455w0rd.wct.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class AppEngCraftingSlot extends AppEngSlot {

	/**
	 * The craft matrix inventory linked to this result slot.
	 */
	private final IInventory craftMatrix;

	/**
	 * The player that is using the GUI where this slot resides.
	 */
	private final EntityPlayer thePlayer;

	/**
	 * The number of items that have been crafted so far. Gets passed to ItemStack.onCrafting before being reset.
	 */
	private int amountCrafted;

	public AppEngCraftingSlot(final EntityPlayer par1EntityPlayer, final IInventory par2IInventory, final IInventory par3IInventory, final int par4, final int par5, final int par6) {
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
		par1ItemStack.onCrafting(thePlayer.worldObj, thePlayer, amountCrafted);
		amountCrafted = 0;

		if (par1ItemStack.getItem() == Item.getItemFromBlock(Blocks.CRAFTING_TABLE)) {
			thePlayer.addStat(AchievementList.BUILD_WORK_BENCH, 1);
		}

		if (par1ItemStack.getItem() instanceof ItemPickaxe) {
			thePlayer.addStat(AchievementList.BUILD_PICKAXE, 1);
		}

		if (par1ItemStack.getItem() == Item.getItemFromBlock(Blocks.FURNACE)) {
			thePlayer.addStat(AchievementList.BUILD_FURNACE, 1);
		}

		if (par1ItemStack.getItem() instanceof ItemHoe) {
			thePlayer.addStat(AchievementList.BUILD_HOE, 1);
		}

		if (par1ItemStack.getItem() == Items.BREAD) {
			thePlayer.addStat(AchievementList.MAKE_BREAD, 1);
		}

		if (par1ItemStack.getItem() == Items.CAKE) {
			thePlayer.addStat(AchievementList.BAKE_CAKE, 1);
		}

		if (par1ItemStack.getItem() instanceof ItemPickaxe && ((ItemTool) par1ItemStack.getItem()).getToolMaterial() != Item.ToolMaterial.WOOD) {
			thePlayer.addStat(AchievementList.BUILD_BETTER_PICKAXE, 1);
		}

		if (par1ItemStack.getItem() instanceof ItemSword) {
			thePlayer.addStat(AchievementList.BUILD_SWORD, 1);
		}

		if (par1ItemStack.getItem() == Item.getItemFromBlock(Blocks.ENCHANTING_TABLE)) {
			thePlayer.addStat(AchievementList.ENCHANTMENTS, 1);
		}

		if (par1ItemStack.getItem() == Item.getItemFromBlock(Blocks.BOOKSHELF)) {
			thePlayer.addStat(AchievementList.BOOKCASE, 1);
		}
	}

	@Override
	public void onPickupFromSlot(final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack) {
		FMLCommonHandler.instance().firePlayerCraftingEvent(par1EntityPlayer, par2ItemStack, craftMatrix);
		this.onCrafting(par2ItemStack);

		for (int i = 0; i < craftMatrix.getSizeInventory(); ++i) {
			final ItemStack itemstack1 = craftMatrix.getStackInSlot(i);

			if (itemstack1 != null) {
				craftMatrix.decrStackSize(i, 1);

				if (itemstack1.getItem().hasContainerItem(itemstack1)) {
					final ItemStack itemstack2 = itemstack1.getItem().getContainerItem(itemstack1);

					if (itemstack2 != null && itemstack2.isItemStackDamageable() && itemstack2.getItemDamage() > itemstack2.getMaxDamage()) {
						MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(thePlayer, itemstack2, null));
						continue;
					}

					if (!itemstack1.getItem().hasContainerItem(itemstack1) || !thePlayer.inventory.addItemStackToInventory(itemstack2)) {
						if (craftMatrix.getStackInSlot(i) == null) {
							craftMatrix.setInventorySlotContents(i, itemstack2);
						}
						else {
							thePlayer.dropItem(itemstack2, false);
						}
					}
				}
			}
		}
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
	 * stack.
	 */
	@Override
	public ItemStack decrStackSize(final int par1) {
		if (getHasStack()) {
			amountCrafted += Math.min(par1, getStack().stackSize);
		}

		return super.decrStackSize(par1);
	}
}