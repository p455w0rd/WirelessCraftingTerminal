package net.p455w0rd.wirelesscraftingterminal.transformer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;

import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


@SideOnly( Side.CLIENT )
public final class MissingCoreMod extends CustomModLoadingErrorDisplayException
{
	private static final int SHADOW_WHITE = 0xeeeeee;
	private static final int COLOR_WHITE = 0xffffff;
	private static final long serialVersionUID = -966774766922821652L;
	private static final int SCREEN_OFFSET = 15;

	private boolean deobf = false;

	@Override
	public void initGui( final GuiErrorScreen errorScreen, final FontRenderer fontRenderer )
	{
		final Class<?> clz = errorScreen.getClass();
		try
		{
			clz.getField( "mc" );
			this.deobf = true;
		}
		catch( final Throwable ignored )
		{

		}
	}

	@Override
	public void drawScreen( final GuiErrorScreen errorScreen, final FontRenderer fontRenderer, final int mouseRelX, final int mouseRelY, final float tickTime )
	{
		int offset = 10;
		this.drawCenteredString( fontRenderer, "Sorry, couldn't load WCT properly.", errorScreen.width / 2, offset, COLOR_WHITE );

		offset += SCREEN_OFFSET;
		this.drawCenteredString( fontRenderer, "Please make sure that WCT is installed into your mods folder.", errorScreen.width / 2, offset, SHADOW_WHITE );

		offset += 2 * SCREEN_OFFSET;

		if( this.deobf )
		{
			offset += SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "In a developer environment add the following too your args,", errorScreen.width / 2, offset, COLOR_WHITE );

			offset += SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "-Dfml.coreMods.load=net.p455w0rd.wirelesscraftingterminal.transformer.WCTCore", errorScreen.width / 2, offset, SHADOW_WHITE );
		}
		else
		{
			this.drawCenteredString( fontRenderer, "You're launcher may refer to this by different names,", errorScreen.width / 2, offset, COLOR_WHITE );

			offset += SCREEN_OFFSET + 5;

			this.drawCenteredString( fontRenderer, "MultiMC calls this tab \"Loader Mods\"", errorScreen.width / 2, offset, SHADOW_WHITE );

			offset += SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "Magic Launcher calls this tab \"External Mods\"", errorScreen.width / 2, offset, SHADOW_WHITE );

			offset += SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "Most other launchers refer to this tab as just \"Mods\"", errorScreen.width / 2, offset, SHADOW_WHITE );

			offset += 2 * SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "Also make sure that the WCT file is a .jar, and not a .zip", errorScreen.width / 2, offset, COLOR_WHITE );
		}
	}

	private void drawCenteredString( final FontRenderer fontRenderer, final String string, final int x, final int y, final int colour )
	{
		final String reEncoded = string.replaceAll( "\\P{InBasic_Latin}", "" );
		final int reEncodedWidth = fontRenderer.getStringWidth( reEncoded );
		final int centeredX = x - reEncodedWidth / 2;

		fontRenderer.drawStringWithShadow( string, centeredX, y, colour );
	}
}