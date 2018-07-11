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
package p455w0rd.wct.util;

import static p455w0rd.wct.init.ModConfig.INFINITY_ENERGY_DRAIN;
import static p455w0rd.wct.init.ModConfig.INFINITY_ENERGY_PER_BOOSTER_CARD;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.wct.api.IWirelessCraftingTermHandler;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.IWirelessFluidTermHandler;
import p455w0rd.wct.api.IWirelessFluidTerminalItem;
import p455w0rd.wct.container.ContainerMagnet;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.ContainerWFT;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.init.ModIntegration.Mods;
import p455w0rd.wct.init.ModItems;
import p455w0rd.wct.init.ModKeybindings;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.integration.Baubles;
import p455w0rd.wct.items.ItemInfinityBooster;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.sync.packets.PacketMagnetFilter;
import p455w0rd.wct.sync.packets.PacketOpenGui;
import p455w0rd.wct.sync.packets.PacketSyncInfinityEnergy;

public class WCTUtils {

	public static final String INFINITY_ENERGY_NBT = "InfinityEnergy";
	public static final String BOOSTER_SLOT_NBT = "BoosterSlot";
	public static final String MAGNET_SLOT_NBT = "MagnetSlot";
	public static final String TIMER_RESET_NBT = "WCTReset";
	public static final String TIMER_PICKUP_NBT = "WCTPickupTimer";
	public static final String IN_RANGE_NBT = "IsInRange";
	public static final String AUTOCONSUME_BOOSTER_NBT = "AutoConsumeBoosters";
	public static final String MAGNET_MODE_NBT = "MagnetMode";

	public static NonNullList<ItemStack> getWirelessTerminals(EntityPlayer player) {
		NonNullList<ItemStack> terminalList = NonNullList.<ItemStack>create();
		for (ItemStack stack : getCraftingTerminals(player)) {
			terminalList.add(stack);
		}
		for (ItemStack stack : getFluidTerminals(player)) {
			terminalList.add(stack);
		}
		return terminalList;
	}

	public static NonNullList<ItemStack> getCraftingTerminals(EntityPlayer player) {
		NonNullList<ItemStack> terminalList = NonNullList.<ItemStack>create();
		InventoryPlayer playerInventory = player.inventory;
		for (ItemStack wirelessTerm : playerInventory.mainInventory) {
			if (isAnyWCT(wirelessTerm)) {
				terminalList.add(wirelessTerm);
			}
		}
		if (Mods.BAUBLES.isLoaded()) {
			if (!Baubles.getWCTBauble(player).isEmpty()) {
				terminalList.add(Baubles.getWCTBauble(player));
			}
		}
		return terminalList;
	}

	public static NonNullList<ItemStack> getFluidTerminals(EntityPlayer player) {
		NonNullList<ItemStack> terminalList = NonNullList.<ItemStack>create();
		InventoryPlayer playerInventory = player.inventory;
		for (ItemStack fluidTerm : playerInventory.mainInventory) {
			if (isAnyWFT(fluidTerm)) {
				terminalList.add(fluidTerm);
			}
		}
		if (Mods.BAUBLES.isLoaded()) {
			if (!Baubles.getWFTBauble(player).isEmpty()) {
				terminalList.add(Baubles.getWFTBauble(player));
			}
		}
		return terminalList;
	}

	@Nonnull
	public static ItemStack getWirelessTerm(InventoryPlayer playerInv) {
		if (!playerInv.player.getHeldItemMainhand().isEmpty() && playerInv.player.getHeldItemMainhand().getItem() instanceof IWirelessCraftingTerminalItem) {
			return playerInv.player.getHeldItemMainhand();
		}
		ItemStack wirelessTerm = ItemStack.EMPTY;
		if (Mods.BAUBLES.isLoaded()) {
			wirelessTerm = Baubles.getWCTBauble(playerInv.player);
		}
		if (wirelessTerm.isEmpty()) {
			int invSize = playerInv.getSizeInventory();
			if (invSize <= 0) {
				return ItemStack.EMPTY;
			}
			for (int i = 0; i < invSize; ++i) {
				ItemStack item = playerInv.getStackInSlot(i);
				if (item.isEmpty()) {
					continue;
				}
				if (item.getItem() instanceof IWirelessCraftingTerminalItem) {
					wirelessTerm = item;
					break;
				}
			}
		}
		return wirelessTerm;
	}

