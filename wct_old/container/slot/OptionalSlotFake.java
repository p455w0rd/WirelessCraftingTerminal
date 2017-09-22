package p455w0rd.wct.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class OptionalSlotFake extends SlotFake {

	private final int srcX;
	private final int srcY;
	private final int groupNum;
	private final IOptionalSlotHost host;
	private boolean renderDisabled = true;

	public OptionalSlotFake(final IItemHandler inv, final IOptionalSlotHost containerBus, final int idx, final int x, final int y, final int offX, final int offY, final int groupNum) {
		super(inv, idx, x + offX * 18, y + offY * 18);
		srcX = x;
		srcY = y;
		this.groupNum = groupNum;
		host = containerBus;
	}

	@Override
	public ItemStack getStack() {
		if (!isEnabled()) {
			if (getDisplayStack() != null) {
				clearStack();
			}
		}

		return super.getStack();
	}

	@Override
	public boolean isEnabled() {
		if (host == null) {
			return false;
		}

		return host.isSlotEnabled(groupNum);
	}

	public boolean renderDisabled() {
		return isRenderDisabled();
	}

	private boolean isRenderDisabled() {
		return renderDisabled;
	}

	public void setRenderDisabled(final boolean renderDisabled) {
		this.renderDisabled = renderDisabled;
	}

	public int getSourceX() {
		return srcX;
	}

	public int getSourceY() {
		return srcY;
	}
}
