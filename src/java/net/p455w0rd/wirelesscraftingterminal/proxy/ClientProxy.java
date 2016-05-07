package net.p455w0rd.wirelesscraftingterminal.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.p455w0rd.wirelesscraftingterminal.handlers.KeybindHandler;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;

public class ClientProxy extends CommonProxy {

	public ClientProxy() {
		MinecraftForge.EVENT_BUS.register(this);
		KeybindHandler.registerKeybinds();
		FMLCommonHandler.instance().bus().register(new KeybindHandler());
	}

	@Override
	public void removeItemsFromNEI() {
		if (Loader.isModLoaded("NotEnoughItems")) {
			codechicken.nei.api.API.hideItem(new ItemStack(ItemEnum.WCT_BOOSTER_BG_ICON.getItem(), 1));
			return;
		}
	}

	public void registerRenderers() {
		// No renderers...yet =]
	}

}
