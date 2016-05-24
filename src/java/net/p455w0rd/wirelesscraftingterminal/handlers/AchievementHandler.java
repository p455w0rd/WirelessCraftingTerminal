package net.p455w0rd.wirelesscraftingterminal.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.AchievementPage;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;

public class AchievementHandler {

	public static final Achievement wctAch = new Achievement("achievement.wctAchievement", "wctAchievement", 0, 0, new ItemStack(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem(), 1), (Achievement) null);
	public static final Achievement boosterAch = new Achievement("achievment.boosterAchievement", "boosterAchievement", 0, 2, ItemEnum.BOOSTER_ICON.getItem(), wctAch);
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
		achPage.getAchievements().add(a);
	}
	


	public static void triggerAch(Achievement a, EntityPlayer p) {
		p.addStat(a, 1);
	}
	
}
