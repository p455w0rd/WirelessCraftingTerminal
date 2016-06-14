package net.p455w0rd.wirelesscraftingterminal.client.gui.widgets;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.client.gui.widgets.ITooltip;
import appeng.client.texture.ExtraBlockTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;


public class GuiTabButton extends GuiButton implements ITooltip
{
	private final RenderItem itemRenderer;
	private final String message;
	private int hideEdge = 0;
	private int myIcon = -1;
	private ItemStack myItem;

	public GuiTabButton( final int x, final int y, final int ico, final String message, final RenderItem ir )
	{
		super( 0, 0, 16, "" );

		this.xPosition = x;
		this.yPosition = y;
		this.width = 22;
		this.height = 22;
		this.myIcon = ico;
		this.message = message;
		this.itemRenderer = ir;
	}

	/**
	 * Using itemstack as an icon
	 *
	 * @param x x pos of button
	 * @param y y pos of button
	 * @param ico used icon
	 * @param message mouse over message
	 * @param ir renderer
	 */
	public GuiTabButton( final int x, final int y, final ItemStack ico, final String message, final RenderItem ir )
	{
		super( 0, 0, 16, "" );
		this.xPosition = x;
		this.yPosition = y;
		this.width = 22;
		this.height = 22;
		this.myItem = ico;
		this.message = message;
		this.itemRenderer = ir;
	}

	@Override
	public void drawButton( final Minecraft minecraft, final int x, final int y )
	{
		if( this.visible )
		{
			GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
			minecraft.renderEngine.bindTexture( ExtraBlockTextures.GuiTexture( "guis/states.png" ) );
			this.field_146123_n = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;

			int uv_x = ( this.hideEdge > 0 ? 11 : 13 );

			final int offsetX = this.hideEdge > 0 ? 1 : 0;

			//this.drawTexturedModalRect( this.xPosition, this.yPosition, uv_x * 16, 0, 25, 22 );
			
			if( this.myIcon >= 0 )
			{
				final int uv_y = (int) Math.floor( this.myIcon / 16 );
				uv_x = this.myIcon - uv_y * 16;

				this.drawTexturedModalRect( offsetX + this.xPosition + 3, this.yPosition + 6, uv_x * 16, uv_y * 16, 16, 16 );
			}
			
			this.mouseDragged( minecraft, x, y );

			if( this.myItem != null )
			{
				this.zLevel = 100.0F;
				this.itemRenderer.zLevel = 100.0F;

				GL11.glEnable( GL11.GL_LIGHTING );
				GL11.glEnable( GL12.GL_RESCALE_NORMAL );
				RenderHelper.enableGUIStandardItemLighting();
				final FontRenderer fontrenderer = minecraft.fontRenderer;
				//this.itemRenderer.renderItemAndEffectIntoGUI( fontrenderer, minecraft.renderEngine, this.myItem, offsetX + this.xPosition + 3, this.yPosition + 6 );
				this.itemRenderer.renderItemIntoGUI( fontrenderer, minecraft.renderEngine, this.myItem, offsetX + this.xPosition + 3, this.yPosition + 6, false );
				GL11.glDisable( GL11.GL_LIGHTING );

				this.itemRenderer.zLevel = 0.0F;
				this.zLevel = 0.0F;
			}
		}
	}

	@Override
	public String getMessage()
	{
		return this.message;
	}

	@Override
	public int xPos()
	{
		return this.xPosition;
	}

	@Override
	public int yPos()
	{
		return this.yPosition;
	}

	@Override
	public int getWidth()
	{
		return 22;
	}

	@Override
	public int getHeight()
	{
		return 22;
	}

	@Override
	public boolean isVisible()
	{
		return this.visible;
	}

	public int getHideEdge()
	{
		return this.hideEdge;
	}

	public void setHideEdge( final int hideEdge )
	{
		this.hideEdge = hideEdge;
	}
}
