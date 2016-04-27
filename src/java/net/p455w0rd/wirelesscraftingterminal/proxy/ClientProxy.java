package net.p455w0rd.wirelesscraftingterminal.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.p455w0rd.wirelesscraftingterminal.common.WirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.handlers.KeybindHandler;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationRegistry;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationType;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.transformer.MissingCoreMod;

public class ClientProxy extends CommonProxy {

	public ClientProxy() {
		MinecraftForge.EVENT_BUS.register(this);
		KeybindHandler.registerKeybinds();
		//MinecraftForge.EVENT_BUS.register(new KeybindHandler());
		FMLCommonHandler.instance().bus().register(new KeybindHandler());
	}

	@Override
	public void removeItemsFromNEI() {
		if (IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI)) {
			codechicken.nei.api.API.hideItem(new ItemStack(ItemEnum.WCT_BOOSTER_BG_ICON.getItem(), 1));
			codechicken.nei.api.API.hideItem(new ItemStack(ItemEnum.WCT_DUMMY_ICON.getItem(), 1));
			return;
		}
	}

	public void registerRenderers() {
		// No renderers...yet =]
	}

	@Override
	public void missingCoreMod() {
		throw new MissingCoreMod();
	}

}
