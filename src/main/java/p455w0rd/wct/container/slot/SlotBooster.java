package p455w0rd.wct.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.*;
import p455w0rd.wct.Globals;
import p455w0rd.wct.items.ItemInfinityBooster;

public class SlotBooster extends AppEngSlot {

	public SlotBooster(IInventory inv, int index, int xPos, int yPos) {
		super(inv, index, xPos, yPos);
	}

	@Override
	public boolean isItemValid(ItemStack is) {
		if (is != null) {
			if (is.getItem() instanceof ItemInfinityBooster) {
				return true;
			}
		}
		// Everything returns false except an instance of our Item
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getBackgroundLocation() {
		return new ResourceLocation(Globals.MODID, "textures/gui/booster_slot.png");
	}

}
