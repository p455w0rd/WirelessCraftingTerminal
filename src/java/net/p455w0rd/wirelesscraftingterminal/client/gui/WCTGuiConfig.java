package net.p455w0rd.wirelesscraftingterminal.client.gui;

import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.p455w0rd.wirelesscraftingterminal.handlers.ConfigHandler;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class WCTGuiConfig extends GuiConfig {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public WCTGuiConfig(GuiScreen guiScreen)
	{
		super(guiScreen,
				new ConfigElement(ConfigHandler.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
				Reference.MODID,
				false,
				false,
				GuiConfig.getAbridgedConfigPath(ConfigHandler.config.toString()));
	}
	
}