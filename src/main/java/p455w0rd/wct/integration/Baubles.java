package p455w0rd.wct.integration;

import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import baubles.common.container.SlotBauble;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.slot.SlotAEBauble;
import p455w0rd.wct.inventory.WCTBaublesInventory;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketBaubleSync;

/**
 * @author p455w0rd
 *
 */
public class Baubles {

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

	public static IBaublesItemHandler getBaubles(EntityPlayer player) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			return player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
		}
		return null;
	}

	public static void setBaublesItemStack(EntityPlayer player, int slot, ItemStack stack) {
		if (player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) {
			IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
			baubles.setStackInSlot(slot, stack);
		}
	}

	public static void addBaubleSlots(ContainerWCT container, EntityPlayer player) {
		IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
		WCTBaublesInventory inventory = new WCTBaublesInventory(player);
		for (int i = 0; i < 7; i++) {
			container.addSlotToContainer(new SlotAEBauble(inventory, i, 178, -62 + i * 18));
		}
	}

	public static boolean isBaubleSlot(Slot slot) {
		return slot instanceof SlotBauble;
	}

	public static void doForcedSync(EntityPlayer player, ItemStack stack) {
		int slot = getWCTBaubleSlotIndex(player);
		//NetworkHandler.instance().sendToDimension(new PacketBaubleSync(slot, stack), player.getEntityWorld().provider.getDimension());
		NetworkHandler.instance().sendToServer(new PacketBaubleSync(slot, stack));
	}

	public static boolean isBaubleItem(ItemStack stack) {
		return stack.getItem() instanceof IBauble;
	}

	public static boolean isAEBaubleSlot(Slot slot) {
		return slot instanceof SlotAEBauble;
	}

}
