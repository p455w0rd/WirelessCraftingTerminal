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

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;

import com.google.common.base.Splitter;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.*;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import p455w0rd.ae2wtlib.api.*;
import p455w0rd.ae2wtlib.api.item.ItemBase;
import p455w0rd.ae2wtlib.api.networking.security.WTPlayerSource;
import p455w0rd.ae2wtlib.items.ItemWUT;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.WCTApi;
import p455w0rd.wct.init.*;
import p455w0rd.wct.sync.packets.PacketMagnetFilterHeld;
import p455w0rd.wct.sync.packets.PacketSetMagnetHeld;
import p455w0rd.wct.util.WCTUtils;
import p455w0rdslib.util.EntityItemUtils;
import p455w0rdslib.util.ItemUtils;

/**
 * @author p455w0rd
 *
 */
@SuppressWarnings("deprecation")
public class ItemMagnet extends ItemBase {

	public static final String MAGNET_SLOT_NBT = "MagnetSlot";
	public static final String MAGNET_FILTER_NBT = "MagnetFilter";
	public static final String TIMER_RESET_NBT = "WCTReset";
	public static final String TIMER_PICKUP_NBT = "WCTPickupTimer";
	public static final String MAGNET_MODE_NBT = "MagnetMode";
	public static final String WHITELISTING_NBT = "Whitelisting";
	public static final String IGNORE_NBT = "IgnoreNBT";
	public static final String IGNORE_META_NBT = "IgnoreMeta";
	public static final String USE_OREDICT_NBT = "UseOreDict";
	public static final String INIT_NBT = "Initialized";
	public static final String ITEMS_NBT = "Items";

	private int distanceFromPlayer;

	private static final String name = "magnet_card";

	public ItemMagnet() {
		super(new ResourceLocation(ModGlobals.MODID, name));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(@Nonnull final ItemStack item) {
		return isActivated(item);
	}

	@Override
	public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged) {
		return slotChanged;
	}

	@Override
	public boolean isDamageable() {
		return false;
	}

