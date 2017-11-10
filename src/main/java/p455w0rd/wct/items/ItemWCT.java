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

import org.lwjgl.input.Keyboard;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.util.IConfigManager;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import baubles.api.BaubleType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.wct.api.IBaubleItem;
import p455w0rd.wct.api.IBaubleRender;
import p455w0rd.wct.api.IModelHolder;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.client.render.RenderLayerWCT;
import p455w0rd.wct.client.render.StackSizeRenderer.ReadableNumberConverter;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketSetInRange;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
@Optional.InterfaceList(value = {

		@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles|API", striprefs = true)
})
public class ItemWCT extends AEBasePoweredItem implements IModelHolder, IWirelessCraftingTerminalItem, IBaubleItem {

	private static final String name = "wct";
	public static final String LINK_KEY_STRING = "key";
	public static double GLOBAL_POWER_MULTIPLIER = PowerMultiplier.CONFIG.multiplier;

	private EntityPlayer entityPlayer;

	public ItemWCT() {
		super(ModConfig.WCT_MAX_POWER);
		setRegistryName(name);
		setUnlocalizedName(name);
		setMaxStackSize(1);
		ForgeRegistries.ITEMS.register(this);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack item = player.getHeldItem(hand);
		if (!world.isRemote && hand == EnumHand.MAIN_HAND && !item.isEmpty() && getAECurrentPower(item) > 0) {
			ModGuiHandler.open(ModGuiHandler.GUI_WCT, player, world, player.getPosition());
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
		}
		if (getAECurrentPower(item) <= 0 && !world.isRemote) {
			WCTUtils.chatMessage(player, new TextComponentString("No Power"));
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
	}

	@Override
	public boolean isDamageable() {
		return false;
	}

	@Override
	public void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemList) {
		if (isInCreativeTab(creativeTab)) {
			super.getCheckedSubItems(creativeTab, itemList);
			itemList.add(new ItemStack(this));
			ItemStack is = new ItemStack(this);
			injectAEPower(is, ModConfig.WCT_MAX_POWER, Actionable.MODULATE);
			itemList.add(is);
		}
	}

	@Override
	public boolean showDurabilityBar(ItemStack is) {
		double aeCurrPower = getAECurrentPower(is);
		double aeMaxPower = getAEMaxPower(is);
		if ((int) aeCurrPower >= (int) aeMaxPower) {
			return false;
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addCheckedInformation(final ItemStack is, final World world, final List<String> list, final ITooltipFlag advancedTooltips) {
		if (entityPlayer == null || WCTUtils.getGUIObject(is, entityPlayer) == null) {
			return;
		}
		String encKey = getEncryptionKey(is);
		String shift = I18n.format("tooltip.press_shift.desc").replace("Shift", TextFormatting.YELLOW + "" + TextFormatting.BOLD + "" + TextFormatting.ITALIC + "Shift" + TextFormatting.GRAY);
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
		list.add(GuiText.StoredEnergy + ": " + pctTxtColor + (int) aeCurrPower + " AE - " + aeCurrPowerPct + "%");
		//if (isShiftKeyDown()) {
		String linked = TextFormatting.RED + GuiText.Unlinked.getLocal();
		if (encKey != null && !encKey.isEmpty()) {
			linked = TextFormatting.BLUE + GuiText.Linked.getLocal();
		}
		list.add("Link Status: " + linked);
		String magnetStatus = (WCTUtils.isMagnetInstalled(is) ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format("tooltip.not.desc")) + " " + I18n.format("tooltip.installed.desc");
		if (ModConfig.WCT_BOOSTER_ENABLED) {
			if (ModConfig.USE_OLD_INFINTY_MECHANIC) {
				list.add(I18n.format("item.infinity_booster_card.name") + ": " + (checkForBooster(is) ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format("tooltip.not.desc")) + " " + I18n.format("tooltip.installed.desc"));
			}
			else {
				int infinityEnergyAmount = WCTUtils.getInfinityEnergy(is);
				String amountColor = infinityEnergyAmount < ModConfig.INFINTY_ENERGY_LOW_WARNING_AMOUNT ? TextFormatting.RED.toString() : TextFormatting.GREEN.toString();
				String reasonString = "";
				if (infinityEnergyAmount <= 0) {
					reasonString = I18n.format("tooltip.out_of.desc") + " " + I18n.format("tooltip.infinity_energy.desc");
				}
				boolean outsideOfWAPRange = !WCTUtils.isInRange(is);
				if (!outsideOfWAPRange) {
					reasonString = I18n.format("tooltip.in_wap_range.desc");
				}
				String activeString = infinityEnergyAmount > 0 && outsideOfWAPRange ? TextFormatting.GREEN + "" + I18n.format("tooltip.active.desc") : TextFormatting.GRAY + "" + I18n.format("tooltip.inactive.desc") + " " + reasonString;
				list.add(I18n.format("tooltip.infinite_range.desc") + ": " + activeString);
				list.add(I18n.format("tooltip.infinity_energy.desc") + ": " + amountColor + "" + (isShiftKeyDown() ? infinityEnergyAmount : ReadableNumberConverter.INSTANCE.toSlimReadableForm(infinityEnergyAmount)) + "" + TextFormatting.GRAY + " " + I18n.format("tooltip.units.desc"));
			}
		}
		list.add(I18n.format("item.magnet_card.name") + ": " + magnetStatus);
	}

	@Override
	public boolean isWirelessCraftingEnabled(final ItemStack wirelessTerminal) {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public static boolean isShiftKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}

	@Override
	public boolean usePower(final EntityPlayer player, final double amount, final ItemStack is) {
		return extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
	}

	@Override
	public boolean hasPower(final EntityPlayer player, final double amt, final ItemStack is) {
		return getAECurrentPower(is) >= amt;
	}

	@Override
	public IConfigManager getConfigManager(final ItemStack target) {
		final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
			final NBTTagCompound data = Platform.openNbtData(target);
			manager.writeToNBT(data);
		});

		out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
		out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
		out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

		out.readFromNBT(Platform.openNbtData(target).copy());
		return out;
	}

