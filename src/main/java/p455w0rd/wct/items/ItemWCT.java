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
package p455w0rd.wct.items;

import java.util.List;

import appeng.api.config.*;
import appeng.api.util.IConfigManager;
import appeng.util.Platform;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.ICustomWirelessTerminalItem;
import p455w0rd.ae2wtlib.api.item.ItemWT;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.WCTApi;
import p455w0rd.wct.init.ModGlobals;
import p455w0rd.wct.init.ModKeybindings;
import p455w0rd.wct.items.ItemMagnet.MagnetFunctionMode;

/**
 * @author p455w0rd
 *
 */

public class ItemWCT extends ItemWT implements IWirelessCraftingTerminalItem {

	public ItemWCT() {
		this(new ResourceLocation(ModGlobals.MODID, "wct"));
	}

	protected ItemWCT(final ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	public IConfigManager getConfigManager(final ItemStack target) {
		final IConfigManager out = super.getConfigManager(target);
		out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
		out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
		out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
		out.readFromNBT(Platform.openNbtData(target).copy());
		return out;
	}

	@Override
	public void openGui(final EntityPlayer player, final boolean isBauble, final int playerSlot) {
		WCTApi.instance().openWCTGui(player, isBauble, playerSlot);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addCheckedInformation(final ItemStack is, final World world, final List<String> list, final ITooltipFlag advancedTooltips) {
		if (hasValidGuiObject(is)) {
			super.addCheckedInformation(is, world, list, advancedTooltips);
			addTooltipMagnetInfo(is, list);
		}
	}

	@SideOnly(Side.CLIENT)
	private void addTooltipMagnetInfo(final ItemStack wirelessTerminal, final List<String> tooltip) {
		final boolean isMagnetInstalled = ItemMagnet.isMagnetInstalled(wirelessTerminal);
		final String magnetStatus = (isMagnetInstalled ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format("tooltip.not.desc")) + " " + I18n.format("tooltip.installed.desc");
		if (isMagnetInstalled) {
			final String magnetActive = TextFormatting.GREEN + I18n.format("tooltip.active.desc");
			final String magnetInactive = TextFormatting.GRAY + I18n.format("tooltip.inactive.desc");
			final MagnetFunctionMode magnetMode = ItemMagnet.getMagnetFunctionMode(ItemMagnet.getMagnetFromWCT(wirelessTerminal));
			final boolean isActive = magnetMode.ordinal() != 0;
			tooltip.add(I18n.format("item.wct:magnet_card.name") + ": " + magnetStatus + TextFormatting.GRAY + " / " + (isActive ? magnetActive : magnetInactive));
			if (isActive && GuiScreen.isShiftKeyDown()) {
				final String magnetModeMsg = magnetMode.getMessage().split("-")[1];
				tooltip.add(" - " + TextFormatting.ITALIC + magnetModeMsg);
				tooltip.add("");
			}
			if (GuiScreen.isShiftKeyDown()) {
				tooltip.add(TextFormatting.BLUE + I18n.format("tooltip.magnet_mode_keybind.desc") + ": " + TextFormatting.RESET + ModKeybindings.cycleMagnetMode.getDisplayName());
				tooltip.add("");
			}
		}
		else {
			tooltip.add(I18n.format("item.wct:magnet_card.name") + ": " + magnetStatus);
		}
	}

	@Override
	public void onUpdate(final ItemStack wirelessTerminal, final World w, final Entity e, final int i, final boolean f) {
		super.onUpdate(wirelessTerminal, w, e, i, f);
		if (!(wirelessTerminal.getItem() instanceof ICustomWirelessTerminalItem)) {
			return;
		}
		ItemMagnet.isMagnetInstalled(wirelessTerminal);
	}

	@Override
	public ResourceLocation getMenuIcon() {
		return new ResourceLocation(ModGlobals.MODID, "textures/items/wct.png");
	}

	@Override
	public int getColor() {
		return 0xFFB8231D;
	}

}
