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
package p455w0rd.wct.util;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.ICustomWirelessTermHandler;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.helpers.WTGuiObject;
import p455w0rd.ae2wtlib.integration.Baubles;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.WCTApi;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.init.ModIntegration.Mods;
import p455w0rd.wct.init.ModKeybindings;

public class WCTUtils {

	public static final String SHIFTCRAFT_NBT = "ShiftCraft";

	// Parent pair contains a boolean which tells whether or not this is a bauble slot
	// Child pair gives the slot number and ItemStack
	public static List<Pair<Boolean, Pair<Integer, ItemStack>>> getCraftingTerminals(EntityPlayer player) {
		return WTApi.instance().getAllWirelessTerminalsByType(player, IWirelessCraftingTerminalItem.class);
	}

	public static ItemStack getWCTBySlot(EntityPlayer player, int slot) {
		if (slot >= 0) {
			ItemStack wirelessTerminal = player.inventory.mainInventory.get(slot);
			if (!wirelessTerminal.isEmpty() && wirelessTerminal.getItem() instanceof IWirelessCraftingTerminalItem) {
				return wirelessTerminal;
			}
		}
		return ItemStack.EMPTY;
	}

	/**
	 * gets the first available Wireless Crafting Terminal
	 * the Integer of the Pair tells the slotNumber
	 * the boolean tells whether or not the Integer is a Baubles slot
	 */
	@Nonnull
	public static Pair<Boolean, Pair<Integer, ItemStack>> getFirstWirelessCraftingTerminal(InventoryPlayer playerInv) {
		boolean isBauble = false;
		int slotID = -1;
		ItemStack wirelessTerm = ItemStack.EMPTY;
		if (!playerInv.player.getHeldItemMainhand().isEmpty() && playerInv.player.getHeldItemMainhand().getItem() instanceof IWirelessCraftingTerminalItem) {
			slotID = playerInv.currentItem;
			wirelessTerm = playerInv.player.getHeldItemMainhand();
		}
		else {
			if (Mods.BAUBLES.isLoaded()) {
				wirelessTerm = Baubles.getFirstWTBauble(playerInv.player).getRight();
				slotID = Baubles.getFirstWTBauble(playerInv.player).getLeft();
				if (!wirelessTerm.isEmpty()) {
					isBauble = true;
				}
			}
			if (wirelessTerm.isEmpty()) {
				int invSize = playerInv.getSizeInventory();
				if (invSize > 0) {
					for (int i = 0; i < invSize; ++i) {
						ItemStack item = playerInv.getStackInSlot(i);
						if (item.isEmpty()) {
							continue;
						}
						if (item.getItem() instanceof IWirelessCraftingTerminalItem) {
							wirelessTerm = item;
							slotID = i;
							break;
						}
					}
				}
			}
		}
		return Pair.of(isBauble, Pair.of(slotID, wirelessTerm));
	}

	public static boolean isAnyWCT(@Nonnull ItemStack wirelessTerm) {
		return wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem;
	}

	/*
	public static IWirelessAccessPoint getWAP(@Nonnull ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
		WCTGuiObject object = getGUIObject(wirelessTerm, player);
		if (object != null) {
			return object.getWAP();
		}
		return null;
	}
	*/
	public static WTGuiObject<?, ?> getGUIObject(EntityPlayer player) {
		return getGUIObject(null, player);
	}

