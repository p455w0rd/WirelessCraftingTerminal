package p455w0rd.wct.container.slot;

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
import net.minecraft.util.math.BlockPos;
import p455w0rd.wct.util.WCTUtils;

public class SlotCraftingTerm extends AppEngCraftingSlot {

	private final IInventory craftInv;
	private final IInventory pattern;

	private final BaseActionSource mySrc;
	private final IEnergySource energySrc;
	private final IStorageMonitorable storage;

	public SlotCraftingTerm(final EntityPlayer player, final BaseActionSource mySrc, final IEnergySource energySrc, final IStorageMonitorable storage, final IInventory cMatrix, final IInventory secondMatrix, final IInventory output, final int x, final int y, final IContainerCraftingPacket ccp) {
		super(player, cMatrix, output, 0, x, y);
		this.energySrc = energySrc;
		this.storage = storage;
		this.mySrc = mySrc;
		pattern = cMatrix;
		craftInv = secondMatrix;
	}

	public IInventory getCraftingMatrix() {
		return craftInv;
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}

	@Override
	public void onPickupFromSlot(final EntityPlayer p, final ItemStack is) {
	}

	public void doClick(final InventoryAction action, final EntityPlayer who) {
		if (getStack() == null) {
			return;
		}
		if (Platform.isClient()) {
			return;
		}

		final IMEMonitor<IAEItemStack> inv = storage.getItemInventory();
		final int howManyPerCraft = getStack().stackSize;
		int maxTimesToCraft = 0;

		InventoryAdaptor ia = null;
		if (action == InventoryAction.CRAFT_SHIFT) {
			ia = InventoryAdaptor.getAdaptor(who, null);
			maxTimesToCraft = (int) Math.floor((double) getStack().getMaxStackSize() / (double) howManyPerCraft);
		}
		else if (action == InventoryAction.CRAFT_STACK) {
			ia = new AdaptorPlayerHand(who);
			maxTimesToCraft = (int) Math.floor((double) getStack().getMaxStackSize() / (double) howManyPerCraft);
		}
		else {
			ia = new AdaptorPlayerHand(who);
			maxTimesToCraft = 1;
		}

		maxTimesToCraft = capCraftingAttempts(maxTimesToCraft);

		if (ia == null) {
			return;
		}

		final ItemStack rs = Platform.cloneItemStack(getStack());
		if (rs == null) {
			return;
		}

		for (int x = 0; x < maxTimesToCraft; x++) {
			if (ia.simulateAdd(rs) == null) {
				final IItemList<IAEItemStack> all = inv.getStorageList();
				final ItemStack extra = ia.addItems(craftItem(who, rs, inv, all));
				if (extra != null) {
					final List<ItemStack> drops = new ArrayList<ItemStack>();
					drops.add(extra);
					Platform.spawnDrops(WCTUtils.world(who), new BlockPos((int) who.posX, (int) who.posY, (int) who.posZ), drops);
					return;
				}
			}
		}
	}

	private int capCraftingAttempts(final int maxTimesToCraft) {
		return maxTimesToCraft;
	}

	@SuppressWarnings({
			"rawtypes",
			"unchecked"
	})
	private ItemStack craftItem(final EntityPlayer p, final ItemStack request, final IMEMonitor<IAEItemStack> inv, final IItemList all) {
		ItemStack is = getStack();

		if (is != null && Platform.itemComparisons().isEqualItem(request, is)) {
			final ItemStack[] set = new ItemStack[getPattern().getSizeInventory()];

			// add one of each item to the items on the board...
			if (Platform.isServer()) {
				final InventoryCrafting ic = new InventoryCrafting(new ContainerNull(), 3, 3);
				for (int x = 0; x < 9; x++) {
					ic.setInventorySlotContents(x, getPattern().getStackInSlot(x));
				}

				final IRecipe r = Platform.findMatchingRecipe(ic, WCTUtils.world(p));

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
							p.openContainer.onCraftMatrixChanged(craftInv);
							return request;
						}
					}
					return null;
				}

				is = r.getCraftingResult(ic);

				if (inv != null) {
					for (int x = 0; x < getPattern().getSizeInventory(); x++) {
						if (getPattern().getStackInSlot(x) != null) {
							set[x] = Platform.extractItemsByRecipe(energySrc, mySrc, inv, WCTUtils.world(p), r, is, ic, getPattern().getStackInSlot(x), x, all, Actionable.MODULATE, null);
							ic.setInventorySlotContents(x, set[x]);
						}
					}
				}
			}

			if (preCraft(p, inv, set, is)) {
				makeItem(p, is);

				postCraft(p, inv, set, is);
			}
			p.openContainer.onCraftMatrixChanged(craftInv);

			return is;
		}
		return null;
	}

	private boolean preCraft(final EntityPlayer p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set, final ItemStack result) {
		return true;
	}

	private void makeItem(final EntityPlayer p, final ItemStack is) {
		super.onPickupFromSlot(p, is);
	}

	private void postCraft(final EntityPlayer p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set, final ItemStack result) {
		final List<ItemStack> drops = new ArrayList<ItemStack>();
		if (Platform.isServer()) {
			for (int x = 0; x < craftInv.getSizeInventory(); x++) {
				if (craftInv.getStackInSlot(x) == null) {
					craftInv.setInventorySlotContents(x, set[x]);
				}
				else if (set[x] != null) {
					// eek! put it back!
					final IAEItemStack fail = inv.injectItems(AEItemStack.create(set[x]), Actionable.MODULATE, mySrc);
					if (fail != null) {
						drops.add(fail.getItemStack());
					}
				}
			}
		}

		if (drops.size() > 0) {
			Platform.spawnDrops(WCTUtils.world(p), new BlockPos((int) p.posX, (int) p.posY, (int) p.posZ), drops);
		}
	}

	IInventory getPattern() {
		return pattern;
	}
}