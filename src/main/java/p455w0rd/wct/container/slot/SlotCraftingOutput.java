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
package p455w0rd.wct.container.slot;

import java.util.*;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.slot.AppEngCraftingSlot;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.*;
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

/**
 * @author p455w0rd
 *
 */
public class SlotCraftingOutput extends AppEngCraftingSlot {

	private final IItemHandler craftInv;
	private final IItemHandler pattern;

	private final IActionSource mySrc;
	private final IEnergySource energySrc;
	private final IStorageMonitorable storage;
	private final IContainerCraftingPacket container;

	public SlotCraftingOutput(final EntityPlayer player, final IActionSource mySrc, final IEnergySource energySrc, final IStorageMonitorable storage, final IItemHandler cMatrix, final IItemHandler secondMatrix, final IItemHandler output, final int x, final int y, final IContainerCraftingPacket ccp) {
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

	/**
	 * Return whether this slot's stack can be taken from this slot.
	 */
	@Override
	public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
		return false;
	}

	@Override
	public ItemStack onTake(final EntityPlayer p, final ItemStack is) {
		return is;
	}

	public void doClick(final InventoryAction action, final EntityPlayer who) {
		if (getStack().isEmpty() || Platform.isClient()) {
			return;
		}

		boolean shiftCraftToNetwork = false;
		ItemStack wirelessTerminal = ItemStack.EMPTY;
		if (container instanceof ContainerWCT) {
			wirelessTerminal = ((ContainerWCT) container).getWirelessTerminal();
		}
		if (!wirelessTerminal.isEmpty()) {
			shiftCraftToNetwork = WCTUtils.getShiftCraftMode(wirelessTerminal);
		}

		final IMEMonitor<IAEItemStack> inv = storage.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
		final int howManyPerCraft = getStack().getCount();
		int maxTimesToCraft = 0;

		InventoryAdaptor ia = null;
		if (action == InventoryAction.CRAFT_SHIFT) // craft into player inventory...
		{
			ia = InventoryAdaptor.getAdaptor(who);
			maxTimesToCraft = (int) Math.floor((double) getStack().getMaxStackSize() / (double) howManyPerCraft);
		}
		else if (action == InventoryAction.CRAFT_STACK) // craft into hand, full stack
		{
			ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
			maxTimesToCraft = (int) Math.floor((double) getStack().getMaxStackSize() / (double) howManyPerCraft);
		}
		else
		// pick up what was crafted...
		{
			ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
			maxTimesToCraft = 1;
		}

		//maxTimesToCraft = Math.min(capCraftingAttempts(maxTimesToCraft), getStack().getMaxStackSize());
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
				//hack to put items into network rather than inventory
				ItemStack extra = ItemStack.EMPTY;
				if (action == InventoryAction.CRAFT_SHIFT && container instanceof ContainerWCT && shiftCraftToNetwork) {
					ItemStack tmpStack = craftItem(who, rs, inv, all);
					IAEItemStack tmpAEStack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(tmpStack);
					if (tmpAEStack != null) {
						IAEItemStack aeStack = inv.injectItems(tmpAEStack, Actionable.MODULATE, mySrc);
						//extra = Platform.poweredInsert(energySrc, ((ContainerWCT)container).getCellInventory(), craftItem(who, rs, inv, all), mySrc);
						if (aeStack != null) {
							extra = aeStack.createItemStack();//Platform.poweredInsert(energySrc, inv, aeStack, mySrc).createItemStack();
						}
					}
				}
				else {
					extra = ia.addItems(craftItem(who, rs, inv, all));
				}
				if (!extra.isEmpty()) {
					final List<ItemStack> drops = new ArrayList<>();
					drops.add(extra);
					Platform.spawnDrops(who.world, new BlockPos((int) who.posX, (int) who.posY, (int) who.posZ), drops);
					return;
				}
			}
		}
	}

	protected IRecipe findRecipe(InventoryCrafting ic, World world) {
		if (container instanceof ContainerCraftingTerm) {
			final ContainerCraftingTerm containerTerminal = (ContainerCraftingTerm) container;
			final IRecipe recipe = containerTerminal.getCurrentRecipe();

			if (recipe != null && recipe.matches(ic, world)) {
				return containerTerminal.getCurrentRecipe();
			}
		}

		return CraftingManager.findMatchingRecipe(ic, world);
	}

	@Override
	protected NonNullList<ItemStack> getRemainingItems(InventoryCrafting ic, World world) {
		if (container instanceof ContainerCraftingTerm) {
			final ContainerCraftingTerm containerTerminal = (ContainerCraftingTerm) container;
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
		// update crafting matrix...
		ItemStack is = getStack();

		if (!is.isEmpty() && ItemStack.areItemsEqual(request, is)) {
			final ItemStack[] set = new ItemStack[getPattern().getSlots()];
			// Safeguard for empty slots in the inventory for now
			Arrays.fill(set, ItemStack.EMPTY);

			// add one of each item to the items on the board...
			if (Platform.isServer()) {
				final InventoryCrafting ic = new InventoryCrafting(new ContainerNull(), 3, 3);
				for (int x = 0; x < 9; x++) {
					ic.setInventorySlotContents(x, getPattern().getStackInSlot(x));
				}

				final IRecipe r = findRecipe(ic, p.world);

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
		final List<ItemStack> drops = new ArrayList<>();

		// add one of each item to the items on the board...
		if (Platform.isServer()) {
			// set new items onto the crafting table...
			for (int x = 0; x < craftInv.getSlots(); x++) {
				if (craftInv.getStackInSlot(x).isEmpty()) {
					ItemHandlerUtil.setStackInSlot(craftInv, x, set[x]);
				}
				else if (!set[x].isEmpty()) {
					// eek! put it back!
					final IAEItemStack fail = inv.injectItems(AEItemStack.fromItemStack(set[x]), Actionable.MODULATE, mySrc);
					if (fail != null) {
						drops.add(fail.createItemStack());
					}
				}
			}
		}

		if (drops.size() > 0) {
			Platform.spawnDrops(p.world, new BlockPos((int) p.posX, (int) p.posY, (int) p.posZ), drops);
		}
	}

	IItemHandler getPattern() {
		return pattern;
	}
}
