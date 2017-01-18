package net.p455w0rd.wirelesscraftingterminal.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;

import com.google.common.base.Splitter;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketMagnetFilter;
import net.p455w0rd.wirelesscraftingterminal.handlers.KeybindHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

/**
 * Jotato's amazing magnet item from QuantumFlux adapted for use in the Wireless
 * Crafting Terminal
 *
 * @author p455w0rd, Jotato
 *
 */
public class ItemMagnet extends Item {

	private int distanceFromPlayer;
	public WirelessTerminalGuiObject obj;
	public IPortableCell civ;
	public IEnergySource powerSrc;
	public IMEMonitor<IAEItemStack> monitor;
	public IMEInventoryHandler<IAEItemStack> cellInv;
	public BaseActionSource mySrc;
	private ItemStack thisItemStack;
	private int pickupTimer = 0;

	public ItemMagnet() {
		super();
		setMaxStackSize(1);
		distanceFromPlayer = 16;
		canRepair = false;
		setMaxDamage(0);
		setTextureName(Reference.MODID + ":magnetCard");
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
	public void addInformation(ItemStack is, EntityPlayer player, List list, boolean par4) {
		list.add(color("aqua") + "==============================");
		String shift = LocaleHandler.PressShift.getLocal().replace("Shift", color("yellow") + color("bold") + color("italics") + "Shift" + color("gray"));
		if (isShiftKeyDown()) {
			ItemStack itemStack = getItemStack();

			String info = LocaleHandler.MagnetDesc.getLocal();
			for (String line : Splitter.on("\n").split(WordUtils.wrap(info, 37, "\n", false))) {
				list.add(line.trim());
			}

			list.add("");
			list.add(color("italics") + "" + LocaleHandler.MagnetDesc2.getLocal());

			if (isActivated(itemStack)) {
				String boundKey = Keyboard.getKeyName(KeybindHandler.openMagnetFilter.getKeyCode());
				if (!boundKey.equals("NONE")) {
					list.add(color("italics") + LocaleHandler.OrPress.getLocal() + " " + color("yellow") + color("bold") + "[" + boundKey + "]");
				}
				String boundKey2 = Keyboard.getKeyName(KeybindHandler.changeMagnetMode.getKeyCode());
				if (!boundKey2.equals("NONE")) {
					list.add(color("italics") + LocaleHandler.Press.getLocal() + " " + color("yellow") + color("bold") + "[" + boundKey2 + "] " + color("gray") + color("italics") + LocaleHandler.ToSwitchMode.getLocal());
				}
			}

			list.add("");
			list.add(LocaleHandler.Status.getLocal() + ": " + (isActivated(is) ? color("green") + LocaleHandler.Active.getLocal() : color("red") + LocaleHandler.Inactive.getLocal()));
			if (is.getItemDamage() == 1) {
				list.add(color("white") + "  " + LocaleHandler.MagnetActiveDesc1.getLocal());
			}
			else if (is.getItemDamage() == 2) {
				list.add(color("white") + "  " + LocaleHandler.MagnetActiveDesc2.getLocal());
			}

			String white = LocaleHandler.Whitelisting.getLocal();
			String black = LocaleHandler.Blacklisting.getLocal();

			list.add(LocaleHandler.FilterMode.getLocal() + ": " + color("white") + (getMode(is) ? white : black));
			String not = LocaleHandler.Not.getLocal();
			String ignoring = LocaleHandler.Ignoring.getLocal();
			String nbtData = LocaleHandler.NBTData.getLocal();
			String metaData = LocaleHandler.MetaData.getLocal();
			String usingOreDict = LocaleHandler.Using.getLocal() + " " + LocaleHandler.OreDict.getLocal();

			list.add((!doesMagnetUseOreDict() ? " " + not : color("green")) + " " + usingOreDict);
			list.add((!doesMagnetIgnoreNBT() ? " " + not : color("green")) + " " + ignoring + " " + nbtData);
			list.add((!doesMagnetIgnoreMeta() ? " " + not : color("green")) + " " + ignoring + " " + metaData);

			List<ItemStack> filteredItems = getFilteredItems(itemStack);

			if (filteredItems != null) {
				list.add("");
				list.add(color("gray") + LocaleHandler.FilteredItems.getLocal() + ":");
				for (int i = 0; i < filteredItems.size(); i++) {
					list.add("  " + filteredItems.get(i).getDisplayName());
				}
			}

			list.add("");
			String onlyWorks = LocaleHandler.OnlyWorks.getLocal();
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
		return RandomUtils.color(color);
	}

	@SideOnly(Side.CLIENT)
	public static boolean isShiftKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
		if (!world.isRemote) {
			if (player.isSneaking()) {
				switchMagnetMode(item, player);
			}
			else {
				if (!RandomUtils.isMagnetInitialized(item)) {
					NetworkHandler.instance.sendToServer(new PacketMagnetFilter(0, true));
				}
				int x = (int) player.posX;
				int y = (int) player.posY;
				int z = (int) player.posZ;
				WCTGuiHandler.launchGui(Reference.GUI_MAGNET, player, world, x, y, z);
			}
		}
		return item;
	}

	public void switchMagnetMode(ItemStack item, EntityPlayer player) {
		if (item.getItemDamage() == 0) {
			item.setItemDamage(1);
			player.addChatMessage(new ChatComponentText(LocaleHandler.MagnetMode2.getLocal()));
		}
		else if (item.getItemDamage() == 1) {
			item.setItemDamage(2);
			player.addChatMessage(new ChatComponentText(LocaleHandler.MagnetMode3.getLocal()));
		}
		else {
			item.setItemDamage(0);
			player.addChatMessage(new ChatComponentText(LocaleHandler.MagnetMode1.getLocal()));
		}
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

		//List<ItemStack> filteredList = getFilteredItems(getItemStack());
		// items
		Iterator<? extends Entity> iterator = getEntitiesInRange(EntityItem.class, world, (int) player.posX, (int) player.posY, (int) player.posZ, distanceFromPlayer).iterator();
		while (iterator.hasNext()) {
			EntityItem itemToGet = (EntityItem) iterator.next();
			if (itemToGet.func_145800_j() != null && itemToGet.func_145800_j().equals(player.getCommandSenderName())) {
				continue;
			}

			EntityPlayer closestPlayer = world.getClosestPlayerToEntity(itemToGet, distanceFromPlayer);

			if (closestPlayer != null && closestPlayer != player) {
				continue;
			}

			if (!world.isRemote) {
				if (itemToGet.delayBeforeCanPickup > 0) {
					itemToGet.delayBeforeCanPickup = 0;
				}
				itemToGet.motionX = itemToGet.motionY = itemToGet.motionZ = 0;
				itemToGet.setPosition(player.posX - 0.2 + (world.rand.nextDouble() * 0.4), player.posY - 0.6, player.posZ - 0.2 + (world.rand.nextDouble() * 0.4));
			}
		}

		// xp
		iterator = getEntitiesInRange(EntityXPOrb.class, world, (int) player.posX, (int) player.posY, (int) player.posZ, distanceFromPlayer).iterator();
		while (iterator.hasNext()) {
			EntityXPOrb xpToGet = (EntityXPOrb) iterator.next();
			if (xpToGet.isDead || xpToGet.isInvisible()) {
				continue;
			}
			int xpAmount = xpToGet.xpValue;
			xpToGet.xpValue = 0;
			player.xpCooldown = 0;
			player.addExperience(xpAmount);
			xpToGet.setDead();
			xpToGet.setInvisible(true);
			world.playSoundAtEntity(player, "random.orb", 0.08F, 0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
		}
	}

	public void doVanillaPickup(EntityItem itemToGet, EntityPlayer player, ItemStack itemStackToGet, World world, int stackSize) {
		if (pickupTimer < 100) {
			pickupTimer++;
			return;
		}
		pickupTimer = 0;
		if (itemToGet.getDistanceToEntity(player) <= 2.0F) {
			if (player.inventory.addItemStackToInventory(itemStackToGet)) {
				player.onItemPickup(itemToGet, stackSize);
				world.playSoundAtEntity(player, "random.pop", 0.15F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			}
		}
	}

	private boolean doesMagnetIgnoreNBT() {
		return RandomUtils.readBoolean(getItemStack(), "IgnoreNBT");
	}

	private boolean doesMagnetIgnoreMeta() {
		return RandomUtils.readBoolean(getItemStack(), "IgnoreMeta");
	}

	private boolean doesMagnetUseOreDict() {
		return RandomUtils.readBoolean(getItemStack(), "UseOreDict");
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

	public boolean isItemFiltered(ItemStack is, List<ItemStack> itemList) {
		if (is != null && itemList != null) {
			for (int i = 0; i < itemList.size(); i++) {
				ItemStack thisStack = itemList.get(i);
				//use oredict
				if (doesMagnetUseOreDict()) {
					if (areOresEqual(is, thisStack)) {
						//if (OreDictionary.itemMatches(is, thisStack, false)) {
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

	public boolean hasNetworkAccess(final SecurityPermissions perm, final boolean requirePower, EntityPlayer player, ItemStack wirelessTerm) {
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

	public List<ItemStack> getFilteredItems(ItemStack magnetItem) {
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
						itemList.add(ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(i)));
					}
					return itemList;
				}
			}
		}
		return null;
	}

	public boolean doInject(IAEItemStack ais, int stackSize, EntityPlayer player, EntityItem itemToGet, ItemStack itemStackToGet, World world) {
		ais = Platform.poweredInsert(powerSrc, cellInv, ais, mySrc);
		if (ais != null) {
			player.onItemPickup(itemToGet, stackSize);
			player.inventory.addItemStackToInventory(itemStackToGet);
			world.playSoundAtEntity(player, "random.pop", 0.15F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			return false;
		}
		return true;
	}

	public boolean isBoosterInstalled(ItemStack wirelessTerm) {
		if (wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem) {
			if (wirelessTerm.hasTagCompound()) {
				NBTTagList boosterNBTList = wirelessTerm.getTagCompound().getTagList("BoosterSlot", 10);
				if (boosterNBTList != null) {
					NBTTagCompound boosterTagCompound = boosterNBTList.getCompoundTagAt(0);
					if (boosterTagCompound != null) {
						ItemStack boosterCard = ItemStack.loadItemStackFromNBT(boosterTagCompound);
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
	public boolean getMode(ItemStack magnetItem) {
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

	public WirelessTerminalGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (it != null) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WirelessTerminalGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	public static List<? extends Entity> getEntitiesInRange(Class<?> entityType, World world, int x, int y, int z, int distance) {
		return world.getEntitiesWithinAABB(entityType, AxisAlignedBB.getBoundingBox(x - distance, y - distance, z - distance, x + distance, y + distance, z + distance));
	}

	public ItemStack getStack() {
		return this.getStack(1);
	}

	public ItemStack getStack(final int size) {
		return new ItemStack(this, size);
	}

	public boolean isActivated(ItemStack item) {
		if (item == null) {
			return false;
		}
		return item.getItemDamage() != 0;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 1;
	}

}
