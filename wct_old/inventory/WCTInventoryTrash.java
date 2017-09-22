package p455w0rd.wct.inventory;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class WCTInventoryTrash extends AppEngInternalInventory {

	public WCTInventoryTrash(Container container, ItemStack stack) {
		super((IAEAppEngInventory) container, 1);
		setEnableClientEvents(true);
		setTileEntity((IAEAppEngInventory) container);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return true;
	}

}
