package p455w0rd.wct.client.gui.widgets;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import p455w0rd.wct.Globals;

/**
 * @author p455w0rd
 *
 */
public class GuiMagnetButton extends GuiButton implements ITooltip {

	public GuiMagnetButton(int x, int y) {
		super(x, y, 8, "");
		xPosition = x;
		yPosition = y;
		width = 8;
		height = 8;
	}

	@Override
	public void drawButton(final Minecraft mc, final int par2, final int par3) {
		if (visible) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(xPosition, yPosition, 0.0F);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.scale(0.025F, 0.025F, 0.025F);
			//GL11.glScalef(0.5f, 0.5f, 0.5f);
			mc.getTextureManager().bindTexture(new ResourceLocation(Globals.MODID, "textures/gui/magnet_button.png"));
			hovered = par2 >= xPosition && par3 >= yPosition && par2 < xPosition + width && par3 < yPosition + height;
			drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			mouseDragged(mc, par2, par3);
			GL11.glPopMatrix();
		}
	}

	@Override
	public String getMessage() {
		return TextFormatting.WHITE + "" + I18n.format("gui.openmagnetgui") + "\n" + TextFormatting.GRAY + "" + I18n.format("gui.openmagnetgui.desc");
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
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

}