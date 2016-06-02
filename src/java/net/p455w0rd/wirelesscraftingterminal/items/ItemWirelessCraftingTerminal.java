package net.p455w0rd.wirelesscraftingterminal.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Optional;

import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.util.IConfigManager;
import appeng.items.tools.powered.powersink.AERootPoweredItem;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;
import net.p455w0rd.wirelesscraftingterminal.api.WCTApi;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class ItemWirelessCraftingTerminal extends AERootPoweredItem implements IWirelessCraftingTerminalItem
{

	public static final String LINK_KEY_STRING = "key";
	public static double GLOBAL_POWER_MULTIPLIER = PowerMultiplier.CONFIG.multiplier;
	private static final String POWER_NBT_KEY = "internalCurrentPower";
	private static final String BOOSTER_SLOT_NBT = "BoosterSlot";
	private static final String MAGNET_SLOT_NBT = "MagnetSlot";
	private EntityPlayer entityPlayer;

	public ItemWirelessCraftingTerminal() {
		super(Reference.WCT_MAX_POWER, Optional.<String>absent());
		setUnlocalizedName("wirelessCraftingTerminal");
		setTextureName(Reference.MODID + ":wirelessCraftingTerminal");
		setMaxStackSize(1);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(ItemStack is, int pass) {
		return checkForBooster(is);
	}

	public boolean checkForBooster(final ItemStack wirelessTerminal) {
		if (wirelessTerminal.hasTagCompound()) {
			NBTTagList boosterNBTList = wirelessTerminal.getTagCompound().getTagList(BOOSTER_SLOT_NBT, 10);
			if (boosterNBTList != null) {
				NBTTagCompound boosterTagCompound = boosterNBTList.getCompoundTagAt(0);
				if (boosterTagCompound != null) {
					ItemStack boosterCard = ItemStack.loadItemStackFromNBT(boosterTagCompound);
					if (boosterCard != null) {
						return ((boosterCard.getItem() instanceof ItemInfinityBooster) && Reference.WCT_BOOSTER_ENABLED);
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
		if (this.entityPlayer == null) {
			this.entityPlayer = p;
		}
		ItemStack wirelessTerminal = null;
		InventoryPlayer inv = p.inventory;
		wirelessTerminal = RandomUtils.getWirelessTerm(inv);
		if (wirelessTerminal == null || !(wirelessTerminal.getItem() instanceof IWirelessCraftingTerminalItem)) {
			return;
		}
		checkForBooster(wirelessTerminal);
	}

	@Override
	public ItemStack onItemRightClick(final ItemStack itemStack, final World world, final EntityPlayer player) {
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			// Open the gui
			WCTApi.instance().interact().openWirelessCraftingTerminalGui(player);
		}
		return itemStack;
	}

	@Override
	public String getEncryptionKey(final ItemStack wirelessTerminal) {
		if (wirelessTerminal == null) {
			return null;
		}
		// Ensure the terminal has a tag
		if (wirelessTerminal.hasTagCompound()) {
			// Get the security terminal source key
			String sourceKey = wirelessTerminal.getTagCompound().getString(LINK_KEY_STRING);

			// Ensure the source is not empty nor null
			if ((sourceKey != null) && (!sourceKey.isEmpty())) {
				// The terminal is linked.
				return sourceKey;
			}
		}

		// Terminal is unlinked.
		return "";
	}

	@Override
	public void setEncryptionKey(final ItemStack wirelessTerminal, final String sourceKey, final String name) {
		final NBTTagCompound tag = ensureTagCompound(wirelessTerminal);
		tag.setString(ItemWirelessCraftingTerminal.LINK_KEY_STRING, sourceKey);
	}

	@Override
	public boolean canHandle(final ItemStack is) {
		return true;
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
		final ConfigManager out = new ConfigManager(new IConfigManagerHost() {

			@Override
			public void updateSetting(final IConfigManager manager, @SuppressWarnings("rawtypes") final Enum settingName, @SuppressWarnings("rawtypes") final Enum newValue) {
				final NBTTagCompound data = Platform.openNbtData(target);
				manager.writeToNBT(data);
			}
		});

		out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
		out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
		out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

		out.readFromNBT((NBTTagCompound) Platform.openNbtData(target).copy());
		return out;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getCheckedSubItems(Item item, CreativeTabs creativeTab, List itemList) {
		List itemList2 = itemList;
		itemList2.add(new ItemStack(item));
		ItemStack is = new ItemStack(item);
		injectAEPower(is, Reference.WCT_MAX_POWER);
		itemList2.add(is);
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


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SideOnly(Side.CLIENT)
	@Override
	public void addCheckedInformation(ItemStack is, EntityPlayer player, List list, boolean displayMore) {
		String shift = LocaleHandler.PressShift.getLocal().replace("Shift", color("yellow") + "" + color("bold") + "" + color("italics") + "Shift" + color("gray"));
		final NBTTagCompound tag = ensureTagCompound(is);

		String encKey = tag.getString("key");
		String pctTxtColor = color("white") + "";
		double aeCurrPower = getAECurrentPower(is);
		double aeCurrPowerPct = (int) Math.floor(aeCurrPower / Reference.WCT_MAX_POWER * 1e4) / 1e2;
		if ((int) aeCurrPowerPct >= 75) {
			pctTxtColor = color("green") + "";
		}
		if ((int) aeCurrPowerPct <= 5) {
			pctTxtColor = color("red") + "";
		}
		list.add(color("aqua") + "==============================");
		list.add(StatCollector.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + pctTxtColor + (int) aeCurrPower + " AE - " + aeCurrPowerPct + "%");
		if (isShiftKeyDown()) {
			String linked = color("red") + StatCollector.translateToLocal("gui.appliedenergistics2.Unlinked");
			if (encKey != null && !encKey.isEmpty()) {
				linked = color("blue") + StatCollector.translateToLocal("gui.appliedenergistics2.Linked");
			}
			list.add(LocaleHandler.LinkStatus.getLocal() + ": " + linked);
			String boosterStatus = (checkForBooster(is) ? color("green") + "" + LocaleHandler.Installed.getLocal() : color("red") + "" + LocaleHandler.NotInstalled.getLocal());
			String magnetStatus = (isMagnetInstalled(is) ? color("green") + "" + LocaleHandler.Installed.getLocal() : color("red") + "" + LocaleHandler.NotInstalled.getLocal());
			if (Reference.WCT_BOOSTER_ENABLED) {
				list.add(getItemName("infinityBoosterCard") + ": " + boosterStatus);
			}
			list.add(getItemName("magnetCard") + ": " + magnetStatus);
		}
		else {
			list.add(shift);
		}
	}

	@SideOnly(Side.CLIENT)
	private String color(String color) {
		return RandomUtils.color(color);
	}

	@SideOnly(Side.CLIENT)
	private static String getItemName(String item) {
		return StatCollector.translateToLocal("item." + item + ".name");
	}

	@SideOnly(Side.CLIENT)
	public static boolean isShiftKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

	}

	private double getInternalBattery(final ItemStack is, final batteryOperation op, final double adjustment) {
		final NBTTagCompound data = ensureTagCompound(is);

		double currentStorage = data.getDouble(POWER_NBT_KEY);
		final double maxStorage = getAEMaxPower(is);

		switch (op) {
		case INJECT:
			currentStorage += adjustment;
			if (currentStorage > maxStorage) {
				final double diff = currentStorage - maxStorage;
				data.setDouble(POWER_NBT_KEY, maxStorage);
				return diff;
			}
			data.setDouble(POWER_NBT_KEY, currentStorage);
			return 0;
		case EXTRACT:
			if (currentStorage > adjustment) {
				currentStorage -= adjustment;
				data.setDouble(POWER_NBT_KEY, currentStorage);
				return adjustment;
			}
			data.setDouble(POWER_NBT_KEY, 0);
			return currentStorage;
		default:
			break;
		}

		return currentStorage;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack is) {
		return 1 - getAECurrentPower(is) / getAEMaxPower(is);
	}

	private NBTTagCompound ensureTagCompound(ItemStack is) {
		if (!is.hasTagCompound()) {
			is.setTagCompound(new NBTTagCompound());
		}
		return is.getTagCompound();
	}

	@Override
	public double getAECurrentPower(final ItemStack is) {
		return getInternalBattery(is, batteryOperation.STORAGE, 0);
	}

	@Override
	public AccessRestriction getPowerFlow(ItemStack is) {
		return AccessRestriction.WRITE;
	}

	@Override
	public double injectAEPower(final ItemStack is, final double amt) {
		return getInternalBattery(is, batteryOperation.INJECT, amt);
	}

	@Override
	public double extractAEPower(final ItemStack is, final double amt) {
		int finalAmt = 0;
		if (this.entityPlayer != null) {
			finalAmt = (int) (this.entityPlayer.capabilities.isCreativeMode ? 0 : amt);
		}
		return getInternalBattery(is, batteryOperation.EXTRACT, finalAmt);
	}

	@Override
	public double getAEMaxPower(ItemStack is) {
		return Reference.WCT_MAX_POWER;
	}

	private enum batteryOperation {
		STORAGE, INJECT, EXTRACT
	}

	@Override
	public boolean isDamageable() {
		return false;
	}

	@Override
	public boolean isDamaged(final ItemStack stack) {
		return false;
	}

	@Override
	public boolean isRepairable() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isFull3D() {
		return false;
	}

	@Override
	public int getMaxDamage() {
		return 0;
	}

	@Override
	public int getDamage(ItemStack stack) {
		return 0;
	}
	
	//RF Integration
	@Override
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
		if (container == null) {
			return 0;
		}
		if (simulate) {
			double current = PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(container));
			double max = PowerUnits.AE.convertTo(PowerUnits.RF, getAEMaxPower(container));
			if (max - current >= maxReceive) {
				return maxReceive;
			}
			else {
				return (int) (max - current);
			}
		}
		else {
			double currentAEPower = getAECurrentPower(container);
			if ((int) currentAEPower < Reference.WCT_MAX_POWER) {
				int leftOver = (int) PowerUnits.AE.convertTo(PowerUnits.RF, injectAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, maxReceive)));
				return (int) maxReceive - leftOver;
			}
			else {
				return 0;
			}
		}
	}

	@Override
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
		if (container == null) {
			return 0;
		}
		if (simulate) {
			if (getEnergyStored(container) >= maxExtract) {
				return maxExtract;
			}
			else {
				return getEnergyStored(container);
			}
		}
		else {
			return (int) PowerUnits.AE.convertTo(PowerUnits.RF, extractAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, maxExtract)));
		}
	}

	@Override
	public int getEnergyStored(ItemStack container) {
		return (int) PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(container));
	}

	@Override
	public int getMaxEnergyStored(ItemStack container) {
		return (int) PowerUnits.AE.convertTo(PowerUnits.RF, getAEMaxPower(container));
	}

}
