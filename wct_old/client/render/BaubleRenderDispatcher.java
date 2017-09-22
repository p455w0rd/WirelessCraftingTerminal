package p455w0rd.wct.client.render;

import java.util.Map;
import java.util.WeakHashMap;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.api.IBaubleItem;
import p455w0rd.wct.api.IBaubleRender;

/**
 * from EnderIO
 *
 */
public class BaubleRenderDispatcher implements LayerRenderer<AbstractClientPlayer> {

	public final static BaubleRenderDispatcher instance = new BaubleRenderDispatcher(null);

	private final RenderPlayer renderPlayer;

	public BaubleRenderDispatcher(RenderPlayer renderPlayer) {
		this.renderPlayer = renderPlayer;
	}

	private static final Map<RenderPlayer, Object> REGISTRY = new WeakHashMap<RenderPlayer, Object>();

	public static final Map<RenderPlayer, Object> getRegistry() {
		return REGISTRY;
	}

	@Override
	public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
		IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(entitylivingbaseIn);
		if (baubles != null) {
			for (int i = 0; i < baubles.getSlots(); i++) {
				ItemStack piece = baubles.getStackInSlot(i);
				if (piece != null && piece.getItem() instanceof IBaubleItem) {
					IBaubleRender render = ((IBaubleItem) piece.getItem()).getRender();
					if (render != null) {
						render.doRenderLayer(renderPlayer, piece, entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale);
					}
				}
			}
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return true;
	}

}