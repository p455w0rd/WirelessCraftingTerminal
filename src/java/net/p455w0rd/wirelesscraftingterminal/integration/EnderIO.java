package net.p455w0rd.wirelesscraftingterminal.integration;

import java.lang.invoke.MethodHandle;
import java.util.Iterator;
import java.util.Map;

import cpw.mods.fml.common.Loader;
import crazypants.enderio.enchantment.EnchantmentSoulBound;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import p455w0rdslib.util.ReflectionUtils;

/**
 * @author p455w0rd
 *
 */
public class EnderIO {

	public static boolean isLoaded() {
		return Loader.isModLoaded("EnderIO");
	}

	@SuppressWarnings("rawtypes")
	public static boolean isSoulBound(ItemStack stack) {
		if (isLoaded() && crazypants.enderio.config.Config.enchantmentSoulBoundEnabled) {
			try {
				MethodHandle GET_SOULBOUND = ReflectionUtils.findFieldGetter(crazypants.enderio.enchantment.Enchantments.class, "soulBound");
				EnchantmentSoulBound soulbound = (EnchantmentSoulBound) GET_SOULBOUND.invoke(crazypants.enderio.enchantment.Enchantments.getInstance());
				MethodHandle GET_ID = ReflectionUtils.findFieldGetter(crazypants.enderio.enchantment.EnchantmentSoulBound.class, "id");
				int soulboundId = (int) GET_ID.invoke(soulbound);
				//int soulboundId = crazypants.enderio.config.Config.enchantmentSoulBoundId;
				Map<Short, Short> enchants = EnchantmentHelper.getEnchantments(stack);
				if (enchants != null) {
					Iterator i = enchants.keySet().iterator();
					while (i.hasNext()) {
						int enchId = (int) i.next();
						if (soulboundId != enchId) {
							continue;
						}
						return true;
					}
				}
			}
			catch (Throwable e) {
			}
		}
		return false;
	}

}
