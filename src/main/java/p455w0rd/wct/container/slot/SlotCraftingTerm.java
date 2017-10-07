package p455w0rd.wct.container.slot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.util.WCTUtils;

public class SlotCraftingTerm extends AppEngCraftingSlot {

	private final IItemHandler craftInv;
	private final IItemHandler pattern;

	private final IActionSource mySrc;
	private final IEnergySource energySrc;
	private final IStorageMonitorable storage;
	private final IContainerCraftingPacket container;

	public SlotCraftingTerm(final EntityPlayer player, final IActionSource mySrc, final IEnergySource energySrc, final IStorageMonitorable storage, final IItemHandler cMatrix, final IItemHandler secondMatrix, final IItemHandler output, final int x, final int y, final IContainerCraftingPacket ccp) {
		super(player, cMatrix, output, 0, x, y);
		this.energySrc = energySrc;
		this.storage = storage;
		this.mySrc = mySrc;
		pattern = cMatrix;
		craftInv = secondMatrix;
		container = ccp;
	}

	public IItemHandler getCraftingMatrix() {
		return craftInv;
	}

	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}

	@Override
	public ItemStack onTake(final EntityPlayer p, final ItemStack is) {
		return is;
	}

	public void doClick(final InventoryAction action, final EntityPlayer who) {
		if (getStack().isEmpty()) {
			return;
		}
		if (Platform.isClient()) {
			return;
		}

		final IMEMonitor<IAEItemStack> inv = storage.getItemInventory();
		final int howManyPerCraft = getStack().getCount();
		int maxTimesToCraft = 0;

		InventoryAdaptor ia = null;
		if (action == InventoryAction.CRAFT_SHIFT) {
			ia = InventoryAdaptor.getAdaptor(who);
			maxTimesToCraft = (int) Math.floor((double) getStack().getMaxStackSize() / (double) howManyPerCraft);
		}
		else if (action == InventoryAction.CRAFT_STACK) {
			ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
			maxTimesToCraft = (int) Math.floor((double) getStack().getMaxStackSize() / (double) howManyPerCraft);
		}
		else {
			ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
			maxTimesToCraft = 1;
		}

		maxTimesToCraft = capCraftingAttempts(maxTimesToCraft);

		if (ia == null) {
			return;
		}

		final ItemStack rs = getStack().copy();
		if (rs.isEmpty()) {
			return;
		}

		for (int x = 0; x < maxTimesToCraft; x++) {
			if (ia.simulateAdd(rs).isEmpty()) {
				final IItemList<IAEItemStack> all = inv.getStorageList();
				final ItemStack extra = ia.addItems(craftItem(who, rs, inv, all));
				if (!extra.isEmpty()) {
					final List<ItemStack> drops = new ArrayList<ItemStack>();
					drops.add(extra);
					Platform.spawnDrops(WCTUtils.world(who), new BlockPos((int) who.posX, (int) who.posY, (int) who.posZ), drops);
					return;
				}
			}
		}
	}

	protected IRecipe findRecipe(InventoryCrafting ic, World world) {
		if (container instanceof ContainerWCT) {
			final ContainerWCT containerTerminal = (ContainerWCT) container;
			final IRecipe recipe = containerTerminal.getCurrentRecipe();

			if (recipe != null && recipe.matches(ic, world)) {
				return containerTerminal.getCurrentRecipe();
			}
		}

		return CraftingManager.findMatchingRecipe(ic, world);
	}

	@Override
	protected NonNullList<ItemStack> getRemainingItems(InventoryCrafting ic, World world) {
		if (container instanceof ContainerWCT) {
			final ContainerWCT containerTerminal = (ContainerWCT) container;
			final IRecipe recipe = containerTerminal.getCurrentRecipe();

			if (recipe != null && recipe.matches(ic, world)) {
				return containerTerminal.getCurrentRecipe().getRemainingItems(ic);
			}
		}

		return CraftingManager.getRemainingItems(ic, world);
	}

	private int capCraftingAttempts(final int maxTimesToCraft) {
		return maxTimesToCraft;
	}

	private ItemStack craftItem(final EntityPlayer p, final ItemStack request, final IMEMonitor<IAEItemStack> inv, final IItemList<IAEItemStack> all) {
		ItemStack is = getStack();

		if (!is.isEmpty() && ItemStack.areItemsEqual(request, is)) {
			final ItemStack[] set = new ItemStack[getPattern().getSlots()];

			Arrays.fill(set, ItemStack.EMPTY);

			// add one of each item to the items on the board...
			if (Platform.isServer()) {
				final InventoryCrafting ic = new InventoryCrafting(new ContainerNull(), 3, 3);
				for (int x = 0; x < 9; x++) {
					ic.setInventorySlotContents(x, getPattern().getStackInSlot(x));
				}

				final IRecipe r = findRecipe(ic, p.getEntityWorld());

				if (r == null) {
					final Item target = request.getItem();
					if (target.isDamageable() && target.isRepairable()) {
						boolean isBad = false;
						for (int x = 0; x < ic.getSizeInventory(); x++) {
							final ItemStack pis = ic.getStackInSlot(x);
							if (pis.isEmpty()) {
								continue;
							}
							if (pis.getItem() != target) {
								isBad = true;
							}
						}
						if (!isBad) {
							super.onTake(p, is);
							// actually necessary to cleanup this case...
							p.openContainer.onCraftMatrixChanged(new WrapperInvItemHandler(craftInv));
							return request;
						}
					}
					return ItemStack.EMPTY;
				}

				is = r.getCraftingResult(ic);

				if (inv != null) {
					for (int x = 0; x < getPattern().getSlots(); x++) {
						if (!getPattern().getStackInSlot(x).isEmpty()) {
							set[x] = Platform.extractItemsByRecipe(energySrc, mySrc, inv, p.world, r, is, ic, getPattern().getStackInSlot(x), x, all, Actionable.MODULATE, ItemViewCell.createFilter(container.getViewCells()));
							ic.setInventorySlotContents(x, set[x]);
						}
					}
				}
			}

			if (preCraft(p, inv, set, is)) {
				makeItem(p, is);

				postCraft(p, inv, set, is);
			}
			p.openContainer.onCraftMatrixChanged(new WrapperInvItemHandler(craftInv));

			return is;
		}
		return ItemStack.EMPTY;
	}

	private boolean preCraft(final EntityPlayer p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set, final ItemStack result) {
		return true;
	}

	private void makeItem(final EntityPlayer p, final ItemStack is) {
		super.onTake(p, is);
	}

	private void postCraft(final EntityPlayer p, final IMEMonitor<IAEItemStack> inv, final ItemStack[] set, final ItemStack result) {
		final List<ItemStack> drops = new ArrayList<ItemStack>();
		if (Platform.isServer()) {
			for (int x = 0; x < craftInv.getSlots(); x++) {
				if (craftInv.getStackInSlot(x).isEmpty()) {
					ItemHandlerUtil.setStackInSlot(craftInv, x, set[x]);
				}
				else if (!set[x].isEmpty()) {
					// eek! put it back!
					final IAEItemStack fail = inv.injectItems(AEItemStack.create(set[x]), Actionable.MODULATE, mySrc);
					if (fail != null) {
						drops.add(fail.createItemStack());
					}
				}
			}
		}

		if (drops.size() > 0) {
			Platform.spawnDrops(WCTUtils.world(p), new BlockPos((int) p.posX, (int) p.posY, (int) p.posZ), drops);
		}
	}

	IItemHandler getPattern() {
		return pattern;
	}
}