package p455w0rd.wct.inventory;

import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import p455w0rd.wct.init.ModGlobals.Mods;
import p455w0rd.wct.integration.Baubles;

/**
 * @author p455w0rd
 *
 */
public class WCTBaublesInventory implements IInventory {

	IBaublesItemHandler baubles;
	EntityPlayer player;

	public WCTBaublesInventory(EntityPlayer playerIn) {
		player = playerIn;
		baubles = Baubles.getBaubles(player);
	}

	@Override
	public String getName() {
		return Mods.BAUBLES.getId();
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	@Override
	public int getSizeInventory() {
		return 7;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return baubles.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return baubles.extractItem(index, count, false);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return baubles.extractItem(index, baubles.getStackInSlot(index).stackSize, false);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		baubles.setStackInSlot(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public void markDirty() {
		for (int i = 0; i < getSizeInventory(); i++) {
			baubles.setChanged(i, true);
		}
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return baubles.isItemValidForSlot(index, stack, player);
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		for (int i = 0; i < getSizeInventory(); i++) {
			baubles.setStackInSlot(i, null);
		}
	}

}
