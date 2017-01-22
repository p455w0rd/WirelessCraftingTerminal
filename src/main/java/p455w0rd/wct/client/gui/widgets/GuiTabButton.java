package p455w0rd.wct.client.gui.widgets;

import org.lwjgl.opengl.*;

import appeng.client.gui.widgets.ITooltip;
import appeng.core.AppEng;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiTabButton extends GuiButton implements ITooltip {
	private final RenderItem itemRenderer;
	private final String message;
	private int hideEdge = 0;
	private int myIcon = -1;
	private ItemStack myItem;

	public GuiTabButton(final int x, final int y, final int ico, final String message, final RenderItem ir) {
		super(0, 0, 16, "");

		xPosition = x;
		yPosition = y;
		width = 22;
		height = 22;
		myIcon = ico;
		this.message = message;
		itemRenderer = ir;
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
	public GuiTabButton(final int x, final int y, final ItemStack ico, final String message, final RenderItem ir) {
		super(0, 0, 16, "");
		xPosition = x;
		yPosition = y;
		width = 22;
		height = 22;
		myItem = ico;
		this.message = message;
		itemRenderer = ir;
	}

	@Override
	public void drawButton(final Minecraft minecraft, final int x, final int y) {
		if (visible) {
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			minecraft.renderEngine.bindTexture(new ResourceLocation(AppEng.MOD_ID, "textures/guis/states.png"));
			hovered = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;

			int uv_x = (hideEdge > 0 ? 11 : 13);

			final int offsetX = hideEdge > 0 ? 1 : 0;

			//this.drawTexturedModalRect( this.xPosition, this.yPosition, uv_x * 16, 0, 25, 22 );

			if (myIcon >= 0) {
				final int uv_y = (int) Math.floor(myIcon / 16);
				uv_x = myIcon - uv_y * 16;

				this.drawTexturedModalRect(offsetX + xPosition + 3, yPosition + 6, uv_x * 16, uv_y * 16, 16, 16);
			}

			mouseDragged(minecraft, x, y);

			if (myItem != null) {
				zLevel = 100.0F;
				itemRenderer.zLevel = 100.0F;

				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				RenderHelper.enableGUIStandardItemLighting();
				//this.itemRenderer.renderItemAndEffectIntoGUI( fontrenderer, minecraft.renderEngine, this.myItem, offsetX + this.xPosition + 3, this.yPosition + 6 );
				itemRenderer.renderItemIntoGUI(myItem, offsetX + xPosition + 3, yPosition + 6);
				GL11.glDisable(GL11.GL_LIGHTING);

				itemRenderer.zLevel = 0.0F;
				zLevel = 0.0F;
			}
		}
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public int xPos() {
		return xPosition;
	}

	@Override
	public int yPos() {
		return yPosition;
	}

	@Override
	public int getWidth() {
		return 22;
	}

	@Override
	public int getHeight() {
		return 22;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public int getHideEdge() {
		return hideEdge;
	}

	public void setHideEdge(final int hideEdge) {
		this.hideEdge = hideEdge;
	}
}