	@Nonnull
	public static ItemStack getFluidTerm(InventoryPlayer playerInv) {
		if (!playerInv.player.getHeldItemMainhand().isEmpty() && playerInv.player.getHeldItemMainhand().getItem() instanceof IWirelessFluidTerminalItem) {
			return playerInv.player.getHeldItemMainhand();
		}
		ItemStack fluidTerm = ItemStack.EMPTY;
		if (Mods.BAUBLES.isLoaded()) {
			fluidTerm = Baubles.getWFTBauble(playerInv.player);
		}
		if (fluidTerm.isEmpty()) {
			int invSize = playerInv.getSizeInventory();
			if (invSize <= 0) {
				return ItemStack.EMPTY;
			}
			for (int i = 0; i < invSize; ++i) {
				ItemStack item = playerInv.getStackInSlot(i);
				if (item.isEmpty()) {
					continue;
				}
				if (item.getItem() instanceof IWirelessFluidTerminalItem) {
					fluidTerm = item;
					break;
				}
			}
		}
		return fluidTerm;
	}

	public static boolean shouldConsumeBoosters(ItemStack wirelessTerminal) {
		if (!ModConfig.USE_OLD_INFINTY_MECHANIC && wirelessTerminal.hasTagCompound()) {
			if (wirelessTerminal.getTagCompound().hasKey(AUTOCONSUME_BOOSTER_NBT)) {
				return wirelessTerminal.getTagCompound().getBoolean(AUTOCONSUME_BOOSTER_NBT);
			}
		}
		return false;
	}

	public static boolean isBoosterInstalled(final ItemStack wirelessTerminal) {
		if (wirelessTerminal.hasTagCompound()) {
			NBTTagCompound boosterNBT = wirelessTerminal.getSubCompound(BOOSTER_SLOT_NBT);
			if (boosterNBT != null) {
				NBTTagList boosterNBTList = boosterNBT.getTagList("Items", 10);
				if (boosterNBTList != null) {
					NBTTagCompound boosterTagCompound = boosterNBTList.getCompoundTagAt(0);
					if (boosterTagCompound != null) {
						ItemStack boosterCard = new ItemStack(boosterTagCompound);
						if (boosterCard != null && !boosterCard.isEmpty()) {
							return ((boosterCard.getItem() instanceof ItemInfinityBooster) && ModConfig.WCT_BOOSTER_ENABLED);
						}
					}
				}
			}
		}
		return false;
	}

	public static void setInRange(ItemStack wirelessTerm, boolean value) {
		NBTTagCompound nbt = ensureTag(wirelessTerm);
		nbt.setBoolean(IN_RANGE_NBT, value);
		wirelessTerm.setTagCompound(nbt);
	}

	public static boolean isInRange(ItemStack wirelessTerm) {
		NBTTagCompound nbt = ensureTag(wirelessTerm);
		return (nbt.hasKey(IN_RANGE_NBT) && nbt.getBoolean(IN_RANGE_NBT)) || (isWCTCreative(wirelessTerm) || isWFTCreative(wirelessTerm));
	}

