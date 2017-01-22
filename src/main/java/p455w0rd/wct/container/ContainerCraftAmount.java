package p455w0rd.wct.container;

import javax.annotation.Nonnull;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.AppEngInternalInventory;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;
import p455w0rd.wct.api.networking.security.WCTIActionHost;
import p455w0rd.wct.api.networking.security.WCTPlayerSource;
import p455w0rd.wct.container.slot.SlotInaccessible;

public class ContainerCraftAmount extends WCTBaseContainer {

	private final Slot craftingItem;
	private IAEItemStack itemToCreate;

	public ContainerCraftAmount(final InventoryPlayer ip, final ITerminalHost te) {
		super(ip, te);

		craftingItem = new SlotInaccessible(new AppEngInternalInventory(null, 1), 0, 34, 53);
		addSlotToContainer(getCraftingItem());
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		verifyPermissions(SecurityPermissions.CRAFT, false);
	}

	public IGrid getGrid() {
		final WCTIActionHost h = ((WCTIActionHost) getTarget());
		return h.getActionableNode().getGrid();
		//return obj2.getTargetGrid();
	}

	public World getWorld() {
		return getPlayerInv().player.worldObj;
	}

	public BaseActionSource getActionSrc() {
		return new WCTPlayerSource(getPlayerInv().player, (WCTIActionHost) getTarget());
	}

	public Slot getCraftingItem() {
		return craftingItem;
	}

	public IAEItemStack getItemToCraft() {
		return itemToCreate;
	}

	public void setItemToCraft(@Nonnull final IAEItemStack itemToCreate) {
		this.itemToCreate = itemToCreate;
	}
}
