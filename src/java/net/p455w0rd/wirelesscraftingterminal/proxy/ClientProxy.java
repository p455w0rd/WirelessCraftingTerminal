package net.p455w0rd.wirelesscraftingterminal.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.p455w0rd.wirelesscraftingterminal.handlers.KeybindHandler;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

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
	
	@SubscribeEvent
    public void renderPlayer(RenderPlayerEvent.Specials.Pre event) {
        AbstractClientPlayer player = (AbstractClientPlayer) event.entityPlayer;
        if (!isPatron(player)) {
        	return;
        }
        ResourceLocation location = new ResourceLocation(Reference.MODID, "textures/cape/p455cape.png");
        player.func_152121_a(MinecraftProfileTexture.Type.CAPE, location);
    }
	
	static List<String> getPatronList() {
		try {
			InputStream in = new URL("https://raw.githubusercontent.com/p455w0rd/WirelessCraftingTerminal/master/.settings/patrons.txt").openStream();
			return IOUtils.readLines(in);
		}
		catch (MalformedURLException ignored) {
		}
		catch (IOException ignored) {
		}
		return null;
	}
	
	public static boolean isPatron(AbstractClientPlayer player) {
		List<String> patronList = getPatronList();
		if (patronList == null) {
			return false;
		}
		for (int i = 0; i < patronList.size(); i++) {
			String uuid = player.getUniqueID().toString();
			String match = patronList.get(i);
			if (uuid.equals(match)) {
				return true;
			}
		}
		return false;
	}

}