	public static WTGuiObject<?, ?> getGUIObject(@Nullable ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
		if (wirelessTerm == null) {
			if (player.openContainer instanceof ContainerWCT) {
				ContainerWCT c = (ContainerWCT) player.openContainer;
				if (c.getGuiObject() != null) {
					return c.getGuiObject();
				}
			}
		}
		else {
			if (wirelessTerm.getItem() instanceof ICustomWirelessTermHandler) {
				if (player != null && player.getEntityWorld() != null) {
					return new WTGuiObject<IAEItemStack, IItemStorageChannel>((ICustomWirelessTermHandler) wirelessTerm.getItem(), wirelessTerm, player, player.getEntityWorld(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
				}
			}
		}
		return null;
	}

	/*
		public static WCTFluidGuiObject getFluidGUIObject(EntityPlayer player) {
			return getFluidGUIObject(null, player);
		}

		public static WCTFluidGuiObject getFluidGUIObject(@Nullable ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
			//if (wirelessTerm == null) {
			if (player.openContainer instanceof ContainerWFT || wirelessTerm == null) {
				ContainerWFT c = (ContainerWFT) player.openContainer;
				if (c.getObject() != null && c.getObject() instanceof WCTFluidGuiObject) {
					return (WCTFluidGuiObject) c.getObject();
				}
			}
			//}
			else {
				if (wirelessTerm.getItem() instanceof IWirelessFluidTermHandler) {
					if (player != null && player.getEntityWorld() != null) {
						return new WCTFluidGuiObject((IWirelessFluidTermHandler) wirelessTerm.getItem(), wirelessTerm, player, player.getEntityWorld(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
					}
				}
			}
			return null;
		}
	*/

	@SideOnly(Side.CLIENT)
	public static void handleKeybind() {
		EntityPlayer p = Minecraft.getMinecraft().player;
		if (p.openContainer == null) {
			return;
		}
		if (ModKeybindings.openTerminal.isPressed()) {
			Pair<Boolean, Pair<Integer, ItemStack>> pair = WCTUtils.getFirstWirelessCraftingTerminal(p.inventory);
			ItemStack is = pair.getRight().getRight();
			if (is.isEmpty()) {
				return;
			}
			int slot = pair.getRight().getLeft();
			boolean isBauble = pair.getLeft();
			IWirelessCraftingTerminalItem wirelessTerm = (IWirelessCraftingTerminalItem) is.getItem();
			if (wirelessTerm != null) {
				if (!(p.openContainer instanceof ContainerWCT)) {
					if (slot >= 0) {
						WCTApi.instance().openWCTGui(p, isBauble, slot);
						//ModNetworking.instance().sendToServer(new PacketOpenGui(ModGuiHandler.GUI_WCT, slot, isBauble));
					}
				}
				else {
					p.closeScreen();
				}
			}
		}
		/*
		else if (ModKeybindings.openMagnetFilter.getKeyCode() != Keyboard.CHAR_NONE && ModKeybindings.openMagnetFilter.isPressed()) {
			ItemStack magnetItem = ItemMagnet.getFirstMagnet(p.inventory);
			if (!magnetItem.isEmpty()) {
				if (!ItemMagnet.isMagnetInitialized(magnetItem)) {
					if (magnetItem.getTagCompound() == null) {
						magnetItem.setTagCompound(new NBTTagCompound());
					}
					ModNetworking.instance().sendToServer(new PacketMagnetFilterHeld(0, true));
				}
				if (!(p.openContainer instanceof ContainerMagnet)) {
					ModNetworking.instance().sendToServer(new PacketOpenGui(ModGuiHandler.GUI_MAGNET));
				}
			}
		}
		else if (ModKeybindings.changeMagnetMode.getKeyCode() != Keyboard.CHAR_NONE && ModKeybindings.changeMagnetMode.isPressed()) {
			ItemStack magnetItem = ItemMagnet.getFirstMagnet(p.inventory);
			if (!magnetItem.isEmpty()) {
				if (!ItemMagnet.isMagnetInitialized(magnetItem)) {
					if (!magnetItem.hasTagCompound()) {
						magnetItem.setTagCompound(new NBTTagCompound());
					}
					ModNetworking.instance().sendToServer(new PacketMagnetFilterHeld(0, true));
				}
				ItemMagnet.switchMagnetMode(magnetItem);
			}
		}
		*/
	}

	public static boolean getShiftCraftMode(@Nonnull ItemStack wirelessTerminal) {
		if (!wirelessTerminal.isEmpty() && wirelessTerminal.hasTagCompound() && wirelessTerminal.getTagCompound().hasKey(SHIFTCRAFT_NBT, NBT.TAG_BYTE)) {
			return !wirelessTerminal.getTagCompound().getBoolean(SHIFTCRAFT_NBT);
		}
		return false;
	}

}
