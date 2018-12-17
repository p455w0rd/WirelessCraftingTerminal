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
import net.minecraft.client.resources.I18n;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import p455w0rd.ae2wtlib.api.ICustomWirelessTermHandler;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.api.networking.security.WTPlayerSource;
import p455w0rd.ae2wtlib.helpers.WTGuiObject;
import p455w0rd.ae2wtlib.init.*;
import p455w0rd.ae2wtlib.integration.Baubles;
import p455w0rd.ae2wtlib.items.ItemBase;
import p455w0rd.ae2wtlib.sync.packets.PacketSyncInfinityEnergyInv;
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
	public boolean hasEffect(@Nonnull ItemStack item) {
		return isActivated(item);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
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
	public void addInformation(@Nonnull ItemStack is, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add(color("aqua") + "==============================");
		String shift = I18n.format("tooltip.press_shift.desc").replace("Shift", color("yellow") + color("bold") + color("italics") + "Shift" + color("gray"));
		if (isShiftKeyDown()) {

			String info = I18n.format("tooltip.magnet.desc");
			for (String line : Splitter.on("\n").split(WordUtils.wrap(info, 37, "\n", false))) {
				list.add(line.trim());
			}

			list.add("");
			list.add(color("italics") + "" + I18n.format("tooltip.magnet_set_filter.desc"));
			/*
			if (isActivated(itemStack)) {
				String boundKey = ModKeybindings.openMagnetFilter.getDisplayName();
				if (!boundKey.equals("NONE")) {
					list.add(color("italics") + I18n.format("tooltip.or_press.desc") + " " + color("yellow") + color("bold") + "[" + boundKey + "]");
				}
				String boundKey2 = ModKeybindings.changeMagnetMode.getDisplayName();
				if (!boundKey2.equals("NONE")) {
					list.add(color("italics") + I18n.format("tooltip.press.desc") + " " + color("yellow") + color("bold") + "[" + boundKey2 + "] " + color("gray") + color("italics") + I18n.format("tooltip.to_switch.desc"));
				}
			}
			*/
			list.add("");
			String not = I18n.format("tooltip.not.desc");
			list.add(I18n.format("tooltip.status.desc") + ": " + (isActivated(is) ? color("green") + I18n.format("tooltip.active.desc") : color("red") + not + " " + I18n.format("tooltip.active.desc")));
			MagnetFunctionMode mode = getMagnetFunctionMode(is);
			if (mode != MagnetFunctionMode.INACTIVE) {
				list.add(color("white") + "  " + mode.getMessage().replace("-", "\n  -"));
			}

			String white = I18n.format("tooltip.magnet_whitelisting.desc");
			String black = I18n.format("tooltip.magnet_blacklisting.desc");

			list.add(I18n.format("tooltip.filter_mode.desc") + ": " + color("white") + (getListMode(is) ? white : black));

			String ignoring = I18n.format("tooltip.ignoring.desc");
			String nbtData = I18n.format("tooltip.nbt.desc");
			String metaData = I18n.format("tooltip.meta.desc");
			String usingOreDict = I18n.format("tooltip.using.desc") + " " + I18n.format("tooltip.oredict.desc");

			list.add((!doesMagnetUseOreDict(is) ? " " + not : color("green")) + " " + usingOreDict);
			list.add((!doesMagnetIgnoreNBT(is) ? " " + not : color("green")) + " " + ignoring + " " + nbtData);
			list.add((!doesMagnetIgnoreMeta(is) ? " " + not : color("green")) + " " + ignoring + " " + metaData);

			List<ItemStack> filteredItems = getFilteredItems(is);

			if (filteredItems != null) {
				list.add("");
				list.add(color("gray") + I18n.format("tooltip.filtered_items.desc") + ":");
				for (int i = 0; i < filteredItems.size(); i++) {
					list.add("  " + filteredItems.get(i).getDisplayName());
				}
			}

			list.add("");
			String onlyWorks = I18n.format("tooltip.only_works.desc");
			for (String line : Splitter.on("\n").split(WordUtils.wrap(onlyWorks, 27, "\n", false))) {
				list.add(color("white") + color("bold") + color("italics") + line.trim());
			}

		}
		else {
			list.add(shift);
		}
	}

	@SideOnly(Side.CLIENT)
	private String color(String color) {
		return WTApi.instance().color(color);
	}

	@SideOnly(Side.CLIENT)
	public static boolean isShiftKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack item = player.getHeldItem(hand);
		if (world.isRemote && hand == EnumHand.MAIN_HAND && !item.isEmpty()) {
			if (!isMagnetInitialized(item)) {
				ModNetworking.instance().sendToServer(new PacketMagnetFilterHeld(0, true));
			}
			if (player.isSneaking()) {
				cycleMagnetFunctionModeHeld(player);
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
			}
			else {
				WCTApi.instance().openMagnetGui(player, false, -1);
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
			}
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, item);
	}

	public void doMagnet(EntityPlayer player, @Nonnull ItemStack wirelessTerminal, boolean isWCTBauble, int wctSlot) {
		World world = player.getEntityWorld();
		ItemStack magnet = getMagnetFromWCT(wirelessTerminal);
		if (world.isRemote || magnet.isEmpty() || !isActivated(magnet) || player == null || player.isSneaking()) {
			return;
		}
		distanceFromPlayer = 6;
		NonNullList<ItemStack> filteredList = getFilteredItems(magnet);
		// items
		Iterator<Entity> iterator = getEntitiesInRange(EntityItem.class, world, (int) player.posX, (int) player.posY, (int) player.posZ, distanceFromPlayer).iterator();
		while (iterator.hasNext()) {
			EntityItem itemToGet = (EntityItem) iterator.next();
			if (itemToGet == null) {
				return;
			}
			if (EntityItemUtils.getThrowerName(itemToGet) != null && EntityItemUtils.getThrowerName(itemToGet).equals(player.getName()) && !EntityItemUtils.canPickup(itemToGet)) {
				continue;
			}
			// Demagnetize integration
			if (itemToGet.getEntityData() != null && itemToGet.getEntityData().hasKey("PreventRemoteMovement")) {
				return;
			}
			ItemStack itemStackToGet = itemToGet.getItem();
			if (itemStackToGet.isEmpty()) {
				return;
			}
			int stackSize = itemStackToGet.getCount();
			WTGuiObject<IAEItemStack, IItemStorageChannel> obj = getGuiObject(wirelessTerminal, player, world, (int) player.posX, (int) player.posY, (int) player.posZ);
			boolean ignoreRange = WTApi.instance().hasInfiniteRange(wirelessTerminal);
			boolean hasAxxess = hasNetworkAccess(SecurityPermissions.INJECT, true, player, wirelessTerminal);
			if ((ignoreRange && hasAxxess) || (obj.rangeCheck() && hasAxxess)) {
				IAEItemStack ais = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(itemStackToGet);
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
		iterator = getEntitiesInRange(EntityXPOrb.class, world, (int) player.posX, (int) player.posY, (int) player.posZ, distanceFromPlayer).iterator();
		while (iterator.hasNext()) {
			EntityXPOrb xpToGet = (EntityXPOrb) iterator.next();
			if (xpToGet.isDead || xpToGet.isInvisible()) {
				continue;
			}
			//if (MinecraftForge.EVENT_BUS.post(new PlayerPickupXpEvent(player, xpToGet))) {
			int xpAmount = xpToGet.xpValue;
			xpToGet.setDead();
			world.playSound((EntityPlayer) null, xpToGet.posX, xpToGet.posY, xpToGet.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
			ItemStack itemstack = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, player);
			if (!itemstack.isEmpty() && itemstack.isItemDamaged()) {
				int i = Math.min(xpToDurability(xpAmount), itemstack.getItemDamage());
				xpAmount -= durabilityToXp(i);
				itemstack.setItemDamage(itemstack.getItemDamage() - i);
			}
			if (xpAmount > 0) {
				player.addExperience(xpAmount);
			}
		}
	}

	private boolean doInject(IAEItemStack ais, int stackSize, EntityPlayer player, ItemStack wirelessTerminal, EntityItem itemToGet, boolean isWCTBauble, int wctSlot) {
		World world = player.getEntityWorld();
		InventoryPlayer playerInv = player.inventory;
		int invSize = playerInv.getSizeInventory();
		if (invSize >= 0 && !LibConfig.USE_OLD_INFINTY_MECHANIC && !wirelessTerminal.isEmpty() && WTApi.instance().shouldConsumeBoosters(wirelessTerminal)) {
			ItemStack pickupStack = itemToGet.getItem();
			if (!pickupStack.isEmpty() && pickupStack.getItem() == LibItems.BOOSTER_CARD) {
				WTApi.instance().addInfinityBoosters(wirelessTerminal, pickupStack);
				LibNetworking.instance().sendToDimension(new PacketSyncInfinityEnergyInv(WTApi.instance().getInfinityEnergy(wirelessTerminal), player.getUniqueID(), isWCTBauble, wctSlot), player.getEntityWorld().provider.getDimension());
				return true;
			}
		}
		WTGuiObject<IAEItemStack, IItemStorageChannel> obj = getGuiObject(wirelessTerminal, player, world, (int) player.posX, (int) player.posY, (int) player.posZ);
		IEnergySource powerSrc = obj;
		IActionSource mySrc = new WTPlayerSource(player, obj);
		ais = Platform.poweredInsert(powerSrc, obj, ais, mySrc);
		ItemStack magnet = getMagnetFromWCT(wirelessTerminal);
		if (ais != null && !magnet.isEmpty() && getMagnetFunctionMode(magnet) == MagnetFunctionMode.ACTIVE_KEEP_IN_INVENTORY) {
			player.onItemPickup(itemToGet, stackSize);
			player.inventory.addItemStackToInventory(itemToGet.getItem());
			world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.getEntityWorld().rand.nextFloat() - player.getEntityWorld().rand.nextFloat()) * 0.7F + 2F));
		}
		return ais == null;
	}

	private int durabilityToXp(int durability) {
		return durability / 2;
	}

	private int xpToDurability(int xp) {
		return xp * 2;
	}

	private void doInventoryPickup(EntityItem itemToGet, EntityPlayer player, ItemStack itemStackToGet, World world, int stackSize) {
		if (itemToGet.getDistance(player) <= distanceFromPlayer) {
			if (player.inventory.addItemStackToInventory(itemStackToGet)) {
				player.onItemPickup(itemToGet, stackSize);
				world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.getEntityWorld().rand.nextFloat() - player.getEntityWorld().rand.nextFloat()) * 0.7F + 2F));
			}
		}
	}

	private static boolean doesMagnetIgnoreNBT(ItemStack magnet) {
		return ItemUtils.readBoolean(magnet, IGNORE_NBT);
	}

	private static boolean doesMagnetIgnoreMeta(ItemStack magnet) {
		return ItemUtils.readBoolean(magnet, IGNORE_META_NBT);
	}

	private static boolean doesMagnetUseOreDict(ItemStack magnet) {
		return ItemUtils.readBoolean(magnet, USE_OREDICT_NBT);
	}

	private static boolean areOresEqual(ItemStack is1, ItemStack is2) {
		int[] list1 = OreDictionary.getOreIDs(is1);
		int[] list2 = OreDictionary.getOreIDs(is2);
		for (int element : list1) {
			for (int element2 : list2) {
				if (element == element2) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isItemFiltered(@Nonnull ItemStack magnet, @Nonnull ItemStack is, NonNullList<ItemStack> itemList) {
		if (!is.isEmpty() && !itemList.isEmpty()) {
			for (int i = 0; i < itemList.size(); i++) {
				ItemStack thisStack = itemList.get(i);
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

	private static boolean isMetaEqual(@Nonnull ItemStack is1, @Nonnull ItemStack is2) {
		return is1.getItemDamage() == is2.getItemDamage();
	}

	private boolean hasNetworkAccess(final SecurityPermissions perm, final boolean requirePower, EntityPlayer player, @Nonnull ItemStack wirelessTerm) {
		if (player.capabilities.isCreativeMode) {
			return true;
		}
		WTGuiObject<IAEItemStack, IItemStorageChannel> obj = getGuiObject(wirelessTerm, player, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
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

	public static NonNullList<ItemStack> getFilteredItems(@Nonnull ItemStack magnet) {
		if (magnet.isEmpty()) {
			return NonNullList.create();
		}
		if (magnet.getItem() == ModItems.MAGNET_CARD) {
			if (magnet.hasTagCompound()) {
				NBTTagCompound nbtTC = magnet.getTagCompound();
				if (!nbtTC.hasKey("MagnetFilter")) {
					return NonNullList.create();
				}
				NBTTagList tagList = nbtTC.getTagList(MAGNET_FILTER_NBT, 10);
				if (tagList.tagCount() > 0 && tagList != null) {
					NonNullList<ItemStack> itemList = NonNullList.create();
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
	public static ItemStack getMagnetFromWCT(@Nonnull ItemStack wirelessTerm) {
		if (!wirelessTerm.isEmpty() && wirelessTerm.hasTagCompound() && wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem) {
			NBTTagCompound magnetNBT = wirelessTerm.getSubCompound(MAGNET_SLOT_NBT);
			if (magnetNBT != null) {
				NBTTagList magnetSlot = magnetNBT.getTagList(ITEMS_NBT, 10);
				ItemStack magnetItem = new ItemStack(magnetSlot.getCompoundTagAt(0));
				if (magnetItem != null && !magnetItem.isEmpty() && magnetItem.getItem() == ModItems.MAGNET_CARD) {
					return magnetItem;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Nonnull
	public static ItemStack getMagnetFromInv(@Nonnull InventoryPlayer inv, int slot) {
		ItemStack magnetItem = inv.getStackInSlot(slot);
		if (magnetItem != null && !magnetItem.isEmpty() && magnetItem.getItem() == ModItems.MAGNET_CARD) {
			return magnetItem;
		}
		return ItemStack.EMPTY;
	}

	// true=whitelist (default:whitelist)
	private boolean getListMode(@Nonnull ItemStack magnet) {
		if (magnet.getItem() == ModItems.MAGNET_CARD) {
			return getItemMode(magnet, 1);
		}
		return true;
	}

	public static boolean getItemMode(@Nonnull ItemStack magnet, int type) {
		if (!magnet.hasTagCompound()) {
			magnet.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbtTC = magnet.getTagCompound();
		if (type == 1) {
			if (nbtTC.hasKey(WHITELISTING_NBT)) {
				return nbtTC.getBoolean(WHITELISTING_NBT);
			}
			else {
				return true;
			}
		}
		else if (type == 2) {
			if (nbtTC.hasKey(IGNORE_NBT)) {
				return nbtTC.getBoolean(IGNORE_NBT);
			}
			else {
				return false;
			}
		}
		else if (type == 3) {
			if (nbtTC.hasKey(IGNORE_META_NBT)) {
				return nbtTC.getBoolean(IGNORE_META_NBT);
			}
			else {
				return false;
			}
		}
		else if (type == 4) {
			if (nbtTC.hasKey(USE_OREDICT_NBT)) {
				return nbtTC.getBoolean(USE_OREDICT_NBT);
			}
			else {
				return false;
			}
		}
		else {
			return true;
		}
	}

	public static void setItemMode(@Nonnull ItemStack magnet, int type, boolean mode) {
		if (!magnet.isEmpty()) {
			if (!magnet.hasTagCompound()) {
				magnet.setTagCompound(new NBTTagCompound());
			}
			NBTTagCompound nbt = magnet.getTagCompound();

			if (type == 0) {
				nbt.setBoolean(INIT_NBT, mode);
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
			}
			else {
				return;
			}
		}
	}

	private WTGuiObject<IAEItemStack, IItemStorageChannel> getGuiObject(@Nonnull final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (!it.isEmpty()) {
			final ICustomWirelessTermHandler wh = (ICustomWirelessTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WTGuiObject<IAEItemStack, IItemStorageChannel>(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	public static List<Entity> getEntitiesInRange(Class<? extends Entity> entityType, World world, int x, int y, int z, int distance) {
		return world.getEntitiesWithinAABB(entityType, new AxisAlignedBB(x - distance, y - distance, z - distance, x + distance, y + distance, z + distance));
	}

	@Nonnull
	public ItemStack getStack() {
		return this.getStack(1);
	}

	@Nonnull
	public ItemStack getStack(final int size) {
		return new ItemStack(this, size);
	}

	public static boolean isActivated(@Nonnull ItemStack magnet) {
		return isActivated(magnet, false);
	}

	public static boolean isActivated(@Nonnull ItemStack stack, boolean isWCT) {
		ItemStack magnet = isWCT ? getMagnetFromWCT(stack) : stack;
		if (magnet.isEmpty()) {
			return false;
		}
		return getMagnetFunctionMode(magnet) != MagnetFunctionMode.INACTIVE;
	}

	@Override
	public int getMaxItemUseDuration(@Nonnull ItemStack stack) {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initModel() {
		for (int i = 0; i < 3; i++) {
			ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(getRegistryName(), "inventory"));
		}
	}

	public static MagnetFunctionMode getMagnetFunctionMode(@Nonnull ItemStack magnet) {
		if (!magnet.isEmpty()) {
			if (!magnet.hasTagCompound()) {
				magnet.setTagCompound(new NBTTagCompound());
			}
			NBTTagCompound nbt = magnet.getTagCompound();
			if (!nbt.hasKey(MAGNET_MODE_NBT)) {
				nbt.setInteger(MAGNET_MODE_NBT, 0);
			}
			int val = nbt.getInteger(MAGNET_MODE_NBT);
			return MagnetFunctionMode.VALUES[val];
		}
		return MagnetFunctionMode.INACTIVE;
	}

	@SideOnly(Side.CLIENT)
	public static void displayMessage(MagnetFunctionMode mode) {
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString(mode.getMessage()));
	}

	public static void setMagnetFunctionMode(@Nonnull ItemStack stack, MagnetFunctionMode mode) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger(MAGNET_MODE_NBT, mode.ordinal());
	}

	public static MagnetFunctionMode cycleMagnetFunctionMode(@Nonnull ItemStack magnet) {
		if (!magnet.isEmpty()) {
			int newMode = getMagnetFunctionMode(magnet).ordinal() + 1;
			int max = MagnetFunctionMode.VALUES.length - 1;
			if (newMode > max) {
				newMode = 0;
			}
			setMagnetFunctionMode(magnet, MagnetFunctionMode.VALUES[newMode]);
			return MagnetFunctionMode.VALUES[newMode];
		}
		return MagnetFunctionMode.INACTIVE;
	}

	public static MagnetFunctionMode cycleMagnetFunctionModeWCT(EntityPlayer player, int wctSlot, boolean isBauble) {
		ItemStack magnet = getMagnetFromWCT(isBauble ? Baubles.getWTBySlot(player, wctSlot, IWirelessCraftingTerminalItem.class) : WCTUtils.getWCTBySlot(player, wctSlot));
		if (player == null || magnet.isEmpty() || wctSlot < 0) {
			return MagnetFunctionMode.INACTIVE;
		}
		if (!magnet.hasTagCompound()) {
			setItemMode(magnet, 0, true);
		}
		MagnetFunctionMode mode = cycleMagnetFunctionMode(magnet);
		if (!(player instanceof EntityPlayerMP)) {
			//displayMessage(mode);
		}
		return mode;
	}

	public static void cycleMagnetFunctionModeHeld(@Nonnull EntityPlayer player) {
		if (player == null || player.getHeldItemMainhand().isEmpty() || player.getHeldItemMainhand().getItem() != ModItems.MAGNET_CARD) {
			return;
		}
		ItemStack magnet = getHeldMagnet(player);
		if (magnet.isEmpty()) {
			return;
		}
		if (!magnet.hasTagCompound()) {
			ItemMagnet.setItemMode(magnet, 0, true);
		}
		MagnetFunctionMode mode = cycleMagnetFunctionMode(magnet);
		if (!(player instanceof EntityPlayerMP)) {
			displayMessage(mode);
			ModNetworking.instance().sendToServer(new PacketSetMagnetHeld(mode));
		}
	}

	@Nonnull
	public static ItemStack getHeldMagnet(EntityPlayer player) {
		if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == ModItems.MAGNET_CARD) {
			return player.getHeldItemMainhand();
		}
		return ItemStack.EMPTY;
	}

	public static boolean isMagnetInitialized(@Nonnull ItemStack magnetItem) {
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

	public static boolean isMagnetInstalled(EntityPlayer player, boolean isBauble, int slot) {
		return isMagnetInstalled(isBauble ? Baubles.getWTBySlot(player, slot, IWirelessCraftingTerminalItem.class) : WCTUtils.getWCTBySlot(player, slot));
	}

	public static boolean isMagnetInstalled(ItemStack wirelessTerm) {
		if (!wirelessTerm.isEmpty() && WCTUtils.isAnyWCT(wirelessTerm) && wirelessTerm.hasTagCompound() && wirelessTerm.getTagCompound().hasKey(MAGNET_SLOT_NBT)) {
			NBTTagCompound magnetNBT = wirelessTerm.getSubCompound(MAGNET_SLOT_NBT);
			if (magnetNBT != null) {
				NBTTagList magnetList = magnetNBT.getTagList(ITEMS_NBT, 10);
				if (magnetList != null && !magnetList.hasNoTags()) {
					NBTTagCompound magnetNBTForm = magnetList.getCompoundTagAt(0);
					if (magnetNBTForm != null) {
						ItemStack magnetItem = new ItemStack(magnetNBTForm);
						if (!magnetItem.isEmpty() && magnetItem.getItem() instanceof ItemMagnet) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static void removeTimerTags(ItemStack is) {
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

			INACTIVE(I18n.format("chatmessages.magnet_deactivated.desc")),
			ACTIVE_KEEP_IN_INVENTORY(I18n.format("chatmessages.magnet_activated.desc") + " - " + I18n.format("tooltip.magnet_active_1.desc")),
			ACTIVE_LEAVE_ON_GROUND(I18n.format("chatmessages.magnet_activated.desc") + " - " + I18n.format("tooltip.magnet_active_2.desc"));

		String message;
		public static MagnetFunctionMode[] VALUES = new MagnetFunctionMode[] {
				INACTIVE, ACTIVE_KEEP_IN_INVENTORY, ACTIVE_LEAVE_ON_GROUND
		};

		MagnetFunctionMode(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}

}
