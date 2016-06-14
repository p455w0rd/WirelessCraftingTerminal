package net.p455w0rd.wirelesscraftingterminal.common.container;

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
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.WCTIActionHost;
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.WCTPlayerSource;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotInaccessible;


public class ContainerCraftAmount extends WCTBaseContainer
{

	private final Slot craftingItem;
	private IAEItemStack itemToCreate;

	public ContainerCraftAmount( final InventoryPlayer ip, final ITerminalHost te )
	{
		super( ip, te );

		this.craftingItem = new SlotInaccessible( new AppEngInternalInventory( null, 1 ), 0, 34, 53 );
		this.addSlotToContainer( this.getCraftingItem() );
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		this.verifyPermissions( SecurityPermissions.CRAFT, false );
	}

	public IGrid getGrid()
	{
		return this.obj2.getTargetGrid();
	}

	public World getWorld()
	{
		return this.getPlayerInv().player.worldObj;
	}

	public BaseActionSource getActionSrc()
	{
		return new WCTPlayerSource( this.getPlayerInv().player, (WCTIActionHost) this.getTarget() );
	}

	public Slot getCraftingItem()
	{
		return this.craftingItem;
	}

	public IAEItemStack getItemToCraft()
	{
		return this.itemToCreate;
	}

	public void setItemToCraft( @Nonnull final IAEItemStack itemToCreate )
	{
		this.itemToCreate = itemToCreate;
	}
}
