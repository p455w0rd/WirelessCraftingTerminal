package p455w0rd.wct.integration;

import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import baubles.common.container.SlotBauble;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.container.ContainerWCT;

/**
 * @author p455w0rd
 *
 */
public class Baubles {

	public static final String MODID = "Baubles";
	public static final String API_MODID = "Baubles|API";

	public static boolean isLoaded() {
		return Loader.isModLoaded(MODID);
	}

	public static ItemStack getWCTBauble(EntityPlayer player) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
			for (int i = 0; i < baubles.getSlots(); i++) {
				if (baubles.getStackInSlot(i) == null) {
					continue;
				}
				if (baubles.getStackInSlot(i).getItem() instanceof IWirelessCraftingTerminalItem) {
					return baubles.getStackInSlot(i);
				}
			}
		}
		return null;
	}

	public static int getWCTBaubleSlotIndex(EntityPlayer player) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
			for (int i = 0; i < baubles.getSlots(); i++) {
				if (baubles.getStackInSlot(i) == null) {
					continue;
				}
				if (baubles.getStackInSlot(i).getItem() instanceof IWirelessCraftingTerminalItem) {
					return i;
				}
			}
		}
		return -1;
	}

	public static void setBaublesItemStack(EntityPlayer player, int slot, ItemStack stack) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
			baubles.setStackInSlot(slot, stack);
		}
	}

	public static void addBaubleSlots(ContainerWCT container, EntityPlayer player) {
		IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
		container.addSlotToContainer(new SlotBauble(player, baubles, 0, 178, 97));
		container.addSlotToContainer(new SlotBauble(player, baubles, 1, 178, 97 + 1 * 18));
		container.addSlotToContainer(new SlotBauble(player, baubles, 2, 178, 97 + 2 * 18));
		container.addSlotToContainer(new SlotBauble(player, baubles, 3, 178, 97 + 3 * 18));
		container.addSlotToContainer(new SlotBauble(player, baubles, 4, 178, 97 + 4 * 18));
		container.addSlotToContainer(new SlotBauble(player, baubles, 5, 178, 97 + 5 * 18));
		container.addSlotToContainer(new SlotBauble(player, baubles, 6, 178, 97 + 6 * 18));
	}

	public static boolean isBaubleSlot(Slot slot) {
		return slot instanceof SlotBauble;
	}

}