	public static ItemStack addInfinityBoosters(@Nonnull ItemStack wirelessTerm, ItemStack boosterCardStack) {
		int currentCardCount = getInfinityEnergy(wirelessTerm) / INFINITY_ENERGY_PER_BOOSTER_CARD;
		int maxCardCount = Integer.MAX_VALUE / INFINITY_ENERGY_PER_BOOSTER_CARD;
		if (currentCardCount < maxCardCount) {
			int spaceAvailable = maxCardCount - currentCardCount;
			int numberOfCardsTryingToAdd = boosterCardStack.getCount();
			if (spaceAvailable > 0 && numberOfCardsTryingToAdd > 0) { //can we at least add 1 card?
				int cardsTryingToAdd = numberOfCardsTryingToAdd * INFINITY_ENERGY_PER_BOOSTER_CARD;
				if (cardsTryingToAdd <= spaceAvailable) {
					setInfinityEnergy(wirelessTerm, cardsTryingToAdd + currentCardCount * INFINITY_ENERGY_PER_BOOSTER_CARD);
					if (cardsTryingToAdd == spaceAvailable) {
						boosterCardStack = ItemStack.EMPTY;
					}
					else {
						boosterCardStack.setCount(cardsTryingToAdd - spaceAvailable);
					}
				}
			}
		}
		return boosterCardStack;
	}

	public static boolean hasInfiniteRange(@Nonnull ItemStack wirelessTerm) {
		if (ModConfig.USE_OLD_INFINTY_MECHANIC) {
			return isBoosterInstalled(wirelessTerm) || (isWCTCreative(wirelessTerm) || isWFTCreative(wirelessTerm));
		}
		else {
			return hasInfinityEnergy(wirelessTerm) || (isWCTCreative(wirelessTerm) || isWFTCreative(wirelessTerm));
		}
	}

	public static boolean hasInfinityEnergy(@Nonnull ItemStack wirelessTerm) {
		if (ensureTag(wirelessTerm).hasKey(INFINITY_ENERGY_NBT)) {
			return getInfinityEnergy(wirelessTerm) > 0 && ModConfig.WCT_BOOSTER_ENABLED;
		}
		return (isWCTCreative(wirelessTerm) || isWFTCreative(wirelessTerm));
	}

	public static boolean isAnyWCT(@Nonnull ItemStack wirelessTerm) {
		return wirelessTerm.getItem() == ModItems.WCT || wirelessTerm.getItem() == ModItems.CREATIVE_WCT || wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem;
	}

	public static boolean isAnyWFT(@Nonnull ItemStack wirelessTerm) {
		return wirelessTerm.getItem() == ModItems.WFT || wirelessTerm.getItem() == ModItems.CREATIVE_WFT || wirelessTerm.getItem() instanceof IWirelessFluidTerminalItem;
	}

	public static boolean isInRangeOfWAP(@Nonnull ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
		return getDistanceToWAP(wirelessTerm, player) <= getWAPRange(wirelessTerm, player) && getWAPRange(wirelessTerm, player) != Double.MAX_VALUE;
	}

	public static double getDistanceToWAP(@Nonnull ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
		IWirelessAccessPoint wap = getClosestWAPToPlayer(wirelessTerm, player);
		if (wap != null && player.getEntityWorld().provider.getDimension() == wap.getLocation().getWorld().provider.getDimension()) {
			BlockPos wapPos = wap.getLocation().getPos();
			BlockPos playerPos = player.getPosition();
			double distanceToWap = Math.sqrt(playerPos.distanceSq(wapPos));
			return distanceToWap;
		}
		return Double.MAX_VALUE;
	}

	public static double getWAPRange(@Nonnull ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
		IWirelessAccessPoint wap = getClosestWAPToPlayer(wirelessTerm, player);
		if (wap != null) {
			return wap.getRange();
		}
		return Double.MAX_VALUE;
	}

	public static IWirelessAccessPoint getClosestWAPToPlayer(@Nonnull ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
		List<IWirelessAccessPoint> wapList = getWAPs(wirelessTerm, player);
		double closestDistance = -1.0D;
		IWirelessAccessPoint closestWAP = null;
		for (IWirelessAccessPoint wap : wapList) {
			BlockPos wapPos = wap.getLocation().getPos();
			BlockPos playerPos = player.getPosition();
			double thisWAPDistance = Math.sqrt(playerPos.distanceSq(wapPos));
			if (closestDistance == -1.0D) {
				closestDistance = thisWAPDistance;
				closestWAP = wap;
			}
			else {
				if (thisWAPDistance < closestDistance) {
					closestDistance = thisWAPDistance;
					closestWAP = wap;
				}
			}
		}
		return closestDistance == -1.0D ? null : closestWAP;
	}

