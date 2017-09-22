package p455w0rd.wct.client.me;

import appeng.api.storage.data.IAEItemStack;
import net.minecraft.item.ItemStack;

public class InternalSlotME {

	private final int offset;
	private final int xPos;
	private final int yPos;
	private final ItemRepo repo;

	public InternalSlotME(final ItemRepo def, final int offset, final int displayX, final int displayY) {
		repo = def;
		this.offset = offset;
		xPos = displayX;
		yPos = displayY;
	}

	ItemStack getStack() {
		return repo.getItem(offset);
	}

	IAEItemStack getAEStack() {
		return repo.getReferenceItem(offset);
	}

	boolean hasPower() {
		return repo.hasPower();
	}

	int getxPosition() {
		return xPos;
	}

	int getyPosition() {
		return yPos;
	}
}