	/*
	public ItemStack getItemStack() {
		if (thisItemStack != null && (thisItemStack.getItem() == ModItems.MAGNET_CARD)) {
			return thisItemStack;
		}
		return ItemStack.EMPTY;
	}
	*/

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull final ItemStack is, @Nullable final World worldIn, final List<String> list, final ITooltipFlag flagIn) {
		list.add(color("aqua") + "==============================");
		final String shift = I18n.translateToLocal("tooltip.press_shift.desc").replace("Shift", color("yellow") + color("bold") + color("italics") + "Shift" + color("gray"));
		if (isShiftKeyDown()) {

			final String info = I18n.translateToLocal("tooltip.magnet.desc");
			for (final String line : Splitter.on("\n").split(WordUtils.wrap(info, 37, "\n", false))) {
				list.add(line.trim());
			}

			list.add("");
			list.add(color("italics") + "" + I18n.translateToLocal("tooltip.magnet_set_filter.desc"));
			/*
			if (isActivated(itemStack)) {
				String boundKey = ModKeybindings.openMagnetFilter.getDisplayName();
				if (!boundKey.equals("NONE")) {
					list.add(color("italics") + I18n.translateToLocal("tooltip.or_press.desc") + " " + color("yellow") + color("bold") + "[" + boundKey + "]");
				}
				String boundKey2 = ModKeybindings.changeMagnetMode.getDisplayName();
				if (!boundKey2.equals("NONE")) {
					list.add(color("italics") + I18n.translateToLocal("tooltip.press.desc") + " " + color("yellow") + color("bold") + "[" + boundKey2 + "] " + color("gray") + color("italics") + I18n.translateToLocal("tooltip.to_switch.desc"));
				}
			}
			*/
			list.add("");
			final String not = I18n.translateToLocal("tooltip.not.desc");
			list.add(I18n.translateToLocal("tooltip.status.desc") + ": " + (isActivated(is) ? color("green") + I18n.translateToLocal("tooltip.active.desc") : color("red") + not + " " + I18n.translateToLocal("tooltip.active.desc")));
			final MagnetFunctionMode mode = getMagnetFunctionMode(is);
			if (mode != MagnetFunctionMode.INACTIVE) {
				list.add(color("white") + "  " + mode.getMessage().replace("-", "\n  -"));
			}

			final String white = I18n.translateToLocal("tooltip.magnet_whitelisting.desc");
			final String black = I18n.translateToLocal("tooltip.magnet_blacklisting.desc");

			list.add(I18n.translateToLocal("tooltip.filter_mode.desc") + ": " + color("white") + (getListMode(is) ? white : black));

			final String ignoring = I18n.translateToLocal("tooltip.ignoring.desc");
			final String nbtData = I18n.translateToLocal("tooltip.nbt.desc");
			final String metaData = I18n.translateToLocal("tooltip.meta.desc");
			final String usingOreDict = I18n.translateToLocal("tooltip.using.desc") + " " + I18n.translateToLocal("tooltip.oredict.desc");

			list.add((!doesMagnetUseOreDict(is) ? " " + not : color("green")) + " " + usingOreDict);
			list.add((!doesMagnetIgnoreNBT(is) ? " " + not : color("green")) + " " + ignoring + " " + nbtData);
			list.add((!doesMagnetIgnoreMeta(is) ? " " + not : color("green")) + " " + ignoring + " " + metaData);

			final List<ItemStack> filteredItems = getFilteredItems(is);

			if (filteredItems != null) {
				list.add("");
				list.add(color("gray") + I18n.translateToLocal("tooltip.filtered_items.desc") + ":");
				for (int i = 0; i < filteredItems.size(); i++) {
					list.add("  " + filteredItems.get(i).getDisplayName());
				}
			}

			list.add("");
			final String onlyWorks = I18n.translateToLocal("tooltip.only_works.desc");
			for (final String line : Splitter.on("\n").split(WordUtils.wrap(onlyWorks, 27, "\n", false))) {
				list.add(color("white") + color("bold") + color("italics") + line.trim());
			}

		}
		else {
			list.add(shift);
		}
	}

	@SideOnly(Side.CLIENT)
	private String color(final String color) {
		return WTApi.instance().color(color);
	}

	@SideOnly(Side.CLIENT)
	public static boolean isShiftKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
		final ItemStack item = player.getHeldItem(hand);
		if (world.isRemote && hand == EnumHand.MAIN_HAND && !item.isEmpty()) {
			if (!isMagnetInitialized(item)) {
				ModNetworking.instance().sendToServer(new PacketMagnetFilterHeld(MagnetItemMode.INIT, true));
			}
			if (player.isSneaking()) {
				cycleMagnetFunctionModeHeld(player);
				return new ActionResult<>(EnumActionResult.SUCCESS, item);
			}
			else {
				WCTApi.instance().openMagnetGui(player, false, -1);
				return new ActionResult<>(EnumActionResult.SUCCESS, item);
			}
		}
		return new ActionResult<>(EnumActionResult.FAIL, item);
	}

	public void doMagnet(final EntityPlayer player, @Nonnull final ItemStack wirelessTerminal, final boolean isWCTBauble, final int wctSlot) {
		final World world = player.getEntityWorld();
		final ItemStack magnet = getMagnetFromWCT(wirelessTerminal);
		if (world.isRemote || magnet.isEmpty() || !isActivated(magnet) || player == null || player.isSneaking()) {
			return;
		}
		distanceFromPlayer = 6;
		final NonNullList<ItemStack> filteredList = getFilteredItems(magnet);
		// items

		Iterator<Entity> iterator = getEntitiesInRange(EntityItem.class, world, player.getPosition(), distanceFromPlayer).iterator();
		while (iterator.hasNext()) {
			final EntityItem itemToGet = (EntityItem) iterator.next();
			if (itemToGet == null) {
				return;
			}
			if (EntityItemUtils.getThrowerName(itemToGet) != null && EntityItemUtils.getThrowerName(itemToGet).equals(player.getName()) && !EntityItemUtils.canPickup(itemToGet)) {
				continue;
			}
			// Demagnetize integration
			if (itemToGet.getEntityData().hasKey("PreventRemoteMovement")) {
				return;
			}
			final ItemStack itemStackToGet = itemToGet.getItem();
			if (itemStackToGet.isEmpty()) {
				return;
			}
			final int stackSize = itemStackToGet.getCount();
			final WTGuiObject<IAEItemStack> obj = getGuiObject(wirelessTerminal, player);
			final boolean ignoreRange = WTApi.instance().hasInfiniteRange(wirelessTerminal);
			final boolean hasAxxess = hasNetworkAccess(SecurityPermissions.INJECT, true, player, wirelessTerminal);
			if (ignoreRange && hasAxxess || obj.rangeCheck() && hasAxxess) {
				final IAEItemStack ais = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(itemStackToGet);
				ais.setStackSize(stackSize);
				if (!itemToGet.isDead) {

					// whitelist
					if (getListMode(magnet)) {
						if (isItemFiltered(magnet, itemStackToGet, filteredList) && !filteredList.isEmpty()) {
							if (doInject(ais, stackSize, player, wirelessTerminal, itemToGet, isWCTBauble, wctSlot)) {
								itemToGet.setDead();
							}
							continue;
						}
						else if (getMagnetFunctionMode(magnet) == MagnetFunctionMode.ACTIVE_KEEP_IN_INVENTORY) {
							doInventoryPickup(itemToGet, player, itemStackToGet, world, stackSize);
						}
					}
					// blacklist
					else {
						if (!isItemFiltered(magnet, itemStackToGet, filteredList) || filteredList.isEmpty()) {
							if (doInject(ais, stackSize, player, wirelessTerminal, itemToGet, isWCTBauble, wctSlot)) {
								itemToGet.setDead();
							}
							continue;
						}
						else if (getMagnetFunctionMode(magnet) == MagnetFunctionMode.ACTIVE_KEEP_IN_INVENTORY) {
							doInventoryPickup(itemToGet, player, itemStackToGet, world, stackSize);
						}
					}
				}
			}
		}

		// xp
		iterator = getEntitiesInRange(EntityXPOrb.class, world, player.getPosition(), distanceFromPlayer).iterator();
		while (iterator.hasNext()) {
			final EntityXPOrb xpToGet = (EntityXPOrb) iterator.next();
			if (xpToGet.isDead || xpToGet.isInvisible()) {
				continue;
			}
			int xpAmount = xpToGet.xpValue;
			xpToGet.setDead();
			world.playSound((EntityPlayer) null, xpToGet.posX, xpToGet.posY, xpToGet.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
			final ItemStack itemstack = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, player);
			if (!itemstack.isEmpty() && itemstack.isItemDamaged()) {
				final int i = Math.min(xpToDurability(xpAmount), itemstack.getItemDamage());
				xpAmount -= durabilityToXp(i);
				itemstack.setItemDamage(itemstack.getItemDamage() - i);
			}
			if (xpAmount > 0) {
				player.addExperience(xpAmount);
			}
		}
	}

	private boolean doInject(IAEItemStack ais, final int stackSize, final EntityPlayer player, final ItemStack wirelessTerminal, final EntityItem itemToGet, final boolean isWCTBauble, final int wctSlot) {
		final World world = player.getEntityWorld();
		final InventoryPlayer playerInv = player.inventory;
		final int invSize = playerInv.getSizeInventory();
		if (invSize >= 0 && !WTApi.instance().getConfig().isOldInfinityMechanicEnabled() && !wirelessTerminal.isEmpty() && WTApi.instance().shouldConsumeBoosters(wirelessTerminal)) {
			final ItemStack pickupStack = itemToGet.getItem();
			if (!pickupStack.isEmpty() && pickupStack.getItem() == WTApi.instance().getBoosterCard()) {
				WTApi.instance().addInfinityBoosters(wirelessTerminal, pickupStack);
				WTApi.instance().getNetHandler().sendToDimension(WTApi.instance().getNetHandler().createInfinityEnergySyncPacket(WTApi.instance().getInfinityEnergy(wirelessTerminal), player.getUniqueID(), isWCTBauble, wctSlot), player.getEntityWorld().provider.getDimension());
				return true;
			}
		}
		ItemStack tempTerminal = wirelessTerminal.copy();
		if (WTApi.instance().getWUTUtility().isWUT(wirelessTerminal)) {
			tempTerminal = getWCTFromWUT(wirelessTerminal);
		}
		final WTGuiObject<IAEItemStack> obj = getGuiObject(tempTerminal, player);
		final IEnergySource powerSrc = obj;
		final IActionSource mySrc = new WTPlayerSource(player, obj);
		ais = Platform.poweredInsert(powerSrc, obj, ais, mySrc);
		final ItemStack magnet = getMagnetFromWCT(wirelessTerminal);
		if (ais != null && !magnet.isEmpty() && getMagnetFunctionMode(magnet) == MagnetFunctionMode.ACTIVE_KEEP_IN_INVENTORY) {
			player.onItemPickup(itemToGet, stackSize);
			player.inventory.addItemStackToInventory(itemToGet.getItem());
			world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.getEntityWorld().rand.nextFloat() - player.getEntityWorld().rand.nextFloat()) * 0.7F + 2F));
		}
		return ais == null;
	}

	private int durabilityToXp(final int durability) {
		return durability / 2;
	}

	private int xpToDurability(final int xp) {
		return xp * 2;
	}

	private void doInventoryPickup(final EntityItem itemToGet, final EntityPlayer player, final ItemStack itemStackToGet, final World world, final int stackSize) {
		if (itemToGet.getDistance(player) <= distanceFromPlayer) {
			if (player.inventory.addItemStackToInventory(itemStackToGet)) {
				player.onItemPickup(itemToGet, stackSize);
				world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.getEntityWorld().rand.nextFloat() - player.getEntityWorld().rand.nextFloat()) * 0.7F + 2F));
			}
		}
	}

	private static boolean doesMagnetIgnoreNBT(final ItemStack magnet) {
		return ItemUtils.readBoolean(magnet, IGNORE_NBT);
	}

	private static boolean doesMagnetIgnoreMeta(final ItemStack magnet) {
		return ItemUtils.readBoolean(magnet, IGNORE_META_NBT);
	}

	private static boolean doesMagnetUseOreDict(final ItemStack magnet) {
		return ItemUtils.readBoolean(magnet, USE_OREDICT_NBT);
	}

	private static boolean areOresEqual(final ItemStack is1, final ItemStack is2) {
		final int[] list1 = OreDictionary.getOreIDs(is1);
		final int[] list2 = OreDictionary.getOreIDs(is2);
		for (final int element : list1) {
			for (final int element2 : list2) {
				if (element == element2) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isItemFiltered(@Nonnull final ItemStack magnet, @Nonnull final ItemStack is, final NonNullList<ItemStack> itemList) {
		if (!is.isEmpty() && !itemList.isEmpty()) {
			for (int i = 0; i < itemList.size(); i++) {
				final ItemStack thisStack = itemList.get(i);
				//use oredict
				if (doesMagnetUseOreDict(magnet)) {
					if (areOresEqual(is, thisStack)) {
						return true;
					}
				}
				//ignore meta & nbt
				if (doesMagnetIgnoreMeta(magnet) && doesMagnetIgnoreNBT(magnet)) {
					if (is.getItem().equals(thisStack.getItem())) {
						return true;
					}
				}
				//ignore meta only
				else if (doesMagnetIgnoreMeta(magnet) && !doesMagnetIgnoreNBT(magnet)) {
					if (ItemStack.areItemStackTagsEqual(is, thisStack) && is.getItem() == thisStack.getItem()) {
						return true;
					}
				}
				//ignore nbt only
				else if (!doesMagnetIgnoreMeta(magnet) && doesMagnetIgnoreNBT(magnet) && is.getItem() == thisStack.getItem()) {
					if (isMetaEqual(is, thisStack)) {
						return true;
					}
				}
				//ignore nothing/don't use oredict--must be exact match
				else {
					if (isMetaEqual(is, thisStack) && ItemStack.areItemStackTagsEqual(is, thisStack) && is.getItem() == thisStack.getItem()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean isMetaEqual(@Nonnull final ItemStack is1, @Nonnull final ItemStack is2) {
		return is1.getItemDamage() == is2.getItemDamage();
	}

	private boolean hasNetworkAccess(final SecurityPermissions perm, final boolean requirePower, final EntityPlayer player, @Nonnull final ItemStack wirelessTerm) {
		if (player.capabilities.isCreativeMode) {
			return true;
		}
		final WTGuiObject<IAEItemStack> obj = getGuiObject(wirelessTerm, player);
		final IGrid g = obj.getTargetGrid();
		if (g != null) {
			if (requirePower) {
				final IEnergyGrid eg = g.getCache(IEnergyGrid.class);
				if (!eg.isNetworkPowered()) {
					return false;
				}
			}

			final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
			if (sg.hasPermission(player, perm)) {
				return true;
			}
		}
		return false;
	}

	public static NonNullList<ItemStack> getFilteredItems(@Nonnull final ItemStack magnet) {
		if (magnet.isEmpty()) {
			return NonNullList.create();
		}
		if (magnet.getItem() == ModItems.MAGNET_CARD) {
			if (magnet.hasTagCompound()) {
				final NBTTagCompound nbtTC = magnet.getTagCompound();
				if (!nbtTC.hasKey("MagnetFilter")) {
					return NonNullList.create();
				}
				final NBTTagList tagList = nbtTC.getTagList(MAGNET_FILTER_NBT, 10);
				if (tagList.tagCount() > 0 && tagList != null) {
					final NonNullList<ItemStack> itemList = NonNullList.create();
					for (int i = 0; i < tagList.tagCount(); i++) {
						itemList.add(new ItemStack(tagList.getCompoundTagAt(i)));
					}
					return itemList;
				}
			}
		}
		return NonNullList.create();
	}

	@Nonnull
	public static ItemStack getMagnetFromWCT(@Nonnull final ItemStack wirelessTerm) {
		if (!wirelessTerm.isEmpty() && wirelessTerm.hasTagCompound() && (wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem || WTApi.instance().getWUTUtility().doesWUTSupportType(wirelessTerm, IWirelessCraftingTerminalItem.class))) {
			if (WTApi.instance().getWUTUtility().isWUT(wirelessTerm)) {
				//wirelessTerm = getWCTFromWUT(wirelessTerm);
			}
			final NBTTagCompound magnetNBT = wirelessTerm.getSubCompound(MAGNET_SLOT_NBT);
			if (magnetNBT != null) {
				final NBTTagList magnetSlot = magnetNBT.getTagList(ITEMS_NBT, 10);
				final ItemStack magnetItem = new ItemStack(magnetSlot.getCompoundTagAt(0));
				if (magnetItem != null && !magnetItem.isEmpty() && magnetItem.getItem() == ModItems.MAGNET_CARD) {
					return magnetItem;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	private static ItemStack getWCTFromWUT(final ItemStack wut) {
		if (!wut.isEmpty() && wut.hasTagCompound() && WTApi.instance().getWUTUtility().doesWUTSupportType(wut, IWirelessCraftingTerminalItem.class) && WTApi.instance().getWUTUtility().isWUT(wut)) {
			final ItemStack wct = ItemWUT.getStoredTerminalByHandler(wut, IWirelessCraftingTerminalItem.class);
			if (WCTUtils.isAnyWCT(wct, true) && !WTApi.instance().getWUTUtility().isWUT(wct)) {
				return wct;
			}
		}
		return ItemStack.EMPTY;
	}

	@Nonnull
	public static ItemStack getMagnetFromInv(@Nonnull final InventoryPlayer inv, final int slot) {
		final ItemStack magnetItem = inv.getStackInSlot(slot);
		if (magnetItem != null && !magnetItem.isEmpty() && magnetItem.getItem() == ModItems.MAGNET_CARD) {
			return magnetItem;
		}
		return ItemStack.EMPTY;
	}

	// true=whitelist (default:whitelist)
	private boolean getListMode(@Nonnull final ItemStack magnet) {
		if (magnet.getItem() == ModItems.MAGNET_CARD) {
			return getItemMode(magnet, 1);
		}
		return true;
	}

	public static boolean getItemMode(@Nonnull final ItemStack magnet, final int type) {
		if (!magnet.hasTagCompound()) {
			magnet.setTagCompound(new NBTTagCompound());
		}
		final NBTTagCompound nbtTC = magnet.getTagCompound();
		if (type == 1) {
			if (nbtTC.hasKey(WHITELISTING_NBT)) {
				return nbtTC.getBoolean(WHITELISTING_NBT);
			}
			return true;
		}
		else if (type == 2) {
			if (nbtTC.hasKey(IGNORE_NBT)) {
				return nbtTC.getBoolean(IGNORE_NBT);
			}
			return false;
		}
		else if (type == 3) {
			if (nbtTC.hasKey(IGNORE_META_NBT)) {
				return nbtTC.getBoolean(IGNORE_META_NBT);
			}
			return false;
		}
		else if (type == 4) {
			if (nbtTC.hasKey(USE_OREDICT_NBT)) {
				return nbtTC.getBoolean(USE_OREDICT_NBT);
			}
			return false;
		}
		return true;
	}

	public static void setItemMode(@Nonnull final ItemStack magnet, final MagnetItemMode whichMode, final boolean isActive) {
		if (!magnet.isEmpty()) {
			if (!magnet.hasTagCompound()) {
				magnet.setTagCompound(new NBTTagCompound());
			}
			magnet.getTagCompound().setBoolean(whichMode.getNBTKey(), isActive);
		}
	}

	@SuppressWarnings("unchecked")
	private WTGuiObject<IAEItemStack> getGuiObject(@Nonnull final ItemStack it, final EntityPlayer player) {
		if (!it.isEmpty()) {
			final ICustomWirelessTerminalItem wh = (ICustomWirelessTerminalItem) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return (WTGuiObject<IAEItemStack>) WTApi.instance().getGUIObject(wh, it, player);
			}
		}
		return null;
	}

	public static List<Entity> getEntitiesInRange(final Class<? extends Entity> entityType, final World world, final BlockPos playerPos, final int distance) {
		return world.getEntitiesWithinAABB(entityType, new AxisAlignedBB(playerPos.down(distance).west(distance).south(distance), playerPos.up(distance).east(distance).north(distance)));
	}

	@Nonnull
	public ItemStack getStack() {
		return this.getStack(1);
	}

	@Nonnull
	public ItemStack getStack(final int size) {
		return new ItemStack(this, size);
	}

	public static boolean isActivated(@Nonnull final ItemStack magnet) {
		return isActivated(magnet, false);
	}

	public static boolean isActivated(@Nonnull final ItemStack stack, final boolean isWCT) {
		final ItemStack magnet = isWCT ? getMagnetFromWCT(stack) : stack;
		if (magnet.isEmpty()) {
			return false;
		}
		return getMagnetFunctionMode(magnet) != MagnetFunctionMode.INACTIVE;
	}

	@Override
	public int getMaxItemUseDuration(@Nonnull final ItemStack stack) {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initModel() {
		for (int i = 0; i < 3; i++) {
			ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(getRegistryName(), "inventory"));
		}
	}

	public static MagnetFunctionMode getMagnetFunctionMode(@Nonnull final ItemStack magnet) {
		if (!magnet.isEmpty()) {
			if (!magnet.hasTagCompound()) {
				magnet.setTagCompound(new NBTTagCompound());
			}
			final NBTTagCompound nbt = magnet.getTagCompound();
			if (!nbt.hasKey(MAGNET_MODE_NBT)) {
				nbt.setInteger(MAGNET_MODE_NBT, 0);
			}
			final int val = nbt.getInteger(MAGNET_MODE_NBT);
			return MagnetFunctionMode.VALUES[val];
		}
		return MagnetFunctionMode.INACTIVE;
	}

	@SideOnly(Side.CLIENT)
	public static void displayMessage(final MagnetFunctionMode mode) {
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString(mode.getMessage()));
	}

	public static void setMagnetFunctionMode(@Nonnull final ItemStack stack, final MagnetFunctionMode mode) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger(MAGNET_MODE_NBT, mode.ordinal());
	}

	public static MagnetFunctionMode cycleMagnetFunctionMode(@Nonnull final ItemStack magnet) {
		if (!magnet.isEmpty()) {
			int newMode = getMagnetFunctionMode(magnet).ordinal() + 1;
			final int max = MagnetFunctionMode.VALUES.length - 1;
			if (newMode > max) {
				newMode = 0;
			}
			setMagnetFunctionMode(magnet, MagnetFunctionMode.VALUES[newMode]);
			return MagnetFunctionMode.VALUES[newMode];
		}
		return MagnetFunctionMode.INACTIVE;
	}

	public static MagnetFunctionMode cycleMagnetFunctionModeWCT(final EntityPlayer player, final int wctSlot, final boolean isBauble) {
		final ItemStack magnet = getMagnetFromWCT(isBauble ? WTApi.instance().getBaublesUtility().getWTBySlot(player, wctSlot, IWirelessCraftingTerminalItem.class) : WCTUtils.getWCTBySlot(player, wctSlot, false));
		if (player == null || magnet.isEmpty() || wctSlot < 0) {
			return MagnetFunctionMode.INACTIVE;
		}
		if (!magnet.hasTagCompound()) {
			setItemMode(magnet, MagnetItemMode.INIT, true);
		}
		final MagnetFunctionMode mode = cycleMagnetFunctionMode(magnet);
		if (!(player instanceof EntityPlayerMP)) {
			//displayMessage(mode);
		}
		return mode;
	}

	public static void cycleMagnetFunctionModeHeld(@Nonnull final EntityPlayer player) {
		if (player == null || player.getHeldItemMainhand().isEmpty() || player.getHeldItemMainhand().getItem() != ModItems.MAGNET_CARD) {
			return;
		}
		final ItemStack magnet = getHeldMagnet(player);
		if (magnet.isEmpty()) {
			return;
		}
		if (!magnet.hasTagCompound()) {
			ItemMagnet.setItemMode(magnet, MagnetItemMode.INIT, true);
		}
		final MagnetFunctionMode mode = cycleMagnetFunctionMode(magnet);
		if (!(player instanceof EntityPlayerMP)) {
			displayMessage(mode);
			ModNetworking.instance().sendToServer(new PacketSetMagnetHeld(mode));
		}
	}

	@Nonnull
	public static ItemStack getHeldMagnet(final EntityPlayer player) {
		if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == ModItems.MAGNET_CARD) {
			return player.getHeldItemMainhand();
		}
		return ItemStack.EMPTY;
	}

	public static boolean isMagnetInitialized(@Nonnull final ItemStack magnetItem) {
		if (!magnetItem.isEmpty() && magnetItem.getItem() == ModItems.MAGNET_CARD) {
			if (!magnetItem.hasTagCompound()) {
				magnetItem.setTagCompound(new NBTTagCompound());
			}
			if (!magnetItem.getTagCompound().hasKey(INIT_NBT)) {
				magnetItem.getTagCompound().setBoolean(INIT_NBT, true);
			}
		}
		return magnetItem.getTagCompound().getBoolean(INIT_NBT);
	}

	public static boolean isMagnetInstalled(final EntityPlayer player, final boolean isBauble, final int slot) {
		return isMagnetInstalled(isBauble ? WTApi.instance().getBaublesUtility().getWTBySlot(player, slot, IWirelessCraftingTerminalItem.class) : WCTUtils.getWCTBySlot(player, slot, false));
	}

	public static boolean isMagnetInstalled(final ItemStack wirelessTerm) {
		if (!wirelessTerm.isEmpty() && WCTUtils.isAnyWCT(wirelessTerm) && wirelessTerm.hasTagCompound() && wirelessTerm.getTagCompound().hasKey(MAGNET_SLOT_NBT)) {
			final NBTTagCompound magnetNBT = wirelessTerm.getSubCompound(MAGNET_SLOT_NBT);
			if (magnetNBT != null) {
				final NBTTagList magnetList = magnetNBT.getTagList(ITEMS_NBT, 10);
				if (magnetList != null && !magnetList.hasNoTags()) {
					final NBTTagCompound magnetNBTForm = magnetList.getCompoundTagAt(0);
					if (magnetNBTForm != null) {
						final ItemStack magnetItem = new ItemStack(magnetNBTForm);
						if (!magnetItem.isEmpty() && magnetItem.getItem() instanceof ItemMagnet) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static void removeTimerTags(final ItemStack is) {
		if (is.isEmpty() || !is.hasTagCompound()) {
			return;
		}
		if (is.getTagCompound().hasKey(TIMER_RESET_NBT)) {
			is.setTagCompound(null);
		}
		if (is.getTagCompound() != null) {
			if (is.getTagCompound().hasKey(TIMER_PICKUP_NBT)) {
				is.getTagCompound().removeTag(TIMER_PICKUP_NBT);
			}
		}
	}

	public static enum MagnetFunctionMode {

			INACTIVE(I18n.translateToLocal("chatmessages.magnet_deactivated.desc")),
			ACTIVE_KEEP_IN_INVENTORY(I18n.translateToLocal("chatmessages.magnet_activated.desc") + " - " + I18n.translateToLocal("tooltip.magnet_active_1.desc")),
			ACTIVE_LEAVE_ON_GROUND(I18n.translateToLocal("chatmessages.magnet_activated.desc") + " - " + I18n.translateToLocal("tooltip.magnet_active_2.desc"));

		String message;

		public static MagnetFunctionMode[] VALUES = new MagnetFunctionMode[] {
				INACTIVE, ACTIVE_KEEP_IN_INVENTORY, ACTIVE_LEAVE_ON_GROUND
		};

		MagnetFunctionMode(final String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}

	/*
	 * nbt.setBoolean(INIT_NBT, mode);
			}
			else if (type == 1) {
				nbt.setBoolean(WHITELISTING_NBT, mode);
			}
			else if (type == 2) {
				nbt.setBoolean(IGNORE_NBT, mode);
			}
			else if (type == 3) {
				nbt.setBoolean(IGNORE_META_NBT, mode);
			}
			else if (type == 4) {
				nbt.setBoolean(USE_OREDICT_NBT, mode);
	 */
	public static enum MagnetItemMode {

			INIT(INIT_NBT), WHITELIST(WHITELISTING_NBT), IGNORENBT(IGNORE_NBT), IGNOREMETA(IGNORE_META_NBT),
			USEOREDICT(USE_OREDICT_NBT);

		String nbtKey;

		public static MagnetItemMode[] VALUES = new MagnetItemMode[] {
				INIT, WHITELIST, IGNORENBT, IGNOREMETA, USEOREDICT
		};

		MagnetItemMode(final String nbtKey) {
			this.nbtKey = nbtKey;
		}

		public String getNBTKey() {
			return nbtKey;
		}

	}

}
