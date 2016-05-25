package net.p455w0rd.wirelesscraftingterminal.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class VersionCheckHandler {
	
	int timer = 150;
	
	public VersionCheckHandler() {
		FMLCommonHandler.instance().bus().register(this);
	}
	
	@SubscribeEvent
	public void tickStart(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.START) {
			return;
		}
		if (Reference.WCT_DOVERSIONCHECK && !Reference.WCT_HASCHECKEDVERSION) {
			if (timer > 0) {
				timer--;
				return;
			}
			checkVersion(event.player);
		}
		else {
			FMLCommonHandler.instance().bus().unregister(this);
		}
	}

	private void checkVersion(EntityPlayer player) {
		if (!fetchVersion().equals(Reference.VERSION) && !Reference.WCT_HASCHECKEDVERSION) {
			Reference.WCT_HASCHECKEDVERSION = true;
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + "-=-=-=-=-=[" + EnumChatFormatting.WHITE + "Wireless Crafting Terminal" + EnumChatFormatting.BLUE + "]=-=-=-=-=-"));
			player.addChatMessage(new ChatComponentText(LocaleHandler.NewVersionAvailable.getLocal()));
			IChatComponent component = IChatComponent.Serializer.func_150699_a(LocaleHandler.ClickString.getLocal());
			player.addChatComponentMessage(component);
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"));
		}
		FMLCommonHandler.instance().bus().unregister(this);
	}

	private String fetchVersion() {
		try {
			InputStream in = new URL(new String("https://raw.githubusercontent.com/p455w0rd/WirelessCraftingTerminal/master/latestversionrv3.txt")).openStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			return r.readLine();
		}
		catch (MalformedURLException ignored) {
			return Reference.VERSION;
		}
		catch (IOException ignored) {
			return Reference.VERSION;
		}
	}

}
