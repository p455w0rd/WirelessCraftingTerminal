package p455w0rd.wct.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.init.ModGlobals;
import p455w0rd.wct.init.ModItems;

public class SlotBooster extends AppEngSlot {

	public SlotBooster(IItemHandler inv, int xPos, int yPos) {
		super(inv, 0, xPos, yPos);
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValid(ItemStack is) {
		return !is.isEmpty() && (is.getItem() == ModItems.BOOSTER_CARD);
	}

	@Override
	public boolean canTakeStack(final EntityPlayer player) {
		return player.capabilities.isCreativeMode;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getBackgroundLocation() {
		return new ResourceLocation(ModGlobals.MODID, "textures/gui/booster_slot.png");
	}

}
