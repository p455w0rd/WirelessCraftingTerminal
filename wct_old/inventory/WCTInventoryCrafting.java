package p455w0rd.wct.inventory;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class WCTInventoryCrafting extends AppEngInternalInventory {

	public WCTInventoryCrafting(Container container, int rows, int cols, ItemStack is) {
		super((IAEAppEngInventory) container, rows * cols);
		setTileEntity((IAEAppEngInventory) container);
		setEnableClientEvents(true);
	}

}
