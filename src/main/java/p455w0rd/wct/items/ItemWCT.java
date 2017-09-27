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
package p455w0rd.wct.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.util.IConfigManager;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AERootPoweredItem;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import baubles.api.BaubleType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.wct.api.IBaubleItem;
import p455w0rd.wct.api.IBaubleRender;
import p455w0rd.wct.api.IModelHolder;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.client.gui.WCTBaseGui;
import p455w0rd.wct.client.render.RenderLayerWCT;
import p455w0rd.wct.handlers.GuiHandler;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles|API")
public class ItemWCT extends AERootPoweredItem implements IModelHolder, IWirelessCraftingTerminalItem, IBaubleItem {

	private static final String name = "wct";
	public static final String LINK_KEY_STRING = "key";
	public static double GLOBAL_POWER_MULTIPLIER = PowerMultiplier.CONFIG.multiplier;
	private static final String BOOSTER_SLOT_NBT = "BoosterSlot";
	private static final String MAGNET_SLOT_NBT = "MagnetSlot";
	private EntityPlayer entityPlayer;

	public ItemWCT() {
		super(ModConfig.WCT_MAX_POWER);
		setRegistryName(name);
		setUnlocalizedName(name);
		setMaxStackSize(1);
		GameRegistry.register(this);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new ICapabilityProvider() {

			@Override
			public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
				return capability == CapabilityEnergy.ENERGY; // || stack.hasCapability(capability, facing);
			}

			@Override
			public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
				return capability != CapabilityEnergy.ENERGY ? null : CapabilityEnergy.ENERGY.cast(new IEnergyStorage() {

					ItemWCT item = (ItemWCT) stack.getItem();

					@Override
					public int receiveEnergy(int maxReceive, boolean simulate) {
						return item.receiveEnergy(stack, maxReceive, simulate);
					}

					@Override
					public int extractEnergy(int maxExtract, boolean simulate) {
						return 0;
					}

					@Override
					public int getEnergyStored() {
						return item.getEnergyStored(stack);
					}

					@Override
					public int getMaxEnergyStored() {
						return item.getMaxEnergyStored(stack);
					}

					@Override
					public boolean canExtract() {
						return false;
					}

					@Override
					public boolean canReceive() {
						return true;
					}

				});
			}

		};
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(final ItemStack itemStackIn, final World world, final EntityPlayer player, EnumHand hand) {
		if (!world.isRemote && hand == EnumHand.MAIN_HAND) {
			GuiHandler.open(GuiHandler.GUI_WCT, player, world, player.getPosition());
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		}
		if (world.isRemote) {
			WCTBaseGui.memoryText = "";
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStackIn);
	}

	@Override
	public boolean isDamageable() {
		return false;
	}

	@Override
	public void getCheckedSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> itemList) {
		itemList.add(new ItemStack(item));
		ItemStack is = new ItemStack(item);
		injectAEPower(is, ModConfig.WCT_MAX_POWER);
		itemList.add(is);
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
	public void addCheckedInformation(ItemStack is, EntityPlayer player, List<String> list, boolean displayMore) {
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
		if (isShiftKeyDown()) {
			String boosterStatus = (checkForBooster(is) ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format("tooltip.not.desc")) + " " + I18n.format("tooltip.installed.desc");
			String magnetStatus = (isMagnetInstalled(is) ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format("tooltip.not.desc")) + " " + I18n.format("tooltip.installed.desc");
			if (ModConfig.WCT_BOOSTER_ENABLED) {
				list.add(I18n.format("item.infinity_booster_card.name") + ": " + boosterStatus);
			}
			list.add(I18n.format("item.magnet_card.name") + ": " + magnetStatus);
		}
		else {
			list.add(shift);
		}
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
		return extractAEPower(is, amount) >= amount - 0.5;
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
		if (is.getItem().getRegistryName().toString().equals(getRegistryName().toString())) {
			return true;
		}
		return false;
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
			final double powerRemainder = injectAEPower(is, inputUnit.convertTo(PowerUnits.AE, amount));
			return PowerUnits.AE.convertTo(inputUnit, powerRemainder);
		}
	}

	@Override
	public int receiveEnergy(final ItemStack is, final int maxReceive, final boolean simulate) {
		return maxReceive - (int) injectPower(PowerUnits.RF, is, maxReceive, simulate);
	}

	@Override
	public int extractEnergy(final ItemStack container, final int maxExtract, final boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(final ItemStack is) {
		return (int) PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(is));
	}

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
		return checkForBooster(is);
	}

	@Override
	public boolean checkForBooster(final ItemStack wirelessTerminal) {
		if (wirelessTerminal.hasTagCompound()) {
			NBTTagList boosterNBTList = wirelessTerminal.getTagCompound().getTagList(BOOSTER_SLOT_NBT, 10);
			if (boosterNBTList != null) {
				NBTTagCompound boosterTagCompound = boosterNBTList.getCompoundTagAt(0);
				if (boosterTagCompound != null) {
					ItemStack boosterCard = ItemStack.loadItemStackFromNBT(boosterTagCompound);
					if (boosterCard != null) {
						return ((boosterCard.getItem() instanceof ItemInfinityBooster) && ModConfig.WCT_BOOSTER_ENABLED);
					}
				}
			}
		}
		return false;
	}

	private boolean isMagnetInstalled(final ItemStack wirelessTerminal) {
		if (wirelessTerminal.hasTagCompound()) {
			NBTTagList magnetNBTList = wirelessTerminal.getTagCompound().getTagList(MAGNET_SLOT_NBT, 10);
			if (magnetNBTList != null) {
				NBTTagCompound magnetTagCompound = magnetNBTList.getCompoundTagAt(0);
				if (magnetTagCompound != null) {
					ItemStack magnetCard = ItemStack.loadItemStackFromNBT(magnetTagCompound);
					if (magnetCard != null) {
						return ((magnetCard.getItem() instanceof ItemMagnet));
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onUpdate(final ItemStack is, final World w, final Entity e, int i, boolean f) {
		if (!(e instanceof EntityPlayer)) {
			return;
		}
		EntityPlayer p = (EntityPlayer) e;
		if (entityPlayer == null) {
			entityPlayer = p;
		}
		ItemStack wirelessTerminal = null;
		InventoryPlayer inv = p.inventory;
		wirelessTerminal = WCTUtils.getWirelessTerm(inv);
		if (wirelessTerminal == null || !(wirelessTerminal.getItem() instanceof IWirelessCraftingTerminalItem)) {
			return;
		}
		checkForBooster(wirelessTerminal);
	}

	//@Optional.Method(modid = "Baubles|API")
	@Override
	public BaubleType getBaubleType(ItemStack itemstack) {
		return BaubleType.HEAD;
	}

	//@Optional.Method(modid = "Baubles|API")
	@Override
	public IBaubleRender getRender() {
		return RenderLayerWCT.getInstance();
	}

	//@Optional.Method(modid = "Baubles|API")
	@Override
	public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
		return true;
	}

}
