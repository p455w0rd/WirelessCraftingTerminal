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
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.util.Platform;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.ICustomWirelessTerminalItem;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.api.client.ItemStackSizeRenderer;
import p455w0rd.ae2wtlib.api.item.ItemWT;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.WCTApi;
import p455w0rd.wct.init.ModGlobals;

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

	@Override
	public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
		final ItemStack item = player.getHeldItem(hand);
		if (world.isRemote && hand == EnumHand.MAIN_HAND && !item.isEmpty() && getAECurrentPower(item) > 0) {
			openGui(player, false, player.inventory.currentItem);
			return new ActionResult<>(EnumActionResult.SUCCESS, item);
		}
		else if (!world.isRemote) {
			if (getAECurrentPower(item) <= 0) {
				player.sendMessage(PlayerMessages.DeviceNotPowered.get());
				return new ActionResult<>(EnumActionResult.FAIL, item);
			}
			if (!WCTApi.instance().isTerminalLinked(item)) {
				player.sendMessage(PlayerMessages.DeviceNotLinked.get());
				return new ActionResult<>(EnumActionResult.FAIL, item);
			}
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, item);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addCheckedInformation(final ItemStack is, final World world, final List<String> list, final ITooltipFlag advancedTooltips) {
		if (getPlayer() == null || WTApi.instance().getGUIObject(is, getPlayer()) == null) {
			return;
		}
		final String encKey = getEncryptionKey(is);
		String pctTxtColor = TextFormatting.WHITE + "";
		final double aeCurrPower = getAECurrentPower(is);
		final double aeCurrPowerPct = (int) Math.floor(aeCurrPower / getAEMaxPower(is) * 1e4) / 1e2;
		if ((int) aeCurrPowerPct >= 75) {
			pctTxtColor = TextFormatting.GREEN + "";
		}
		if ((int) aeCurrPowerPct <= 5) {
			pctTxtColor = TextFormatting.RED + "";
		}
		list.add(TextFormatting.AQUA + "==============================");
		if (WTApi.instance().isWTCreative(is)) {
			list.add(GuiText.StoredEnergy.getLocal() + ": " + TextFormatting.GREEN + "" + I18n.format("tooltip.infinite.desc"));
		}
		else {
			list.add(GuiText.StoredEnergy.getLocal() + ": " + pctTxtColor + (int) aeCurrPower + " AE - " + aeCurrPowerPct + "%");
		}
		String linked = TextFormatting.RED + GuiText.Unlinked.getLocal();
		if (encKey != null && !encKey.isEmpty()) {
			linked = TextFormatting.BLUE + GuiText.Linked.getLocal();
		}
		list.add("Link Status: " + linked);
		final String magnetStatus = (ItemMagnet.isMagnetInstalled(is) ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format("tooltip.not.desc")) + " " + I18n.format("tooltip.installed.desc");
		if (WTApi.instance().getConfig().isInfinityBoosterCardEnabled()) {
			if (WTApi.instance().getConfig().isOldInfinityMechanicEnabled()) {
				list.add(I18n.format("item.ae2wtlib:infinity_booster_card.name") + ": " + (hasInfiniteRange(is) ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format("tooltip.not.desc")) + " " + I18n.format("tooltip.installed.desc"));
			}
			else {
				final int infinityEnergyAmount = WTApi.instance().getInfinityEnergy(is);
				final String amountColor = infinityEnergyAmount < WTApi.instance().getConfig().getLowInfinityEnergyWarningAmount() ? TextFormatting.RED.toString() : TextFormatting.GREEN.toString();
				String reasonString = "";
				if (infinityEnergyAmount <= 0) {
					reasonString = "(" + I18n.format("tooltip.out_of.desc") + " " + I18n.format("tooltip.infinity_energy.desc") + ")";
				}
				final boolean outsideOfWAPRange = !WTApi.instance().isInRange(is);
				if (!outsideOfWAPRange) {
					reasonString = I18n.format("tooltip.in_wap_range.desc");
				}
				final String activeString = infinityEnergyAmount > 0 && outsideOfWAPRange ? TextFormatting.GREEN + "" + I18n.format("tooltip.active.desc") : TextFormatting.GRAY + "" + I18n.format("tooltip.inactive.desc") + " " + reasonString;
				list.add(I18n.format("tooltip.infinite_range.desc") + ": " + activeString);
				final String infinityEnergyString = WTApi.instance().isWTCreative(is) ? I18n.format("tooltip.infinite.desc") : isShiftKeyDown() ? "" + infinityEnergyAmount + "" + TextFormatting.GRAY + " " + I18n.format("tooltip.units.desc") : ItemStackSizeRenderer.getInstance().getConverter().toSlimReadableForm(infinityEnergyAmount);
				list.add(I18n.format("tooltip.infinity_energy.desc") + ": " + amountColor + "" + infinityEnergyString);
			}
		}
		list.add(I18n.format("item.wct:magnet_card.name") + ": " + magnetStatus);
	}

	@Override
	public void onUpdate(final ItemStack wirelessTerminal, final World w, final Entity e, final int i, final boolean f) {
		super.onUpdate(wirelessTerminal, w, e, i, f);
		if (wirelessTerminal == null || !(wirelessTerminal.getItem() instanceof ICustomWirelessTerminalItem)) {
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
