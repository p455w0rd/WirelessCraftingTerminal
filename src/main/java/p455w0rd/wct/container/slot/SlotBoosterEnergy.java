/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.container.slot;

import javax.annotation.Nonnull;

import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.init.ModGlobals;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketSyncInfinityEnergy;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class SlotBoosterEnergy extends AppEngSlot {

	private AEBaseContainer thisContainer;

	public SlotBoosterEnergy(int xPos, int yPos) {
		super(null, 0, xPos, yPos);
	}

	@Override
	public int getSlotStackLimit() {
		return 64;
	}

	@Override
	public boolean isItemValid(ItemStack is) {
		return !is.isEmpty() && (is.getItem() == ModItems.BOOSTER_CARD);
	}

	@Override
	public boolean canTakeStack(final EntityPlayer player) {
		return false;
	}

	@Override
	@Nonnull
	public ItemStack getStack() {
		return ItemStack.EMPTY;
	}

	@Override
	public void setContainer(final AEBaseContainer myContainer) {
		thisContainer = myContainer;
		super.setContainer(myContainer);
	}

	@Override
	public void putStack(final ItemStack stack) {
		if (thisContainer != null && (thisContainer instanceof ContainerWCT)) {
			ContainerWCT c = (ContainerWCT) thisContainer;
			WCTUtils.addInfinityBoosters(c.getWirelessTerminal(), stack);
			c.detectAndSendChanges();
			for (IContainerListener listener : c.getListeners()) {
				if (listener instanceof EntityPlayerMP) {
					ModNetworking.instance().sendTo(new PacketSyncInfinityEnergy(WCTUtils.getInfinityEnergy(((ContainerWCT) thisContainer).getWirelessTerminal())), (EntityPlayerMP) listener);
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getBackgroundLocation() {
		return new ResourceLocation(ModGlobals.MODID, "textures/gui/booster_slot.png");
	}

}
