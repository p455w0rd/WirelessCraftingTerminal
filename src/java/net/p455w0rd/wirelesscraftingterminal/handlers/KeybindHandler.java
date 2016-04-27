package net.p455w0rd.wirelesscraftingterminal.handlers;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ContainerPlayer;
import net.p455w0rd.wirelesscraftingterminal.api.WCTApi;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.WirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketOpenWirelessTerm;

@SideOnly(Side.CLIENT)
public class KeybindHandler {

	public static KeyBinding openTerminal;
	public static KeyBinding openMagnetFilter;
	public static KeyBinding disableMagnet;

	public static void registerKeybinds() {
		openTerminal = new KeyBinding("key.OpenTerminal", Keyboard.CHAR_NONE, "key.categories.ae2wct");
		openMagnetFilter = new KeyBinding("key.OpenMagnetFilter", Keyboard.CHAR_NONE, "key.categories.ae2wct");
		disableMagnet = new KeyBinding("key.DisableMagnet", Keyboard.CHAR_NONE, "key.categories.ae2wct");
		ClientRegistry.registerKeyBinding(openTerminal);
		ClientRegistry.registerKeyBinding(openMagnetFilter);
		ClientRegistry.registerKeyBinding(disableMagnet);
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		//if (event.phase == Phase.END) {
			EntityClientPlayerMP p = (EntityClientPlayerMP) Minecraft.getMinecraft().thePlayer;
			if (openTerminal.isPressed()) {
				//if (p.openContainer != null && p.openContainer instanceof ContainerPlayer) {
				//WCTApi.instance().interact().openWirelessCraftingTerminalGui(p);
				if (!FMLClientHandler.instance().isGUIOpen(GuiWirelessCraftingTerminal.class)) {
					NetworkHandler.instance.sendToServer( new PacketOpenWirelessTerm() );
				}
				//}
			}
		//}
	}

}
