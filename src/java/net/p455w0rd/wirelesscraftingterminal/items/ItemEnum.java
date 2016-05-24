package net.p455w0rd.wirelesscraftingterminal.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum ItemEnum {
	BOOSTER_CARD("infinityBoosterCard", new ItemInfinityBooster().setUnlocalizedName("infinityBoosterCard")),
	BOOSTER_ICON("infinityBoosterIcon", new ItemInfinityBooster().hasEffect(true)),
	WIRELESS_CRAFTING_TERMINAL("wirelessCraftingTerminal", new ItemWirelessCraftingTerminal()),
	MAGNET_CARD("magnetCard", new ItemMagnet().setUnlocalizedName("magnetCard")),
	WCT_BOOSTER_BG_ICON("boosterBG", new ItemBoosterBGIcon());

	private final String internalName;
	private Item item;
	public static final ItemEnum[] VALUES = ItemEnum.values();

	private ItemEnum(final String internalName, final Item item) {
		this.internalName = internalName;

		this.item = item;
	}

	public ItemStack getDMGStack(final int damageValue) {
		return this.getDMGStack(damageValue, 1);
	}

	public ItemStack getDMGStack(final int damageValue, final int size) {
		return new ItemStack(this.item, size, damageValue);
	}

	public String getInternalName() {
		return this.internalName;
	}

	public Item getItem() {
		return this.item;
	}

	public ItemStack getStack() {
		return this.getStack(1);
	}

	public ItemStack getStack(final int size) {
		return new ItemStack(this.item, size);
	}
}
