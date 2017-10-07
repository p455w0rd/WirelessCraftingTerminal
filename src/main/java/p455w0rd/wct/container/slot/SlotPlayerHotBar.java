package p455w0rd.wct.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.init.ModGlobals.Mods;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.integration.Baubles;

public class SlotPlayerHotBar extends AppEngSlot {

	public SlotPlayerHotBar(final IItemHandler inv, final int par2, final int par3, final int par4) {
		super(inv, par2, par3, par4);
		this.setPlayerSide(true);
	}

	@Override
	public boolean canTakeStack(final EntityPlayer player) {
		if (Mods.BAUBLES.isLoaded()) {
			if (!Baubles.getWCTBauble(player).isEmpty()) {
				return true;
			}
			if (!getStack().isEmpty() && getStack().getItem() == ModItems.WCT) {
				return super.canTakeStack(player);
			}
		}
		else {
			if (!getStack().isEmpty() && getStack().getItem() == ModItems.WCT) {
				return true;
			}
		}
		return true;
	}

}
