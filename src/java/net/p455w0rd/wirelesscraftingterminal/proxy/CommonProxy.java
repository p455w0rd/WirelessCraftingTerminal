package net.p455w0rd.wirelesscraftingterminal.proxy;

import java.util.Random;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketConfigSync;
import net.p455w0rd.wirelesscraftingterminal.handlers.AchievementHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.ConfigHandler;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class CommonProxy {

	Achievement wctAch = AchievementHandler.wctAch;
	Achievement boosterAch = AchievementHandler.boosterAch;
	Achievement magnetAch = AchievementHandler.magnetAch;

	public CommonProxy() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(new ConfigHandler());
	}

	public void registerItems() {
		for (ItemEnum item : ItemEnum.VALUES) {
			GameRegistry.registerItem(item.getItem(), item.getInternalName());
		}
	}

	public void removeItemsFromNEI() {
		// XD
	}

	public void registerRenderers() {
		// =P
	}

	public void missingCoreMod() {
		throw new IllegalStateException("Unable to Load WCT Core Mod, please verify that WCT is properly install in the mods folder, with a .jar extension.");
	}

	@SubscribeEvent
	public void tickEvent(TickEvent.PlayerTickEvent e) {
		ItemStack wirelessTerm = null;
		EntityPlayer player = e.player;
		IInventory playerInv;
		if (player instanceof EntityPlayerMP) {
			playerInv = player.inventory.player.inventory;
		}
		else {
			playerInv = player.inventory;
		}
		int invSize = playerInv.getSizeInventory();
		if (invSize <= 0) {
			return;
		}
		for (int i = 0; i < invSize; ++i) {
			ItemStack item = playerInv.getStackInSlot(i);
			if (item == null) {
				continue;
			}
			if (item.getItem() instanceof ItemWirelessCraftingTerminal) {
				wirelessTerm = item;
			}
			if (wirelessTerm == null) {
				continue;
			}
			if (wirelessTerm.hasTagCompound()) {
				NBTTagCompound nbtTC = wirelessTerm.getTagCompound();
				NBTTagList tagList = nbtTC.getTagList("MagnetSlot", 10);
				if (tagList != null) {
					NBTTagCompound magCompound = tagList.getCompoundTagAt(0);
					if (magCompound != null) {
						ItemStack magnetItem = ItemStack.loadItemStackFromNBT(magCompound);
						if (magnetItem != null) {
							((ItemMagnet) magnetItem.getItem()).setItemStack(magnetItem);
							if (magnetItem.getItem() instanceof ItemMagnet) {
								((ItemMagnet) magnetItem.getItem()).doMagnet(magnetItem, e.player.worldObj, e.player, wirelessTerm);
								continue;
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent e) {
		// SSP Login
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			//boolean hasWCTAch = ((EntityPlayerMP) e.player).func_147099_x().hasAchievementUnlocked(AchievementHandler.wctAch);
			//boolean hasBoosterAch = ((EntityPlayerMP) e.player).func_147099_x().hasAchievementUnlocked(AchievementHandler.boosterAch);

			ConfigHandler.removeBooster();
			ConfigHandler.removeBoosterIcon();

			if (ConfigHandler.firstLoad) {
				ConfigHandler.firstLoad = false;
			}
			else {
				ConfigHandler.reloadRecipes();
			}
			// Was using this for hidden booster achievement
			// it was giving me too much of a headache..maybe
			// i'll implement in the future
			/*
			 * AchievementPage achPg = AchievementPage.getAchievementPage(
			 * "Wireless Crafting Term"); if (achPg != null &&
			 * AchievementList.achievementList.contains(AchievementHandler.
			 * boosterAch) &&
			 * !achPg.getAchievements().contains(AchievementHandler.boosterAch))
			 * { AchievementList.achievementList.remove(AchievementHandler.
			 * boosterAch); } if ((Reference.WCT_BOOSTER_ENABLED && hasWCTAch)
			 * || hasBoosterAch) {
			 * AchievementHandler.addAchievementToPage(AchievementHandler.
			 * boosterAch, true, e.player); }
			 */
		}
		else {
			final PacketConfigSync p = new PacketConfigSync(Reference.WCT_MAX_POWER, Reference.WCT_EASYMODE_ENABLED, Reference.WCT_BOOSTER_ENABLED, Reference.WCT_BOOSTER_DROPCHANCE);
			NetworkHandler.instance.sendTo((WCTPacket) p, (EntityPlayerMP) e.player);
		}
	}

	@SubscribeEvent
	public void onMobDrop(LivingDropsEvent event) {
		if (!Reference.WCT_EASYMODE_ENABLED && Reference.WCT_BOOSTER_ENABLED && Reference.WCT_BOOSTERDROP_ENABLED) {
			ItemStack stack = new ItemStack(ItemEnum.BOOSTER_CARD.getItem());
			EntityItem drop = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, stack);
			if (event.entity instanceof EntityDragon) {
				event.drops.add(drop);
			}
			if (event.entity instanceof EntityWither) {
				Random rand = new Random();
				int n = rand.nextInt();
				if (n <= Reference.WCT_BOOSTER_DROPCHANCE) {
					event.drops.add(drop);
				}
			}
		}
	}

	@SubscribeEvent
	public void pickupEvent(PlayerEvent.ItemPickupEvent e) {
		if (Reference.WCT_BOOSTER_ENABLED && !Reference.WCT_EASYMODE_ENABLED) {
			if (e.pickedUp.getEntityItem().getItem() == ItemEnum.BOOSTER_CARD.getItem()) {
				if (AchievementHandler.isAchievementUnlocked(e.player, wctAch)) {
					AchievementHandler.triggerAch(boosterAch, e.player);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerCraftingEvent(final PlayerEvent.ItemCraftedEvent event) {
		if (event.player == null || event.player.isDead || event.player instanceof FakePlayer || event.crafting == null) {
			return;
		}
		if (Loader.isModLoaded("LogisticsPipes")) {
			if (event.player instanceof logisticspipes.blocks.crafting.FakePlayer) {
				return;
			}
		}
		boolean hasWCTAch = false;
		if (event.player instanceof EntityPlayerMP) {
			hasWCTAch = ((EntityPlayerMP) event.player).func_147099_x().hasAchievementUnlocked(AchievementHandler.wctAch);
		}
		if (event.player instanceof EntityClientPlayerMP) {
			hasWCTAch = ((EntityClientPlayerMP) event.player).getStatFileWriter().hasAchievementUnlocked(AchievementHandler.wctAch);
		}
		if (event.crafting.getItem() == ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem()) {
			AchievementHandler.triggerAch(wctAch, event.player);
		}
		if (event.crafting.getItem() == ItemEnum.MAGNET_CARD.getItem() && hasWCTAch) {
			AchievementHandler.triggerAch(magnetAch, event.player);
		}
		/*
		 * if (event.crafting.getItem() ==
		 * ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem()) {
		 * AchievementHandler.triggerAch(wctAch, event.player); if
		 * (Reference.WCT_BOOSTER_ENABLED) {
		 * AchievementHandler.addAchievementToPage(AchievementHandler.
		 * boosterAch, true, event.player); } }
		 */
		if (Reference.WCT_BOOSTER_ENABLED && Reference.WCT_EASYMODE_ENABLED && hasWCTAch) {
			if (event.crafting.getItem() == ItemEnum.BOOSTER_CARD.getItem()) {
				AchievementHandler.triggerAch(boosterAch, event.player);
			}
		}
	}

	/*
	 * @SubscribeEvent public void achievementGetEvent(AchievementEvent event) {
	 * int index = AchievementList.achievementList.indexOf(wctAch); if
	 * (event.achievement.equals(AchievementList.achievementList.get(index))) {
	 * if (Reference.WCT_BOOSTER_ENABLED) { EnumChatFormatting purple =
	 * EnumChatFormatting.LIGHT_PURPLE; EnumChatFormatting green =
	 * EnumChatFormatting.GREEN; event.entityPlayer.addChatMessage(new
	 * ChatComponentText(EnumChatFormatting.ITALIC + "" + purple + "[" + green +
	 * "Wireless Crafting Terminal" + purple + "]: " + green + " New" + purple +
	 * " Achievment" + green + " Unlocked" + purple + "!")); } } }
	 */
}