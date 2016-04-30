package net.p455w0rd.wirelesscraftingterminal.client.gui.widgets;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;

public class GuiTrashButton extends GuiButton implements ITooltip {
	
	public GuiTrashButton(int x, int y) {
		super(x, y, 8, "");
		this.xPosition = x;
		this.yPosition = y;
		this.width = 8;
		this.height = 8;
	}
	
	@Override
	public void drawButton( final Minecraft mc, final int par2, final int par3 )
	{
		if( this.visible )
		{
			GL11.glPushMatrix();
			GL11.glTranslatef( this.xPosition, this.yPosition, 0.0F );
			GL11.glScalef( 0.5f, 0.5f, 0.5f );
			GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
			mc.renderEngine.bindTexture( new ResourceLocation( "appliedenergistics2", "textures/guis/states.png" ) );
			this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
			this.drawTexturedModalRect( 0, 0, 256 - 16, 256 - 16, 16, 16 );
			this.drawTexturedModalRect( 0, 0, 6 * 16, 0, 16, 16 );
			this.mouseDragged(mc, par2, par3 );
			GL11.glPopMatrix();
		}
	}

	@Override
	public String getMessage() {
		return EnumChatFormatting.WHITE + "" + LocaleHandler.EmptyTrash.getLocal() + "\n" + EnumChatFormatting.GRAY + "" + LocaleHandler.EmptyTrashDesc.getLocal();
	}

	@Override
	public int xPos() {
		return this.xPosition;
	}

	@Override
	public int yPos() {
		return this.yPosition;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}

}
