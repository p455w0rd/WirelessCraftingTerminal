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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;

import com.google.common.base.Splitter;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import p455w0rd.wct.api.IWirelessCraftingTermHandler;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.networking.security.WCTPlayerSource;
import p455w0rd.wct.handlers.GuiHandler;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModKeybindings;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketMagnetFilter;
import p455w0rd.wct.sync.packets.PacketSetMagnet;
import p455w0rd.wct.util.WCTUtils;
import p455w0rdslib.util.EntityItemUtils;
import p455w0rdslib.util.ItemUtils;

/**
 * @author p455w0rd
 *
 */
public class ItemMagnet extends ItemBase {

	private int distanceFromPlayer;
	private WCTGuiObject obj;
	//private IPortableCell civ;
	private IEnergySource powerSrc;
	//private IMEMonitor<IAEItemStack> monitor;
	//private IMEInventoryHandler<IAEItemStack> cellInv;
	private BaseActionSource mySrc;
	private ItemStack thisItemStack;
	private int pickupTimer = 0;

	private static final String name = "magnet_card";

	public ItemMagnet() {
		super(name);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(ItemStack item) {
		return isActivated(item);
	}

	public void setItemStack(ItemStack is) {
		thisItemStack = is;
	}

	@Override
	public boolean isDamageable() {
		return false;
	}

	public ItemStack getItemStack() {
		if (thisItemStack != null && (thisItemStack.getItem() instanceof ItemMagnet)) {
			return thisItemStack;
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack is, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add(color("aqua") + "==============================");
		String shift = I18n.format("tooltip.press_shift.desc").replace("Shift", color("yellow") + color("bold") + color("italics") + "Shift" + color("gray"));
		if (isShiftKeyDown()) {
			ItemStack itemStack = getItemStack();

			String info = I18n.format("tooltip.magnet.desc");
			for (String line : Splitter.on("\n").split(WordUtils.wrap(info, 37, "\n", false))) {
				list.add(line.trim());
			}

			list.add("");
			list.add(color("italics") + "" + I18n.format("tooltip.magnet_set_filter.desc"));

			if (isActivated(itemStack)) {
				String boundKey = Keyboard.getKeyName(ModKeybindings.openMagnetFilter.getKeyCode());
				if (!boundKey.equals("NONE")) {
					list.add(color("italics") + I18n.format("tooltip.or_press.desc") + " " + color("yellow") + color("bold") + "[" + boundKey + "]");
				}
				String boundKey2 = Keyboard.getKeyName(ModKeybindings.changeMagnetMode.getKeyCode());
				if (!boundKey2.equals("NONE")) {
					list.add(color("italics") + I18n.format("tooltip.press.desc") + " " + color("yellow") + color("bold") + "[" + boundKey2 + "] " + color("gray") + color("italics") + I18n.format("tooltip.to_switch.desc"));
				}
			}

			list.add("");
			list.add(I18n.format("tooltip.status.desc") + ": " + (isActivated(is) ? color("green") + I18n.format("tooltip.active.desc") : color("red") + I18n.format("tooltip.active.desc")));
			if (getDamageUnsafe(is) == 1) {
				list.add(color("white") + "  " + I18n.format("tooltip.magnet_active_1.desc"));
			}
			else if (getDamageUnsafe(is) == 2) {
				list.add(color("white") + "  " + I18n.format("tooltip.magnet_active_2.desc"));
			}

			String white = I18n.format("tooltip.magnet_whitelisting.desc");
			String black = I18n.format("tooltip.magnet_blacklisting.desc");

			list.add(I18n.format("tooltip.filter_mode.desc") + ": " + color("white") + (getMode(is) ? white : black));
			String not = I18n.format("tooltip.not.desc");
			String ignoring = I18n.format("tooltip.ignoring.desc");
			String nbtData = I18n.format("tooltip.nbt.desc");
			String metaData = I18n.format("tooltip.meta.desc");
			String usingOreDict = I18n.format("tooltip.using.desc") + " " + I18n.format("tooltip.oredict.desc");

			list.add((!doesMagnetUseOreDict() ? " " + not : color("green")) + " " + usingOreDict);
			list.add((!doesMagnetIgnoreNBT() ? " " + not : color("green")) + " " + ignoring + " " + nbtData);
			list.add((!doesMagnetIgnoreMeta() ? " " + not : color("green")) + " " + ignoring + " " + metaData);

			List<ItemStack> filteredItems = getFilteredItems(itemStack);

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
		return WCTUtils.color(color);
	}

	@SideOnly(Side.CLIENT)
	public static boolean isShiftKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack item = player.getHeldItem(hand);
		if (!world.isRemote && hand == EnumHand.MAIN_HAND && item != null && !item.isEmpty()) {
			if (!player.isSneaking()) {
				if (!WCTUtils.isMagnetInitialized(item)) {
					item.getTagCompound().setBoolean("Initialized", true);
				}
				int x = (int) player.posX;
				int y = (int) player.posY;
				int z = (int) player.posZ;
				GuiHandler.open(GuiHandler.GUI_MAGNET, player, world, new BlockPos(x, y, z));
			}
			else {
				switchMagnetMode(item, player, false);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
		}
		else {
			if (!WCTUtils.isMagnetInitialized(item)) {
				NetworkHandler.instance().sendToServer(new PacketMagnetFilter(0, true, item));
			}
			if (player.isSneaking()) {
				switchMagnetMode(item, player, false);
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
			}
			else {
				int x = (int) player.posX;
				int y = (int) player.posY;
				int z = (int) player.posZ;
				GuiHandler.open(GuiHandler.GUI_MAGNET, player, world, new BlockPos(x, y, z));
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
			}
		}
		//return new ActionResult<ItemStack>(EnumActionResult.FAIL, item);
	}

	@SideOnly(Side.CLIENT)
	public static void displayMessage(int mode) {
		EntityPlayer player = WCTUtils.player();
		switch (mode) {
		case 1:
			WCTUtils.chatMessage(player, new TextComponentString(I18n.format("chatmessages.magnet_activated.desc") + " - " + I18n.format("tooltip.magnet_active_1.desc")));
			break;
		case 2:
			WCTUtils.chatMessage(player, new TextComponentString(I18n.format("chatmessages.magnet_activated.desc") + " - " + I18n.format("tooltip.magnet_active_2.desc")));
			break;
		case 0:
			WCTUtils.chatMessage(player, new TextComponentString(I18n.format("chatmessages.magnet_deactivated.desc")));
			break;
		}
	}

	public static void switchMagnetMode(ItemStack item, EntityPlayer player) {
		switchMagnetMode(item, player, true);
	}

	public static void switchMagnetMode(ItemStack item, EntityPlayer player, boolean sync) {
		if (player.getEntityWorld().isRemote) {
			int newMode = 0;
			if (getDamageUnsafe(item) == 0) {
				newMode = 1;
			}
			else if (getDamageUnsafe(item) == 1) {
				newMode = 2;
			}
			else {
				newMode = 0;
			}
			setDamageUnsafe(item, newMode);
			displayMessage(newMode);
			if (sync) {
				NetworkHandler.instance().sendToServer(new PacketSetMagnet(newMode));
			}
		}
		else {
			int newMode = 0;
			if (getDamageUnsafe(item) == 0) {
				newMode = 1;
			}
			else if (getDamageUnsafe(item) == 1) {
				newMode = 2;
			}
			else {
				newMode = 0;
			}
			setDamageUnsafe(item, newMode);
		}
	}

	private static int getDamageUnsafe(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			if (!stack.getTagCompound().hasKey("MagnetMode")) {
				stack.getTagCompound().setInteger("MagnetMode", 0);
			}
		}
		return stack.getTagCompound().getInteger("MagnetMode");
	}

	private static void setDamageUnsafe(ItemStack stack, int damage) {
		stack.getTagCompound().setInteger("MagnetMode", damage);
	}

	public void doMagnet(ItemStack item, World world, EntityPlayer player, ItemStack wirelessTerm) {
		if (world.isRemote) {
			return;
		}
		if (getItemStack() == null) {
			return;
		}
		if (!isActivated(item)) {
			return;
		}
		if (player == null) {
			return;
		}
		if (player.isSneaking()) {
			return;
		}
		distanceFromPlayer = 6;
		List<ItemStack> filteredList = getFilteredItems(getItemStack());
		// items
		Iterator<?> iterator = getEntitiesInRange(EntityItem.class, world, (int) player.posX, (int) player.posY, (int) player.posZ, distanceFromPlayer).iterator();
		while (iterator.hasNext()) {

			EntityItem itemToGet = (EntityItem) iterator.next();
			if (itemToGet == null) {
				return;
			}
			if (EntityItemUtils.getThrowerName(itemToGet) != null && EntityItemUtils.getThrowerName(itemToGet).equals(player.getName()) && !EntityItemUtils.canPickup(itemToGet)) {
				continue;
			}

			ItemStack itemStackToGet = itemToGet.getItem();
			if (itemStackToGet == null) {
				return;
			}
			int stackSize = itemStackToGet.getCount();

			if (obj == null) {
				obj = getGuiObject(wirelessTerm, player, world, (int) player.posX, (int) player.posY, (int) player.posZ);
				powerSrc = obj;
				mySrc = new WCTPlayerSource(player, obj);
			}

			boolean ignoreRange = (isBoosterInstalled(wirelessTerm) && ModConfig.WCT_BOOSTER_ENABLED);
			boolean hasAxxess = hasNetworkAccess(SecurityPermissions.INJECT, true, player, wirelessTerm);
			if ((ignoreRange && hasAxxess) || (obj.rangeCheck() && hasAxxess)) {
				IAEItemStack ais = AEApi.instance().storage().createItemStack(itemStackToGet);
				ais.setStackSize(stackSize);
				if (!itemToGet.isDead) {

					// whitelist
					if (getMode(getItemStack())) {
						if (isItemFiltered(itemStackToGet, filteredList) && filteredList != null && filteredList.size() > 0) {
							if (doInject(ais, stackSize, player, itemToGet, itemStackToGet, world)) {
								itemToGet.setDead();
							}
							continue;
						}
						else if (getDamageUnsafe(item) == 1) {
							doInventoryPickup(itemToGet, player, itemStackToGet, world, stackSize);
						}
					}
					// blacklist
					else {
						if (!isItemFiltered(itemStackToGet, filteredList) || filteredList == null || filteredList.size() <= 0) {
							if (doInject(ais, stackSize, player, itemToGet, itemStackToGet, world)) {
								itemToGet.setDead();
							}
							continue;
						}
						else if (getDamageUnsafe(item) == 1) {
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
			//player.onItemPickup(xpToGet, 1);
			ItemStack itemstack = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, player);
			if (itemstack != null && itemstack.isItemDamaged()) {
				int i = Math.min(xpToDurability(xpAmount), itemstack.getItemDamage());
				xpAmount -= durabilityToXp(i);
				itemstack.setItemDamage(itemstack.getItemDamage() - i);
			}
			if (xpAmount > 0) {
				player.addExperience(xpAmount);
			}
		}
	}

	private int durabilityToXp(int durability) {
		return durability / 2;
	}

	private int xpToDurability(int xp) {
		return xp * 2;
	}

	private void doInventoryPickup(EntityItem itemToGet, EntityPlayer player, ItemStack itemStackToGet, World world, int stackSize) {
		if (itemToGet.getDistanceToEntity(player) <= distanceFromPlayer) {
			if (player.inventory.addItemStackToInventory(itemStackToGet)) {
				player.onItemPickup(itemToGet, stackSize);
				world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((WCTUtils.world(player).rand.nextFloat() - WCTUtils.world(player).rand.nextFloat()) * 0.7F + 2F));
			}
		}
	}

	private boolean doesMagnetIgnoreNBT() {
		return ItemUtils.readBoolean(getItemStack(), "IgnoreNBT");
	}

	private boolean doesMagnetIgnoreMeta() {
		return ItemUtils.readBoolean(getItemStack(), "IgnoreMeta");
	}

	private boolean doesMagnetUseOreDict() {
		return ItemUtils.readBoolean(getItemStack(), "UseOreDict");
	}

	private boolean areOresEqual(ItemStack is1, ItemStack is2) {
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

	private boolean isItemFiltered(ItemStack is, List<ItemStack> itemList) {
		if (is != null && itemList != null) {
			for (int i = 0; i < itemList.size(); i++) {
				ItemStack thisStack = itemList.get(i);
				//use oredict
				if (doesMagnetUseOreDict()) {
					if (areOresEqual(is, thisStack)) {
						return true;
					}
				}
				//ignore meta & nbt
				if (doesMagnetIgnoreMeta() && doesMagnetIgnoreNBT()) {
					if (is.getItem().equals(thisStack.getItem())) {
						return true;
					}
				}
				//ignore meta only
				else if (doesMagnetIgnoreMeta() && !doesMagnetIgnoreNBT()) {
					if (ItemStack.areItemStackTagsEqual(is, thisStack) && is.getItem() == thisStack.getItem()) {
						return true;
					}
				}
				//ignore nbt only
				else if (!doesMagnetIgnoreMeta() && doesMagnetIgnoreNBT() && is.getItem() == thisStack.getItem()) {
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

	private boolean isMetaEqual(ItemStack is1, ItemStack is2) {
		return is1.getItemDamage() == is2.getItemDamage();
	}

	private boolean hasNetworkAccess(final SecurityPermissions perm, final boolean requirePower, EntityPlayer player, ItemStack wirelessTerm) {
		if (player.capabilities.isCreativeMode) {
			return true;
		}
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

	private List<ItemStack> getFilteredItems(ItemStack magnetItem) {
		if (magnetItem == null) {
			return null;
		}
		if (magnetItem.getItem() instanceof ItemMagnet) {
			if (magnetItem.hasTagCompound()) {
				NBTTagCompound nbtTC = magnetItem.getTagCompound();
				if (!nbtTC.hasKey("MagnetFilter")) {
					return null;
				}
				NBTTagList tagList = nbtTC.getTagList("MagnetFilter", 10);
				if (tagList.tagCount() > 0 && tagList != null) {
					List<ItemStack> itemList = new ArrayList<ItemStack>();
					for (int i = 0; i < tagList.tagCount(); i++) {
						itemList.add(new ItemStack(tagList.getCompoundTagAt(i)));
					}
					return itemList;
				}
			}
		}
		return null;
	}

	private boolean doInject(IAEItemStack ais, int stackSize, EntityPlayer player, EntityItem itemToGet, ItemStack itemStackToGet, World world) {
		ais = Api.INSTANCE.storage().poweredInsert(powerSrc, obj.getItemInventory(), ais, mySrc);
		if (ais != null && WCTUtils.getMagnet(player.inventory) != null && WCTUtils.getMagnet(player.inventory).getItemDamage() != 2) {
			player.onItemPickup(itemToGet, stackSize);
			player.inventory.addItemStackToInventory(itemStackToGet);
			world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((WCTUtils.world(player).rand.nextFloat() - WCTUtils.world(player).rand.nextFloat()) * 0.7F + 2F));
		}
		return ais == null;
	}

	private boolean isBoosterInstalled(ItemStack wirelessTerm) {
		if (wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem) {
			if (wirelessTerm.hasTagCompound()) {
				NBTTagList boosterNBTList = wirelessTerm.getTagCompound().getTagList("BoosterSlot", 10);
				if (boosterNBTList != null) {
					NBTTagCompound boosterTagCompound = boosterNBTList.getCompoundTagAt(0);
					if (boosterTagCompound != null) {
						ItemStack boosterCard = new ItemStack(boosterTagCompound);
						if (boosterCard != null) {
							return (boosterCard.getItem() instanceof ItemInfinityBooster);
						}
					}
				}
			}
		}
		return false;
	}

	// true=whitelist (default:whitelist)
	private boolean getMode(ItemStack magnetItem) {
		if (magnetItem.getItem() instanceof ItemMagnet) {
			if (magnetItem.hasTagCompound()) {
				NBTTagCompound nbtTC = magnetItem.getTagCompound();
				if (nbtTC.hasKey("Whitelisting")) {
					return nbtTC.getBoolean("Whitelisting");
				}
			}
		}
		return true;
	}

	private WCTGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (it != null) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WCTGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	public static List<?> getEntitiesInRange(Class<? extends Entity> entityType, World world, int x, int y, int z, int distance) {
		return world.getEntitiesWithinAABB(entityType, new AxisAlignedBB(x - distance, y - distance, z - distance, x + distance, y + distance, z + distance));
	}

	public ItemStack getStack() {
		return this.getStack(1);
	}

	public ItemStack getStack(final int size) {
		return new ItemStack(this, size);
	}

	protected boolean isActivated(ItemStack item) {
		if (item == null) {
			return false;
		}
		return getDamageUnsafe(item) != 0;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initModel() {
		for (int i = 0; i < 3; i++) {
			ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(getRegistryName(), "inventory"));
		}
	}

	public static enum MagnetMode {
			INACTIVE, ACTIVE_KEEP, ACTIVE_LEAVE
	}

}