	public static List<IWirelessAccessPoint> getWAPs(@Nonnull ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
		if (isAnyWCT(wirelessTerm) || isAnyWFT(wirelessTerm)) {
			WCTGuiObject<?> object = getGUIObject(wirelessTerm, player);
			if (object != null) {
				return object.getWAPs();
			}
		}
		/*
		else if (isAnyWFT(wirelessTerm)) {
			WCTFluidGuiObject object = getFluidGUIObject(wirelessTerm, player);
			if (object != null) {
				return object.getWAPs();
			}
		}
		*/
		return Collections.emptyList();
	}

	/*
	public static IWirelessAccessPoint getWAP(@Nonnull ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
		WCTGuiObject object = getGUIObject(wirelessTerm, player);
		if (object != null) {
			return object.getWAP();
		}
		return null;
	}
	*/
	public static WCTGuiObject<?> getGUIObject(EntityPlayer player) {
		return getGUIObject(null, player);
	}

	public static WCTGuiObject<?> getGUIObject(@Nullable ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
		if (wirelessTerm == null) {
			if (player.openContainer instanceof ContainerWCT) {
				ContainerWCT c = (ContainerWCT) player.openContainer;
				if (c.getObject() != null) {
					return (WCTGuiObject<?>) c.getObject();
				}
			}
			else if (player.openContainer instanceof ContainerWFT) {
				ContainerWFT c = (ContainerWFT) player.openContainer;
				if (c.getObject() != null) {
					return (WCTGuiObject<?>) c.getObject();
				}
			}
		}
		else {
			if (wirelessTerm.getItem() instanceof IWirelessCraftingTermHandler) {
				if (player != null && player.getEntityWorld() != null) {
					return new WCTGuiObject<IAEItemStack>((IWirelessCraftingTermHandler) wirelessTerm.getItem(), wirelessTerm, player, player.getEntityWorld(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
				}
			}
			else if (wirelessTerm.getItem() instanceof IWirelessFluidTermHandler) {
				return new WCTGuiObject<IAEItemStack>((IWirelessFluidTermHandler) wirelessTerm.getItem(), wirelessTerm, player, player.getEntityWorld(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
			}
		}
		return null;
	}

	/*
		public static WCTFluidGuiObject getFluidGUIObject(EntityPlayer player) {
			return getFluidGUIObject(null, player);
		}

		public static WCTFluidGuiObject getFluidGUIObject(@Nullable ItemStack wirelessTerm, @Nonnull EntityPlayer player) {
			//if (wirelessTerm == null) {
			if (player.openContainer instanceof ContainerWFT || wirelessTerm == null) {
				ContainerWFT c = (ContainerWFT) player.openContainer;
				if (c.getObject() != null && c.getObject() instanceof WCTFluidGuiObject) {
					return (WCTFluidGuiObject) c.getObject();
				}
			}
			//}
			else {
				if (wirelessTerm.getItem() instanceof IWirelessFluidTermHandler) {
					if (player != null && player.getEntityWorld() != null) {
						return new WCTFluidGuiObject((IWirelessFluidTermHandler) wirelessTerm.getItem(), wirelessTerm, player, player.getEntityWorld(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
					}
				}
			}
			return null;
		}
	*/
	public static void setInfinityEnergy(@Nonnull ItemStack wirelessTerm, int amount) {
		if (!isWCTCreative(wirelessTerm) || !isWFTCreative(wirelessTerm)) {
			NBTTagCompound nbt = ensureTag(wirelessTerm);
			nbt.setInteger(INFINITY_ENERGY_NBT, amount);
			wirelessTerm.setTagCompound(nbt);
		}
	}

	public static int getInfinityEnergy(@Nonnull ItemStack wirelessTerm) {
		NBTTagCompound nbt = ensureTag(wirelessTerm);
		if (!nbt.hasKey(INFINITY_ENERGY_NBT) && (!isWCTCreative(wirelessTerm) || !isWFTCreative(wirelessTerm))) {
			nbt.setInteger(INFINITY_ENERGY_NBT, 0);
		}
		return (isWCTCreative(wirelessTerm) || isWFTCreative(wirelessTerm)) ? Integer.MAX_VALUE : nbt.getInteger(INFINITY_ENERGY_NBT);
	}

	public static void drainInfinityEnergy(@Nonnull ItemStack wirelessTerm, EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			if (!ModConfig.USE_OLD_INFINTY_MECHANIC && (!isWCTCreative(wirelessTerm) || !isWFTCreative(wirelessTerm))) {
				int current = getInfinityEnergy(wirelessTerm);
				if (!isInRangeOfWAP(wirelessTerm, player)) {
					int reducedAmount = current - INFINITY_ENERGY_DRAIN;
					if (reducedAmount < 0) {
						reducedAmount = 0;
					}
					setInfinityEnergy(wirelessTerm, reducedAmount);

					//if (current > WARNING_AMOUNT && reducedAmount < WARNING_AMOUNT) {
					ModNetworking.instance().sendTo(new PacketSyncInfinityEnergy(getInfinityEnergy(wirelessTerm)), (EntityPlayerMP) player);
					//}
				}
			}
		}
	}

	public static NBTTagCompound ensureTag(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		return stack.getTagCompound();
	}

	@Nonnull
	public static ItemStack getMagnet(@Nonnull ItemStack wirelessTerm) {
		if (!wirelessTerm.isEmpty() && wirelessTerm.hasTagCompound() && wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem) {
			NBTTagCompound magnetNBT = wirelessTerm.getSubCompound(MAGNET_SLOT_NBT);
			if (magnetNBT != null) {
				NBTTagList magnetSlot = magnetNBT.getTagList("Items", 10);
				ItemStack magnetItem = new ItemStack(magnetSlot.getCompoundTagAt(0));
				if (magnetItem != null && !magnetItem.isEmpty() && magnetItem.getItem() == ModItems.MAGNET_CARD) {
					return magnetItem;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	public static int getMagnetMode(ItemStack wirelessTerm) {
		if (!getMagnet(wirelessTerm).isEmpty()) {
			return ItemMagnet.getDamageUnsafe(getMagnet(wirelessTerm));
		}
		return -1;
	}

	@Nonnull
	public static ItemStack getMagnet(InventoryPlayer playerInv) {
		// Is player holding a Magnet Card?
		if (!playerInv.player.getHeldItemMainhand().isEmpty() && playerInv.player.getHeldItemMainhand().getItem() == ModItems.MAGNET_CARD) {
			return playerInv.player.getHeldItemMainhand();
		}
		// if not true, try to return first magnet card from first
		// wireless term that has a MagnetCard installed
		ItemStack wirelessTerm = getWirelessTerm(playerInv);
		if (!wirelessTerm.isEmpty() && wirelessTerm.hasTagCompound() && wirelessTerm.getItem() instanceof IWirelessCraftingTerminalItem) {
			NBTTagCompound magnetNBT = wirelessTerm.getSubCompound(MAGNET_SLOT_NBT);
			if (magnetNBT != null) {
				NBTTagList magnetSlot = magnetNBT.getTagList("Items", 10);
				ItemStack magnetItem = new ItemStack(magnetSlot.getCompoundTagAt(0));
				if (magnetItem != null && !magnetItem.isEmpty() && magnetItem.getItem() == ModItems.MAGNET_CARD) {
					return magnetItem;
				}
			}
		}
		// No wireless crafting terminal with Magnet Card installed,
		// is there a Magnet Card in the player's inventory?
		int invSize = playerInv.getSizeInventory();
		ItemStack magnetItem = ItemStack.EMPTY;
		if (invSize <= 0) {
			return ItemStack.EMPTY;
		}
		for (int i = 0; i < invSize; ++i) {
			ItemStack item = playerInv.getStackInSlot(i);
			if (item.isEmpty()) {
				continue;
			}
			if (item.getItem() == ModItems.MAGNET_CARD) {
				magnetItem = item;
				break;
			}
		}
		return magnetItem;
	}

	public static boolean isMagnetInitialized(@Nonnull ItemStack magnetItem) {
		if (!magnetItem.isEmpty() && magnetItem.getItem() == ModItems.MAGNET_CARD) {
			if (!magnetItem.hasTagCompound()) {
				magnetItem.setTagCompound(new NBTTagCompound());
			}
			if (!magnetItem.getTagCompound().hasKey("Initialized")) {
				magnetItem.getTagCompound().setBoolean("Initialized", true);
			}
		}
		return magnetItem.getTagCompound().getBoolean("Initialized");
	}

	public static boolean isMagnetInstalled(InventoryPlayer ip) {
		return isMagnetInstalled(getWirelessTerm(ip));
	}

	public static boolean isMagnetInstalled(ItemStack wirelessTerm) {
		if (!wirelessTerm.isEmpty() && wirelessTerm.hasTagCompound() && wirelessTerm.getTagCompound().hasKey(MAGNET_SLOT_NBT)) {
			NBTTagCompound magnetNBT = wirelessTerm.getSubCompound(MAGNET_SLOT_NBT);
			if (magnetNBT != null) {
				NBTTagList magnetList = magnetNBT.getTagList("Items", 10);
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

	public static boolean isWCTCreative(ItemStack wirelessTerm) {
		return !wirelessTerm.isEmpty() && wirelessTerm.getItem() == ModItems.CREATIVE_WCT;
	}

	public static boolean isWFTCreative(ItemStack fluidTerm) {
		return !fluidTerm.isEmpty() && fluidTerm.getItem() == ModItems.CREATIVE_WFT;
	}

	public static ItemStack setCreativeWCT(ItemStack wirelessTerm, boolean makeCreative) {
		if (!wirelessTerm.isEmpty()) {
			if (makeCreative) {
				if (wirelessTerm.getItem() == ModItems.WCT) {
					ItemStack creativeStack = new ItemStack(ModItems.CREATIVE_WCT);
					if (wirelessTerm.hasTagCompound()) {
						creativeStack.setTagCompound(wirelessTerm.writeToNBT(new NBTTagCompound()));
					}
					wirelessTerm = creativeStack.copy();
				}
			}
			else if (wirelessTerm.getItem() == ModItems.CREATIVE_WCT) {
				ItemStack wirelessStack = new ItemStack(ModItems.WCT);
				if (wirelessTerm.hasTagCompound()) {
					wirelessStack.setTagCompound(wirelessTerm.writeToNBT(new NBTTagCompound()));
				}
				wirelessTerm = wirelessStack.copy();
			}
		}
		return wirelessTerm;
	}

	public static ItemStack getCreativeWCT(ItemStack wirelessTerm) {
		return setCreativeWCT(wirelessTerm, true);
	}

	public static void removeTimerTags(ItemStack is) {
		if (is.isEmpty() || is.getTagCompound() == null) {
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

	@SideOnly(Side.CLIENT)
	public static String color(String color) {
		switch (color) {
		case "white":
			return TextFormatting.WHITE.toString();
		case "black":
			return TextFormatting.BLACK.toString();
		case "green":
			return TextFormatting.GREEN.toString();
		case "red":
			return TextFormatting.RED.toString();
		case "yellow":
			return TextFormatting.YELLOW.toString();
		case "aqua":
			return TextFormatting.AQUA.toString();
		case "blue":
			return TextFormatting.BLUE.toString();
		case "italics":
			return TextFormatting.ITALIC.toString();
		case "bold":
			return TextFormatting.BOLD.toString();
		default:
		case "gray":
			return TextFormatting.GRAY.toString();
		}
	}

	@SideOnly(Side.CLIENT)
	public static EntityPlayer player() {
		return Minecraft.getMinecraft().player;
	}

	public static EntityPlayer player(InventoryPlayer playerInv) {
		return playerInv.player;
	}

	@SideOnly(Side.CLIENT)
	public static World world() {
		return Minecraft.getMinecraft().world;
	}

	public static World world(EntityPlayer player) {
		return player.getEntityWorld();
	}

	public static void chatMessage(EntityPlayer player, ITextComponent message) {
		player.sendMessage(message);
	}

	@SideOnly(Side.CLIENT)
	public static void handleKeybind() {
		EntityPlayer p = WCTUtils.player();
		if (p.openContainer == null) {
			return;
		}
		if (ModKeybindings.openTerminal.getKeyCode() != Keyboard.CHAR_NONE && ModKeybindings.openTerminal.isPressed()) {
			ItemStack is = WCTUtils.getWirelessTerm(p.inventory);
			if (is.isEmpty()) {
				return;
			}
			IWirelessCraftingTerminalItem wirelessTerm = (IWirelessCraftingTerminalItem) is.getItem();
			if (wirelessTerm != null && wirelessTerm.isWirelessCraftingEnabled(is)) {
				if (!(p.openContainer instanceof ContainerWCT)) {
					ModNetworking.instance().sendToServer(new PacketOpenGui(ModGuiHandler.GUI_WCT));
				}
				else {
					p.closeScreen();
				}
			}
		}
		else if (ModKeybindings.openMagnetFilter.getKeyCode() != Keyboard.CHAR_NONE && ModKeybindings.openMagnetFilter.isPressed()) {
			ItemStack magnetItem = WCTUtils.getMagnet(p.inventory);
			if (!magnetItem.isEmpty()) {
				if (!WCTUtils.isMagnetInitialized(magnetItem)) {
					if (magnetItem.getTagCompound() == null) {
						magnetItem.setTagCompound(new NBTTagCompound());
					}
					ModNetworking.instance().sendToServer(new PacketMagnetFilter(0, true));
				}
				if (!(p.openContainer instanceof ContainerMagnet)) {
					ModNetworking.instance().sendToServer(new PacketOpenGui(ModGuiHandler.GUI_MAGNET));
				}
			}
		}
		else if (ModKeybindings.changeMagnetMode.getKeyCode() != Keyboard.CHAR_NONE && ModKeybindings.changeMagnetMode.isPressed()) {
			ItemStack magnetItem = WCTUtils.getMagnet(p.inventory);
			if (!magnetItem.isEmpty()) {
				if (!WCTUtils.isMagnetInitialized(magnetItem)) {
					if (!magnetItem.hasTagCompound()) {
						magnetItem.setTagCompound(new NBTTagCompound());
					}
					ModNetworking.instance().sendToServer(new PacketMagnetFilter(0, true));
				}
				ItemMagnet.switchMagnetMode(magnetItem);
			}
		}
		else if (ModKeybindings.openFluidTerminal.getKeyCode() != Keyboard.CHAR_NONE && ModKeybindings.openFluidTerminal.isPressed()) {
			ItemStack is = WCTUtils.getFluidTerm(p.inventory);
			if (is.isEmpty()) {
				return;
			}
			IWirelessFluidTerminalItem fluidTerm = (IWirelessFluidTerminalItem) is.getItem();
			if (fluidTerm != null && fluidTerm.isWirelessFluidEnabled(is)) {
				if (!(p.openContainer instanceof ContainerWCT)) {
					ModNetworking.instance().sendToServer(new PacketOpenGui(ModGuiHandler.GUI_WFT));
				}
				else {
					p.closeScreen();
				}
			}
		}
	}

}
