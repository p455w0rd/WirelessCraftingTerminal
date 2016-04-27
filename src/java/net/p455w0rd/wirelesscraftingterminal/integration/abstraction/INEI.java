package net.p455w0rd.wirelesscraftingterminal.integration.abstraction;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Slot;

public interface INEI {

	void drawSlot(Slot s);

	RenderItem setItemRender(RenderItem renderItem);
}