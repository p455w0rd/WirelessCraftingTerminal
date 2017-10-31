package p455w0rd.wct.integration;

import com.google.common.collect.Lists;

import p455w0rd.wct.init.ModGlobals.Mods;

/**
 * @author p455w0rd
 *
 */
public class ItemScroller {

	public static void blackListSlots() {
		if (Mods.ITEMSCROLLER.isLoaded()) {
			fi.dy.masa.itemscroller.config.Configs.SLOT_BLACKLIST.addAll(Lists.<String>newArrayList("p455w0rd.wct.client.me.SlotME", "p455w0rd.wct.container.slot.SlotBooster", "p455w0rd.wct.container.slot.SlotMagnet"));
		}
	}

}
