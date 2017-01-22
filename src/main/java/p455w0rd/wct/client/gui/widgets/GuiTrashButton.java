package p455w0rd.wct.client.gui.widgets;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiTrashButton extends GuiButton implements ITooltip {

	public GuiTrashButton(int x, int y) {
		super(x, y, 8, "");
		xPosition = x;
		yPosition = y;
		width = 8;
		height = 8;
	}

	@Override
	public void drawButton(final Minecraft mc, final int par2, final int par3) {
		if (visible) {
			GL11.glPushMatrix();
			GL11.glTranslatef(xPosition, yPosition, 0.0F);
			GL11.glScalef(0.5f, 0.5f, 0.5f);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			mc.renderEngine.bindTexture(new ResourceLocation("appliedenergistics2", "textures/guis/states.png"));
			hovered = par2 >= xPosition && par3 >= yPosition && par2 < xPosition + width && par3 < yPosition + height;
			this.drawTexturedModalRect(0, 0, 256 - 16, 256 - 16, 16, 16);
			this.drawTexturedModalRect(0, 0, 6 * 16, 0, 16, 16);
			mouseDragged(mc, par2, par3);
			GL11.glPopMatrix();
		}
	}

	@Override
	public String getMessage() {
		return TextFormatting.WHITE + "" + I18n.format("gui.emptytrash") + "\n" + TextFormatting.GRAY + "" + I18n.format("gui.emptytrash.desc");
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
