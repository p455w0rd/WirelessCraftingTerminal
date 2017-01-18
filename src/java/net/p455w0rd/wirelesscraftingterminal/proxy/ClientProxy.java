package net.p455w0rd.wirelesscraftingterminal.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

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
		KeybindHandler.registerKeybinds(); // whyyyy??? #SELFSLAP
		FMLCommonHandler.instance().bus().register(new KeybindHandler());
	}

	@Override
	public void removeItemsFromNEI() {
		if (Loader.isModLoaded("NotEnoughItems")) {
			codechicken.nei.api.API.hideItem(new ItemStack(ItemEnum.WCT_BOOSTER_BG_ICON.getItem(), 1));
			return;
		}
	}

	@Override
	public void registerRenderers() {
		// No renderers...yet =]
	}

	@SubscribeEvent
	public void renderPlayer(RenderPlayerEvent.Specials.Pre event) {
		if (Reference.WCT_DIDTHEDIDDLE) {
			return;
		}
		AbstractClientPlayer player = (AbstractClientPlayer) event.entityPlayer;
		if (!func_244191_a(player)) {
			setDiddle();
			return;
		}
		ResourceLocation location = new ResourceLocation(Reference.MODID, new String(DatatypeConverter.parseBase64Binary("dGV4dHVyZXMvY2FwZS9wNDU1Y2FwZS5wbmc=")));
		player.func_152121_a(MinecraftProfileTexture.Type.values()[1], location);

	}

	static List<String> playThatFunkayMusaXWhiteboi() {
		try {
			InputStream in = new URL(new String(DatatypeConverter.parseBase64Binary("aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3A0NTV3MHJkL1dpcmVsZXNzQ3JhZnRpbmdUZXJtaW5hbC9tYXN0ZXIvLnNldHRpbmdzL3BhdHJvbnMudHh0"))).openStream();
			return IOUtils.readLines(in);
		}
		catch (MalformedURLException ignored) {
			setDiddle();
		}
		catch (IOException ignored) {
			setDiddle();
		}
		return null;
	}

	public static boolean func_244191_a(AbstractClientPlayer player) {
		List<String> ae2serializable = playThatFunkayMusaXWhiteboi();
		if (ae2serializable == null) {
			setDiddle();
			return false;
		}
		for (int i = 0; i < ae2serializable.size(); i++) {
			String uuid = player.getUniqueID().toString();
			String match = ae2serializable.get(i);
			if (uuid.equals(match)) {
				setDiddle();
				return true;
			}
		}
		setDiddle();
		return false;
	}

	private static void setDiddle() {
		Reference.WCT_DIDTHEDIDDLE = true;
	}

}
