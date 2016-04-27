package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;

public class SlotMagnet extends AppEngSlot {
	
	public SlotMagnet(IInventory inv, int xPos, int yPos) {
		super(inv, 0, xPos, yPos);
	}

	@Override
	public boolean isItemValid(ItemStack is) {
		return ((is != null) && (is.getItem() instanceof ItemMagnet));
	}

}
