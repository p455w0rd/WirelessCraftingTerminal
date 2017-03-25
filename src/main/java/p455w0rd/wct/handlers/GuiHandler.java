/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2016, p455w0rd
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
package p455w0rd.wct.handlers;

import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.helpers.WirelessTerminalGuiObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import p455w0rd.wct.WCT;
import p455w0rd.wct.api.IWirelessCraftingTermHandler;
import p455w0rd.wct.client.gui.GuiCraftAmount;
import p455w0rd.wct.client.gui.GuiCraftConfirm;
import p455w0rd.wct.client.gui.GuiCraftingStatus;
import p455w0rd.wct.client.gui.GuiMagnet;
import p455w0rd.wct.client.gui.GuiWCT;
import p455w0rd.wct.container.ContainerCraftAmount;
import p455w0rd.wct.container.ContainerCraftConfirm;
import p455w0rd.wct.container.ContainerCraftingStatus;
import p455w0rd.wct.container.ContainerMagnet;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class GuiHandler implements IGuiHandler {

	public static final int GUI_WCT = 0;
	public static final int GUI_CRAFT_CONFIRM = 1;
	public static final int GUI_CRAFT_AMOUNT = 2;
	public static final int GUI_CRAFTING_STATUS = 3;
	public static final int GUI_MAGNET = 4;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID != GUI_MAGNET) {
			ITerminalHost terminal = getTerminal(player, world, new BlockPos(x, y, z));
			if (terminal != null) {
				if (ID == GUI_WCT) {
					return new ContainerWCT(player, player.inventory);
				}

				if (ID == GUI_CRAFTING_STATUS) {
					return new ContainerCraftingStatus(player.inventory, terminal);
				}

				if (ID == GUI_CRAFT_AMOUNT) {
					return new ContainerCraftAmount(player.inventory, terminal);
				}

				if (ID == GUI_CRAFT_CONFIRM) {
					return new ContainerCraftConfirm(player.inventory, terminal);
				}
			}
		}
		if (ID == GUI_MAGNET) {
			return new ContainerMagnet(player, player.inventory);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID != GUI_MAGNET) {
			ITerminalHost terminal = getTerminal(player, world, new BlockPos(x, y, z));
			if (terminal != null) {
				if (ID == GUI_WCT) {
					GuiWCT.setSwitchingGuis(false);
					return new GuiWCT(new ContainerWCT(player, player.inventory));
				}

				if (ID == GUI_CRAFTING_STATUS) {
					return new GuiCraftingStatus(player.inventory, terminal);
				}

				if (ID == GUI_CRAFT_AMOUNT) {
					return new GuiCraftAmount(player.inventory, terminal);
				}

				if (ID == GUI_CRAFT_CONFIRM) {
					return new GuiCraftConfirm(player.inventory, terminal);
				}
			}
		}
		if (ID == GUI_MAGNET) {
			return new GuiMagnet(new ContainerMagnet(player, player.inventory), WCTUtils.getMagnet(player.inventory));
		}
		return null;
	}

	private ITerminalHost getTerminal(EntityPlayer player, World world, BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(WCTUtils.getWirelessTerm(player.inventory));
		final WirelessTerminalGuiObject terminal = wh == null ? null : new WirelessTerminalGuiObject(wh, WCTUtils.getWirelessTerm(player.inventory), player, world, x, y, z);
		return terminal;
	}

	public static void open(int ID, EntityPlayer playerIn, World worldIn, BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		playerIn.openGui(WCT.INSTANCE, ID, worldIn, x, y, z);
	}

}
