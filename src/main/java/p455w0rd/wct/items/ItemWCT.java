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

import appeng.core.localization.GuiText;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.client.render.StackSizeRenderer.ReadableNumberConverter;
import p455w0rd.ae2wtlib.items.ItemWT;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.init.ModGlobals;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
@Optional.InterfaceList(value = {

		@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles|API", striprefs = true)
})
public class ItemWCT extends ItemWT implements IWirelessCraftingTerminalItem {

	public ItemWCT() {
		this(new ResourceLocation(ModGlobals.MODID, "wct"));
	}

	protected ItemWCT(ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack item = player.getHeldItem(hand);
		if (!world.isRemote && hand == EnumHand.MAIN_HAND && !item.isEmpty() && getAECurrentPower(item) > 0) {
			ModGuiHandler.open(ModGuiHandler.GUI_WCT, player, world, player.getPosition(), false, false, player.inventory.currentItem);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
		}
		if (getAECurrentPower(item) <= 0 && !world.isRemote) {
			player.sendMessage(new TextComponentString("No Power"));
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addCheckedInformation(final ItemStack is, final World world, final List<String> list, final ITooltipFlag advancedTooltips) {
		if (getPlayer() == null || WCTUtils.getGUIObject(is, getPlayer()) == null) {
			return;
		}
		String encKey = getEncryptionKey(is);
		//String shift = I18n.format("tooltip.press_shift.desc").replace("Shift", TextFormatting.YELLOW + "" + TextFormatting.BOLD + "" + TextFormatting.ITALIC + "Shift" + TextFormatting.GRAY);
		String pctTxtColor = TextFormatting.WHITE + "";
		double aeCurrPower = getAECurrentPower(is);
		double aeCurrPowerPct = (int) Math.floor(aeCurrPower / getAEMaxPower(is) * 1e4) / 1e2;
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
		String magnetStatus = (ItemMagnet.isMagnetInstalled(is) ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format("tooltip.not.desc")) + " " + I18n.format("tooltip.installed.desc");
		if (WTApi.instance().getConfig().isInfinityBoosterCardEnabled()) {
			if (WTApi.instance().getConfig().isOldInfinityMechanicEnabled()) {
				list.add(I18n.format("item.infinity_booster_card.name") + ": " + (checkForBooster(is) ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format("tooltip.not.desc")) + " " + I18n.format("tooltip.installed.desc"));
			}
			else {
				int infinityEnergyAmount = WTApi.instance().getInfinityEnergy(is);
				String amountColor = infinityEnergyAmount < WTApi.instance().getConfig().getLowInfinityEnergyWarningAmount() ? TextFormatting.RED.toString() : TextFormatting.GREEN.toString();
				String reasonString = "";
				if (infinityEnergyAmount <= 0) {
					reasonString = "(" + I18n.format("tooltip.out_of.desc") + " " + I18n.format("tooltip.infinity_energy.desc") + ")";
				}
				boolean outsideOfWAPRange = !WTApi.instance().isInRange(is);
				if (!outsideOfWAPRange) {
					reasonString = I18n.format("tooltip.in_wap_range.desc");
				}
				String activeString = infinityEnergyAmount > 0 && outsideOfWAPRange ? TextFormatting.GREEN + "" + I18n.format("tooltip.active.desc") : TextFormatting.GRAY + "" + I18n.format("tooltip.inactive.desc") + " " + reasonString;
				list.add(I18n.format("tooltip.infinite_range.desc") + ": " + activeString);
				String infinityEnergyString = WTApi.instance().isWTCreative(is) ? I18n.format("tooltip.infinite.desc") : (isShiftKeyDown() ? "" + infinityEnergyAmount + "" + TextFormatting.GRAY + " " + I18n.format("tooltip.units.desc") : ReadableNumberConverter.INSTANCE.toSlimReadableForm(infinityEnergyAmount));
				list.add(I18n.format("tooltip.infinity_energy.desc") + ": " + amountColor + "" + infinityEnergyString);
			}
		}
		list.add(I18n.format("item.wct:magnet_card.name") + ": " + magnetStatus);
	}

	@Override
	public void onUpdate(final ItemStack wirelessTerminal, final World w, final Entity e, int i, boolean f) {
		if (!(e instanceof EntityPlayer)) {
			return;
		}
		EntityPlayer p = (EntityPlayer) e;
		if (getPlayer() == null) {
			setPlayer(p);
		}
		if (wirelessTerminal == null || !(wirelessTerminal.getItem() instanceof IWirelessCraftingTerminalItem)) {
			return;
		}
		super.onUpdate(wirelessTerminal, w, e, i, f);
		ItemMagnet.isMagnetInstalled(wirelessTerminal);
	}

}
