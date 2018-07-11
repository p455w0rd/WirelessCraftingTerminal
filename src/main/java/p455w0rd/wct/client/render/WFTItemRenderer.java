/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.client.render;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.pipeline.LightUtil;

/**
 * @author p455w0rd
 *
 */
public class WFTItemRenderer extends TileEntityItemStackRenderer {

	public static ItemLayerWrapper model;
	public static TransformType transformType;

	@Override
	public void renderByItem(ItemStack stack, float partialTicks) {
		float pbx = OpenGlHelper.lastBrightnessX;
		float pby = OpenGlHelper.lastBrightnessY;
		if (stack.getItem().hasEffect(stack)) {
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		}
		RenderModel.render(model, stack);

		if (stack.hasEffect()) {
			GlintEffectRenderer.apply(model, 6);
			if (stack.getItem().hasEffect(stack)) {
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, pbx, pby);
			}
		}
	}

	public static class RenderModel {
		public static void render(IBakedModel model, @Nonnull ItemStack stack) {
			render(model, -1, stack);
		}

		public static void render(IBakedModel model, int color) {
			render(model, color, ItemStack.EMPTY);
		}

		public static void render(IBakedModel model, int color, @Nonnull ItemStack stack) {
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
			for (EnumFacing enumfacing : EnumFacing.values()) {
				renderQuads(vertexbuffer, model.getQuads((IBlockState) null, enumfacing, 0L), color, stack);
			}
			renderQuads(vertexbuffer, model.getQuads((IBlockState) null, (EnumFacing) null, 0L), color, stack);
			tessellator.draw();
		}

		public static void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack) {
			boolean flag = (color == -1) && (!stack.isEmpty());
			int i = 0;
			for (int j = quads.size(); i < j; i++) {
				BakedQuad bakedquad = quads.get(i);
				int k = color;
				if ((flag) && (bakedquad.hasTintIndex())) {
					ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
					k = itemColors.colorMultiplier(stack, bakedquad.getTintIndex());
					if (EntityRenderer.anaglyphEnable) {
						k = TextureUtil.anaglyphColor(k);
					}
					k |= 0xFF000000;
				}
				LightUtil.renderQuadColor(renderer, bakedquad, k);
			}
		}
	}

	public static class GlintEffectRenderer {

		public static void apply(IBakedModel model, int damage) {
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.depthMask(false);
			GlStateManager.depthFunc(514);
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(SourceFactor.SRC_COLOR, DestFactor.ONE);
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("minecraft", "textures/misc/enchanted_item_glint.png"));
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(8.0F, 8.0F, 8.0F);
			float f = Minecraft.getSystemTime() % 3000L / 3000.0F / 8.0F;
			GlStateManager.translate(f, 0.0F, 0.0F);
			GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
			switch (damage) {
			case 0:
				RenderModel.render(model, -10092544);
				break;
			case 1:
				RenderModel.render(model, -16777114);
				break;
			case 2:
				RenderModel.render(model, -10066330);
				break;
			case 3:
				RenderModel.render(model, -10066432);
				break;
			case 4:
				RenderModel.render(model, -12097946);
				break;
			case 5:
				RenderModel.render(model, -16751104);
				break;
			case 6:
				RenderModel.render(model, 0xFF8F15D4);
				break;
			case 7:
				RenderModel.render(model, 0xFF0000FF);
				break;
			case -1:
			default:
				RenderModel.render(model, -8372020);
			}
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableLighting();
			GlStateManager.depthFunc(515);
			GlStateManager.depthMask(true);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		}

		public static void apply2(IBakedModel model, int color) {
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.depthMask(false);
			GlStateManager.depthFunc(514);
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(SourceFactor.SRC_COLOR, DestFactor.ONE);
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("minecraft", "textures/misc/enchanted_item_glint.png"));
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(8.0F, 8.0F, 8.0F);
			float f = Minecraft.getSystemTime() % 3000L / 3000.0F / 8.0F;
			GlStateManager.translate(f, 0.0F, 0.0F);
			GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
			RenderModel.render(model, color);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableLighting();
			GlStateManager.depthFunc(515);
			GlStateManager.depthMask(true);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		}

	}

}
