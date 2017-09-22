package p455w0rd.wct.client.me;

import javax.annotation.Nonnull;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.ReadableNumberConverter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class WCTRenderItem extends RenderItem {
	private boolean useLg = false;
	ItemStack itemStack;
	private IAEItemStack aeStack = null;

	public WCTRenderItem(TextureManager p_i46552_1_, ModelManager p_i46552_2_, ItemColors p_i46552_3_, boolean useLg) {
		super(p_i46552_1_, p_i46552_2_, p_i46552_3_);
		this.useLg = useLg;
	}

	@Override
	public void renderItemOverlayIntoGUI(FontRenderer fontRenderer, ItemStack is, int par4, int par5, String par6Str) {
		if (is != null) {
			float scaleFactor = useLg ? 1.0F : 0.5F;
			float inverseScaleFactor = 1.0F / scaleFactor;
			int offset = useLg ? 0 : -1;
			String stackSize = "";

			boolean unicodeFlag = fontRenderer.getUnicodeFlag();
			fontRenderer.setUnicodeFlag(false);
			if (is.getItem().showDurabilityBar(is)) {
				double health = is.getItem().getDurabilityForDisplay(is);
				int j = (int) Math.round(13.0D - health * 13.0D);
				int i = (int) Math.round(255.0D - health * 255.0D);

				GlStateManager.disableDepth();
				GlStateManager.disableTexture2D();

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder vertexbuffer = tessellator.getBuffer();
				draw(vertexbuffer, par4 + 2, par5 + 13, 13, 2, 0, 0, 0, 255);
				draw(vertexbuffer, par4 + 2, par5 + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
				draw(vertexbuffer, par4 + 2, par5 + 13, j, 1, 255 - i, i, 0, 255);

				GlStateManager.enableTexture2D();

				GlStateManager.enableDepth();
			}
			long amount = 0L;
			if (aeStack != null) {
				amount = aeStack.getStackSize();
				if (amount != 0L) {
					scaleFactor = 0.5F;
					inverseScaleFactor = 1.0F / scaleFactor;
					offset = -1;
					stackSize = getToBeRenderedStackSize(amount);
				}
			}
			else {
				amount = is.getCount();
				if (amount != 0) {
					scaleFactor = 1.0F;
					inverseScaleFactor = 1.0F / scaleFactor;
					offset = 0;
					stackSize = getToBeRenderedStackSize(amount);
				}
			}
			GlStateManager.disableLighting();
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
			GlStateManager.disableDepth();
			GlStateManager.pushMatrix();
			GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);

			int X = (int) ((par4 + offset + 16.0F - fontRenderer.getStringWidth(stackSize) * scaleFactor) * inverseScaleFactor);

			int Y = (int) ((par5 + offset + 16.0F - 7.0F * scaleFactor) * inverseScaleFactor);
			if (amount > 1L) {
				fontRenderer.drawStringWithShadow(stackSize, X, Y, 16777215);
			}
			GlStateManager.popMatrix();
			GlStateManager.enableDepth();
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.enableLighting();

			fontRenderer.setUnicodeFlag(unicodeFlag);
		}
	}

	private void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
		renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		renderer.pos(x + 0, y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos(x + 0, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos(x + width, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos(x + width, y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();
	}

	private String getToBeRenderedStackSize(long originalSize) {
		if (useLg) {
			return ReadableNumberConverter.INSTANCE.toSlimReadableForm(originalSize);
		}
		return ReadableNumberConverter.INSTANCE.toWideReadableForm(originalSize);
	}

	public IAEItemStack getAeStack() {
		return aeStack;
	}

	public void setAeStack(@Nonnull final IAEItemStack aeStack) {
		this.aeStack = aeStack;
	}
}