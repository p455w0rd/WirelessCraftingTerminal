package net.p455w0rd.wirelesscraftingterminal.integration.modules;

import javax.annotation.Nonnull;

import ic2.api.item.ElectricItem;
import net.minecraft.item.ItemStack;

/**
 * @author p455w0rd
 *
 */
public class IC2 {

	public static void drawPowerFromChestItem(@Nonnull ItemStack chestStack, double transferLimit) {
		ElectricItem.manager.discharge(chestStack, transferLimit, 4, true, false, false);
	}

}