	@Override
	public String getEncryptionKey(final ItemStack item) {
		final NBTTagCompound tag = Platform.openNbtData(item);
		return tag.getString("encryptionKey");
	}

	@Override
	public void setEncryptionKey(final ItemStack item, final String encKey, final String name) {
		final NBTTagCompound tag = Platform.openNbtData(item);
		tag.setString("encryptionKey", encKey);
		tag.setString("name", name);
	}

	@Override
	public boolean canHandle(ItemStack is) {
		return is.getItem() == this;
	}

	private double injectPower(PowerUnits inputUnit, final ItemStack is, final double amount, final boolean simulate) {
		if (simulate) {
			final int requiredExt = (int) PowerUnits.AE.convertTo(inputUnit, getAEMaxPower(is) - getAECurrentPower(is));
			if (amount < requiredExt) {
				return 0;
			}
			return amount - requiredExt;
		}
		else {
			final double powerRemainder = injectAEPower(is, inputUnit.convertTo(PowerUnits.AE, amount), simulate ? Actionable.SIMULATE : Actionable.MODULATE);
			return PowerUnits.AE.convertTo(inputUnit, powerRemainder);
		}
	}

	@Override
	public double injectAEPower(final ItemStack is, final double amount, Actionable mode) {
		final double maxStorage = getAEMaxPower(is);
		final double currentStorage = getAECurrentPower(is);
		final double required = maxStorage - currentStorage;
		final double overflow = Math.min(amount * 2 - required, amount - required);

		if (mode == Actionable.MODULATE) {
			final NBTTagCompound data = Platform.openNbtData(is);
			final double toAdd = Math.min(amount * 2, required);

			data.setDouble("internalCurrentPower", currentStorage + toAdd);
		}

		return Math.max(0, overflow);
	}

	@Optional.Method(modid = "redstoneflux")
	@Override
	public int receiveEnergy(final ItemStack is, final int maxReceive, final boolean simulate) {
		return maxReceive - (int) injectPower(PowerUnits.RF, is, maxReceive, simulate);
	}

	@Optional.Method(modid = "redstoneflux")
	@Override
	public int extractEnergy(final ItemStack container, final int maxExtract, final boolean simulate) {
		return 0;
	}

	@Optional.Method(modid = "redstoneflux")
	@Override
	public int getEnergyStored(final ItemStack is) {
		return (int) PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(is));
	}

	@Optional.Method(modid = "redstoneflux")
	@Override
	public int getMaxEnergyStored(final ItemStack is) {
		return (int) PowerUnits.AE.convertTo(PowerUnits.RF, getAEMaxPower(is));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(ItemStack is) {
		if (ModConfig.USE_OLD_INFINTY_MECHANIC) {
			return checkForBooster(is);
		}
		return WCTUtils.hasInfiniteRange(is) && !WCTUtils.isInRange(is);
	}

	@Override
	public void onUpdate(final ItemStack wirelessTerminal, final World w, final Entity e, int i, boolean f) {
		if (!(e instanceof EntityPlayer)) {
			return;
		}
		EntityPlayer p = (EntityPlayer) e;
		if (entityPlayer == null) {
			entityPlayer = p;
		}
		//ItemStack wirelessTerminal = null;
		InventoryPlayer inv = p.inventory;
		//wirelessTerminal = WCTUtils.getWirelessTerm(inv);
		if (wirelessTerminal == null || !(wirelessTerminal.getItem() instanceof IWirelessCraftingTerminalItem)) {
			return;
		}
		if (p instanceof EntityPlayerMP) {
			rangeCheck(wirelessTerminal, (EntityPlayerMP) p);
		}
		WCTUtils.isBoosterInstalled(wirelessTerminal);
		WCTUtils.isMagnetInstalled(wirelessTerminal);
	}

	private void rangeCheck(ItemStack wirelessTerm, EntityPlayerMP player) {
		boolean inRange = WCTUtils.isInRangeOfWAP(wirelessTerm, player);
		boolean currentValue = WCTUtils.isInRange(wirelessTerm);
		if (inRange != currentValue) {
			WCTUtils.setInRange(wirelessTerm, inRange);
			ModNetworking.instance().sendTo(new PacketSetInRange(inRange), player);
		}
	}

	@Override
	public BaubleType getBaubleType(ItemStack itemstack) {
		return BaubleType.HEAD;
	}

	@Override
	public IBaubleRender getRender() {
		return RenderLayerWCT.getInstance();
	}

	@Override
	public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
		return true;
	}

	@Override
	public void onWornTick(ItemStack itemstack, EntityLivingBase playerIn) {
		if (playerIn instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) playerIn;
			rangeCheck(itemstack, player);
		}
	}

}
