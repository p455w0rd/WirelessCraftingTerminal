package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import java.util.ArrayList;
import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class SlotCraftingTerm extends AppEngCraftingSlot {

	private final IInventory craftInv;
	private final IInventory pattern;

	private final BaseActionSource mySrc;
	private final IEnergySource energySrc;
	private final IStorageMonitorable storage;

	public SlotCraftingTerm(final EntityPlayer player, final BaseActionSource mySrc, final IEnergySource energySrc,
			final IStorageMonitorable storage, final IInventory cMatrix, final IInventory secondMatrix,
			final IInventory output, final int x, final int y, final IContainerCraftingPacket ccp) {
		super(player, cMatrix, output, 0, x, y);
		this.energySrc = energySrc;
		this.storage = storage;
		this.mySrc = mySrc;
		this.pattern = cMatrix;
		this.craftInv = secondMatrix;
	}

	public IInventory getCraftingMatrix() {
		return this.craftInv;
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}

	@Override
	public void onPickupFromSlot(final EntityPlayer p, final ItemStack is) {
	}

	public void doClick(final InventoryAction action, final EntityPlayer who) {
		if (this.getStack() == null) {
			return;
		}
		if (Platform.isClient()) {
			return;
		}

		final IMEMonitor<IAEItemStack> inv = this.storage.getItemInventory();
		final int howManyPerCraft = this.getStack().stackSize;
		int maxTimesToCraft = 0;

		InventoryAdaptor ia = null;
		if (action == InventoryAction.CRAFT_SHIFT) // craft into player
													// inventory...
		{
			ia = InventoryAdaptor.getAdaptor(who, null);
			maxTimesToCraft = (int) Math.floor((double) this.getStack().getMaxStackSize() / (double) howManyPerCraft);
		} else if (action == InventoryAction.CRAFT_STACK) // craft into hand,
															// full stack
		{
			ia = new AdaptorPlayerHand(who);
			maxTimesToCraft = (int) Math.floor((double) this.getStack().getMaxStackSize() / (double) howManyPerCraft);
		} else
		// pick up what was crafted...
		{
			ia = new AdaptorPlayerHand(who);
			maxTimesToCraft = 1;
		}

		maxTimesToCraft = this.capCraftingAttempts(maxTimesToCraft);

		if (ia == null) {
			return;
		}

		final ItemStack rs = Platform.cloneItemStack(this.getStack());
		if (rs == null) {
			return;
		}

		for (int x = 0; x < maxTimesToCraft; x++) {
			if (ia.simulateAdd(rs) == null) {
				final IItemList<IAEItemStack> all = inv.getStorageList();
				final ItemStack extra = ia.addItems(this.craftItem(who, rs, inv, all));
				if (extra != null) {
					final List<ItemStack> drops = new ArrayList<ItemStack>();
					drops.add(extra);
					Platform.spawnDrops(who.worldObj, (int) who.posX, (int) who.posY, (int) who.posZ, drops);
					return;
				}
			}
		}
	}

	private int capCraftingAttempts(final int maxTimesToCraft) {
		return maxTimesToCraft;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ItemStack craftItem(final EntityPlayer p, final ItemStack request, final IMEMonitor<IAEItemStack> inv,
			final IItemList all) {
		// update crafting matrix...
		ItemStack is = this.getStack();

		if (is != null && Platform.isSameItem(request, is)) {
			final ItemStack[] set = new ItemStack[this.getPattern().getSizeInventory()];

			// add one of each item to the items on the board...
			if (Platform.isServer()) {
				final InventoryCrafting ic = new InventoryCrafting(new ContainerNull(), 3, 3);
				for (int x = 0; x < 9; x++) {
					ic.setInventorySlotContents(x, this.getPattern().getStackInSlot(x));
				}

				final IRecipe r = Platform.findMatchingRecipe(ic, p.worldObj);

				if (r == null) {
					final Item target = request.getItem();
					if (target.isDamageable() && target.isRepairable()) {
						boolean isBad = false;
						for (int x = 0; x < ic.getSizeInventory(); x++) {
							final ItemStack pis = ic.getStackInSlot(x);
							if (pis == null) {
								continue;
							}
							if (pis.getItem() != target) {
								isBad = true;
							}
						}
						if (!isBad) {
							super.onPickupFromSlot(p, is);
							// actually necessary to cleanup this case...
							p.openContainer.onCraftMatrixChanged(this.craftInv);
							return request;
						}
					}
					return null;
				}

				is = r.getCraftingResult(ic);

				if (inv != null) {
					for (int x = 0; x < this.getPattern().getSizeInventory(); x++) {
						if (this.getPattern().getStackInSlot(x) != null) {
							set[x] = Platform.extractItemsByRecipe(this.energySrc, this.mySrc, inv, p.worldObj, r, is,
									ic, this.getPattern().getStackInSlot(x), x, all, Actionable.MODULATE, null);
							ic.setInventorySlotContents(x, set[x]);
						}
					}
				}
			}

			if (this.preCraft(p, inv, set, is)) {
				this.makeItem(p, is);

				this.postCraft(p, inv, set, is);
			}

			// shouldn't be necessary...
			p.openContainer.onCraftMatrixChanged(this.craftInv);

			return is;
		}

		return null;
	}

	private boolean preCraft(final EntityPlayer p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set,
			final ItemStack result) {
		return true;
	}

	private void makeItem(final EntityPlayer p, final ItemStack is) {
		super.onPickupFromSlot(p, is);
	}

	private void postCraft(final EntityPlayer p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set,
			final ItemStack result) {
		final List<ItemStack> drops = new ArrayList<ItemStack>();

		// add one of each item to the items on the board...
		if (Platform.isServer()) {
			// set new items onto the crafting table...
			for (int x = 0; x < this.craftInv.getSizeInventory(); x++) {
				if (this.craftInv.getStackInSlot(x) == null) {
					this.craftInv.setInventorySlotContents(x, set[x]);
				} else if (set[x] != null) {
					// eek! put it back!
					final IAEItemStack fail = inv.injectItems(AEItemStack.create(set[x]), Actionable.MODULATE,
							this.mySrc);
					if (fail != null) {
						drops.add(fail.getItemStack());
					}
				}
			}
		}

		if (drops.size() > 0) {
			Platform.spawnDrops(p.worldObj, (int) p.posX, (int) p.posY, (int) p.posZ, drops);
		}
	}

	IInventory getPattern() {
		return this.pattern;
	}
}