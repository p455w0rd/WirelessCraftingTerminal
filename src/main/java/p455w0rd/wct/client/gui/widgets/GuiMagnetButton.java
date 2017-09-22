package p455w0rd.wct.client.gui.widgets;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import p455w0rd.wct.init.ModGlobals;

/**
 * @author p455w0rd
 *
 */
public class GuiMagnetButton extends GuiButton implements ITooltip {

	public GuiMagnetButton(int xIn, int yIn) {
		super(xIn, yIn, 8, "");
		x = xIn;
		y = yIn;
		width = 8;
		height = 8;
	}

	@Override
	public void drawButton(final Minecraft mc, final int par2, final int par3, float partial) {
		if (visible) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0.0F);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.scale(0.025F, 0.025F, 0.025F);
			//GL11.glScalef(0.5f, 0.5f, 0.5f);
			mc.getTextureManager().bindTexture(new ResourceLocation(ModGlobals.MODID, "textures/gui/magnet_button.png"));
			hovered = par2 >= x && par3 >= y && par2 < x + width && par3 < y + height;
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
		return x;
	}

	@Override
	public int yPos() {
		return y;
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