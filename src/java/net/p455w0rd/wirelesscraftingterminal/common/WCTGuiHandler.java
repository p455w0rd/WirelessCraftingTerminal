package net.p455w0rd.wirelesscraftingterminal.common;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IPortableCell;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftAmount;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftingStatus;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiMagnet;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftAmount;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftingStatus;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class WCTGuiHandler implements IGuiHandler {

	public void registerRenderers() {
	}

	@Override
	public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {
		final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(RandomUtils.getWirelessTerm(player.inventory));
		if (wh != null) {
			final WirelessTerminalGuiObject obj = new WirelessTerminalGuiObject(wh, RandomUtils.getWirelessTerm(player.inventory), player, world, x, y, z);
			if (obj != null) {
				final IPortableCell terminal = obj;

				if (guiId == Reference.GUI_WCT) {
					return new ContainerWirelessCraftingTerminal(player, player.inventory);
				}

				if (guiId == Reference.GUI_CRAFTING_STATUS) {
					return new ContainerCraftingStatus(player.inventory, terminal);
				}

				if (guiId == Reference.GUI_CRAFT_AMOUNT) {
					return new ContainerCraftAmount(player.inventory, terminal);
				}

				if (guiId == Reference.GUI_CRAFT_CONFIRM) {
					return new ContainerCraftConfirm(player.inventory, terminal);
				}
			}
		}
		if (guiId == Reference.GUI_MAGNET) {
			return new ContainerMagnet(player, player.inventory);
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {
		final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(RandomUtils.getWirelessTerm(player.inventory));
		if (wh != null) {
			final WirelessTerminalGuiObject obj = new WirelessTerminalGuiObject(wh, RandomUtils.getWirelessTerm(player.inventory), player, world, x, y, z);
			if (obj != null) {
				final IPortableCell terminal = obj;

				if (guiId == Reference.GUI_WCT) {
					return new GuiWirelessCraftingTerminal(new ContainerWirelessCraftingTerminal(player, player.inventory));
				}

				if (guiId == Reference.GUI_CRAFTING_STATUS) {
					return new GuiCraftingStatus(player.inventory, terminal);
				}

				if (guiId == Reference.GUI_CRAFT_AMOUNT) {
					return new GuiCraftAmount(player.inventory, terminal);
				}

				if (guiId == Reference.GUI_CRAFT_CONFIRM) {
					return new GuiCraftConfirm(player.inventory, terminal);
				}
			}
		}
		if (guiId == Reference.GUI_MAGNET) {
			return new GuiMagnet(new ContainerMagnet(player, player.inventory));
		}

		return null;
	}

	public static void launchGui(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
		player.openGui(WirelessCraftingTerminal.INSTANCE, ID, world, x, y, z);
	}
}
