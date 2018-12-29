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
package p455w0rd.wct.init;

import org.apache.commons.lang3.tuple.Pair;

import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import p455w0rd.ae2wtlib.api.*;
import p455w0rd.wct.WCT;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.client.gui.*;
import p455w0rd.wct.container.*;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class ModGuiHandler implements IGuiHandler {

	private static final ModGuiHandler INSTANCE = new ModGuiHandler();
	public static final int GUI_WCT = 0;
	public static final int GUI_CRAFT_CONFIRM = 1;
	public static final int GUI_CRAFT_AMOUNT = 2;
	public static final int GUI_CRAFTING_STATUS = 3;
	public static final int GUI_MAGNET = 4;
	public static final int GUI_WFT = 5;
	private static int slot = -1;
	private static boolean isBauble = false;
	private static boolean isMagnetHeld = false;

	public static ModGuiHandler getInstance() {
		return INSTANCE;
	}

	public static boolean isBauble() {
		return isBauble;
	}

	public static void setIsBauble(boolean value) {
		isBauble = value;
	}

	public static int getSlot() {
		return slot;
	}

	public static void setSlot(int value) {
		slot = value;
	}

	public static boolean isMagnetHeld() {
		return isMagnetHeld;
	}

	public static void setMagnetHeld(boolean isHeld) {
		isMagnetHeld = isHeld;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID != GUI_MAGNET) {
			ITerminalHost terminal = getCraftingTerminal(player, world, new BlockPos(x, y, z), isBauble(), getSlot());
			if (terminal != null) {
				if (ID == GUI_WCT) {
					return new ContainerWCT(player, terminal, getSlot(), isBauble());
				}

				if (ID == GUI_CRAFTING_STATUS) {
					return new ContainerCraftingStatus(player.inventory, terminal, getSlot(), isBauble());
				}

				if (ID == GUI_CRAFT_AMOUNT) {
					return new ContainerCraftAmount(player.inventory, terminal, getSlot(), isBauble());
				}

				if (ID == GUI_CRAFT_CONFIRM) {
					return new ContainerCraftConfirm(player.inventory, terminal, isBauble(), getSlot());
				}
			}
		}
		if (ID == GUI_MAGNET) {
			return new ContainerMagnet(player, isMagnetHeld(), isBauble(), getSlot());
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID != GUI_MAGNET) {
			ITerminalHost craftingTerminal = getCraftingTerminal(player, world, new BlockPos(x, y, z), isBauble(), getSlot());
			if (craftingTerminal != null) {
				if (ID == GUI_WCT) {
					GuiWCT.setSwitchingGuis(false);
					return new GuiWCT(new ContainerWCT(player, craftingTerminal, getSlot(), isBauble()));
				}

				if (ID == GUI_CRAFTING_STATUS) {
					return new GuiCraftingStatus(player.inventory, craftingTerminal, getSlot(), isBauble());
				}

				if (ID == GUI_CRAFT_AMOUNT) {
					return new GuiCraftAmount(player.inventory, craftingTerminal, getSlot(), isBauble());
				}

				if (ID == GUI_CRAFT_CONFIRM) {
					return new GuiCraftConfirm(player.inventory, craftingTerminal, isBauble(), getSlot());
				}
			}
		}
		if (ID == GUI_MAGNET) {
			return new GuiMagnet(new ContainerMagnet(player, isMagnetHeld(), isBauble(), getSlot()));
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private ITerminalHost getCraftingTerminal(EntityPlayer player, World world, BlockPos pos, boolean isBauble, int slot) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		ItemStack wirelessTerminal = ItemStack.EMPTY;
		if (slot >= 0) {
			wirelessTerminal = isBauble ? WTApi.instance().getBaublesUtility().getWTBySlot(player, slot, IWirelessCraftingTerminalItem.class) : WTApi.instance().getWTBySlot(player, slot);
		}
		else {
			Pair<Boolean, Pair<Integer, ItemStack>> firstTerm = WCTUtils.getFirstWirelessCraftingTerminal(player.inventory);
			wirelessTerminal = firstTerm.getRight().getRight();
			setSlot(firstTerm.getRight().getLeft());
			setIsBauble(firstTerm.getLeft());
		}
		final ICustomWirelessTermHandler wh = (ICustomWirelessTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(wirelessTerminal);
		final WTGuiObject<IAEItemStack> terminal = wh == null ? null : (WTGuiObject<IAEItemStack>) WTApi.instance().getGUIObject(wh, wirelessTerminal, player);
		return terminal;
	}

	public static void open(int ID, EntityPlayer player, World world, BlockPos pos, boolean isMagnetHeld, boolean isBauble, int slot) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		setIsBauble(isBauble);
		setSlot(slot);
		setMagnetHeld(isMagnetHeld);
		player.openGui(WCT.INSTANCE, ID, world, x, y, z);
	}

}
