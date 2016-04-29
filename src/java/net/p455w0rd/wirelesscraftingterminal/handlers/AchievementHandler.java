package net.p455w0rd.wirelesscraftingterminal.handlers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.AchievementEvent;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.items.ItemInfinityBooster;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class AchievementHandler {

	private static ItemInfinityBooster booster = (ItemInfinityBooster) ItemEnum.BOOSTER_ICON.getItem();
	public static final Achievement wctAch = new Achievement("achievement.wctAchievement", "wctAchievement", 0, 0, ItemEnum.WIRELESS_CRAFTING_TERMINAL.getStack(), (Achievement) null);
	public static final Achievement boosterAch = new Achievement("achievment.boosterAchievement", "boosterAchievement", 0, 2, booster.hasEffect(true), wctAch);
	public static final Achievement magnetAch = new Achievement("achievement.magnetAchievement", "magnetAchievement", 2, 0, ItemEnum.MAGNET_CARD.getItem(), wctAch);
	private static final AchievementPage achPage = new AchievementPage("Wireless Crafting Term", wctAch, boosterAch, magnetAch);
	
	public static void init() {
		registerAchievements();
		registerPage();
	}

	public static void registerAchievements() {
		if (!AchievementList.achievementList.contains(wctAch)) {
			wctAch.initIndependentStat().registerStat();
			boosterAch.initIndependentStat().registerStat().setSpecial();
			magnetAch.initIndependentStat().registerStat();
		}
	}

	public static void registerPage() {
		if (AchievementPage.getAchievementPage("Wireless Crafting Term") == null) {
			AchievementPage.registerAchievementPage(achPage);
		}
	}

	public static void addAchievementToPage(Achievement a) {
		addAchievementToPage(a, false, null);
	}

	public static void addAchievementToPage(Achievement a, boolean hidden, EntityPlayer player) {
		if (hidden) {
			if (player != null && FMLCommonHandler.instance().getSide() == Side.CLIENT) {
				if (!isAchievementUnlocked(player, wctAch) || achPage.getAchievements().contains(boosterAch)) {
					return;
				}
			}
		}
		achPage.getAchievements().add(a);
	}
	
	public static boolean isAchievementUnlocked(EntityPlayer p, Achievement a) {
		return ((EntityPlayerMP) p).func_147099_x().hasAchievementUnlocked(a);
	}

	public static void triggerAch(Achievement a, EntityPlayer p) {
		p.addStat(a, 1);
	}
	
}
