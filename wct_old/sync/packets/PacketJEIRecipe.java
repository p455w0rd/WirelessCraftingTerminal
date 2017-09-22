package p455w0rd.wct.sync.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import p455w0rd.wct.api.IWirelessCraftingTermHandler;
import p455w0rd.wct.helpers.WCTGuiObject;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

public class PacketJEIRecipe extends WCTPacket {

	private ItemStack[][] recipe;

	// automatic.
	public PacketJEIRecipe(final ByteBuf stream) throws IOException {
		final ByteArrayInputStream bytes = new ByteArrayInputStream(stream.array());
		bytes.skip(stream.readerIndex());
		final NBTTagCompound comp = CompressedStreamTools.readCompressed(bytes);
		if (comp != null) {
			recipe = new ItemStack[9][];
			for (int x = 0; x < recipe.length; x++) {
				final NBTTagList list = comp.getTagList("#" + x, 10);
				if (list.tagCount() > 0) {
					recipe[x] = new ItemStack[list.tagCount()];
					for (int y = 0; y < list.tagCount(); y++) {
						recipe[x][y] = new ItemStack(list.getCompoundTagAt(y));
					}
				}
			}
		}
	}

	// api
	public PacketJEIRecipe(final NBTTagCompound recipe) throws IOException {
		final ByteBuf data = Unpooled.buffer();

		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final DataOutputStream outputStream = new DataOutputStream(bytes);

		data.writeInt(getPacketID());

		CompressedStreamTools.writeCompressed(recipe, outputStream);
		data.writeBytes(bytes.toByteArray());

		configureWrite(data);
	}

	private WCTGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (it != null) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WCTGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		final EntityPlayerMP pmp = (EntityPlayerMP) player;
		final Container con = pmp.openContainer;

		if (!(con instanceof IContainerCraftingPacket)) {
			return;
		}

		final IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
		final IGridNode node = cct.getNetworkNode();

		if (node == null) {
			return;
		}

		final IGrid grid = node.getGrid();
		if (grid == null) {
			return;
		}

		final IStorageGrid inv = grid.getCache(IStorageGrid.class);
		final IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
		final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
		final IItemHandler craftMatrix = cct.getInventoryByName("crafting");
		final IItemHandler playerInventory = cct.getInventoryByName("player");

		if (inv != null && recipe != null && security != null) {
			final IMEMonitor<IAEItemStack> storage = inv.getItemInventory();
			final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(cct.getViewCells());

			for (int x = 0; x < craftMatrix.getSlots(); x++) {
				ItemStack currentItem = craftMatrix.getStackInSlot(x);

				// prepare slots
				if (!currentItem.isEmpty()) {
					// already the correct item?
					ItemStack newItem = canUseInSlot(x, currentItem);

					// put away old item
					if (newItem != currentItem && security.hasPermission(player, SecurityPermissions.INJECT)) {
						final IAEItemStack in = AEItemStack.create(currentItem);
						final IAEItemStack out = cct.useRealItems() ? Platform.poweredInsert(energy, storage, in, cct.getActionSource()) : null;
						if (out != null) {
							currentItem = out.getItemStack();
						}
						else {
							currentItem = ItemStack.EMPTY;
						}
					}
				}

				if (currentItem.isEmpty() && recipe[x] != null) {
					// for each variant
					for (int y = 0; y < recipe[x].length && currentItem.isEmpty(); y++) {
						final IAEItemStack request = AEItemStack.create(recipe[x][y]);
						if (request != null) {
							// try ae
							if ((filter == null || filter.isListed(request)) && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
								request.setStackSize(1);
								IAEItemStack out;
								if (cct.useRealItems()) {
									out = Platform.poweredExtraction(energy, storage, request, cct.getActionSource());
								}
								else {
									out = storage.extractItems(request, Actionable.SIMULATE, cct.getActionSource());
								}

								if (out != null) {
									currentItem = out.getItemStack();
								}
							}

							// try inventory
							if (currentItem.isEmpty()) {
								AdaptorItemHandler ad = new AdaptorItemHandler(playerInventory);

								if (cct.useRealItems()) {
									currentItem = ad.removeItems(1, recipe[x][y], null);
								}
								else {
									currentItem = ad.simulateRemove(1, recipe[x][y], null);
								}
							}
						}
					}
				}
				ItemHandlerUtil.setStackInSlot(craftMatrix, x, currentItem);
			}
			con.onCraftMatrixChanged(new WrapperInvItemHandler(craftMatrix));
		}
	}
	/*
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		final EntityPlayerMP pmp = (EntityPlayerMP) player;
		final Container con = pmp.openContainer;
	
		if (con instanceof IContainerCraftingPacket) {
			final IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
			IGridNode node = cct.getNetworkNode();
			if (node == null) {
				WCTGuiObject obj = getGuiObject(WCTUtils.getWirelessTerm(player.inventory), player, WCTUtils.world(player), (int) player.posX, (int) player.posY, (int) player.posZ);
				node = obj.getActionableNode(true);
			}
	
			if (node != null) {
				final IGrid grid = node.getGrid();
				if (grid == null) {
					return;
				}
	
				final IStorageGrid inv = grid.getCache(IStorageGrid.class);
				final IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
				final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
				final IItemHandler craftMatrix = cct.getInventoryByName("crafting");
				final IItemHandler playerInventory = cct.getInventoryByName("player");
	
				final Actionable realForFake = cct.useRealItems() ? Actionable.MODULATE : Actionable.SIMULATE;
	
				if (inv != null && recipe != null && security != null) {
					final InventoryCrafting testInv = new InventoryCrafting(new ContainerNull(), 3, 3);
					for (int x = 0; x < 9; x++) {
						if (recipe[x] != null && recipe[x].length > 0) {
							testInv.setInventorySlotContents(x, recipe[x][0]);
						}
					}
	
					final IRecipe r = Platform.findMatchingRecipe(testInv, WCTUtils.world(pmp));
	
					if (r != null && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
						final ItemStack is = r.getCraftingResult(testInv);
	
						if (is != null) {
							final IMEMonitor<IAEItemStack> storage = inv.getItemInventory();
							final IItemList<IAEItemStack> all = storage.getStorageList();
							final ItemStack[] nullStack = new ItemStack[0];
							final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(nullStack);
	
							for (int x = 0; x < craftMatrix.getSlots(); x++) {
								final ItemStack patternItem = testInv.getStackInSlot(x);
	
								ItemStack currentItem = craftMatrix.getStackInSlot(x);
								if (currentItem != null) {
									testInv.setInventorySlotContents(x, currentItem);
									final ItemStack newItemStack = r.matches(testInv, WCTUtils.world(pmp)) ? r.getCraftingResult(testInv) : null;
									testInv.setInventorySlotContents(x, patternItem);
	
									if (newItemStack == null || !Platform.itemComparisons().isSameItem(newItemStack, is)) {
										final IAEItemStack in = AEItemStack.create(currentItem);
										if (in != null) {
											final IAEItemStack out = realForFake == Actionable.SIMULATE ? null : Api.INSTANCE.storage().poweredInsert(energy, storage, in, cct.getActionSource());
											if (out != null) {
												ItemHandlerUtil.setStackInSlot(craftMatrix, x, out.getItemStack());
											}
											else {
												ItemHandlerUtil.setStackInSlot(craftMatrix, x, ItemStack.EMPTY);
											}
	
											currentItem = craftMatrix.getStackInSlot(x);
										}
									}
								}
	
								// True if we need to fetch an item for the recipe
								if (patternItem != null && currentItem == null) {
									// Grab from network by recipe
									ItemStack whichItem = Platform.extractItemsByRecipe(energy, cct.getActionSource(), storage, WCTUtils.world(player), r, is, testInv, patternItem, x, all, realForFake, filter);
	
									// If that doesn't get it, grab exact items from network (?)
									// TODO see if this code is necessary
									if (whichItem == null) {
										for (int y = 0; y < recipe[x].length; y++) {
											final IAEItemStack request = AEItemStack.create(recipe[x][y]);
											if (request != null) {
												if (filter == null || filter.isListed(request)) {
													request.setStackSize(1);
													final IAEItemStack out = Api.INSTANCE.storage().poweredExtraction(energy, storage, request, cct.getActionSource());
													if (out != null) {
														whichItem = out.getItemStack();
														break;
													}
												}
											}
										}
									}
	
									// If that doesn't work, grab from the player's inventory
									if (whichItem == null && playerInventory != null) {
										whichItem = extractItemFromPlayerInventory(player, realForFake, patternItem);
									}
	
									ItemHandlerUtil.setStackInSlot(craftMatrix, x, whichItem);
								}
							}
							con.onCraftMatrixChanged(new WrapperInvItemHandler(craftMatrix));
						}
					}
				}
			}
		}
	}
	*/

	/**
	 * Tries to extract an item from the player inventory. Does account for fuzzy items.
	 *
	 * @param player the {@link EntityPlayer} to extract from
	 * @param mode the {@link Actionable} to simulate or modulate the operation
	 * @param patternItem which {@link ItemStack} to extract
	 * @return null or a found {@link ItemStack}
	 */
	private ItemStack extractItemFromPlayerInventory(final EntityPlayer player, final Actionable mode, final ItemStack patternItem) {
		final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player);
		final AEItemStack request = AEItemStack.create(patternItem);
		final boolean isSimulated = mode == Actionable.SIMULATE;
		final boolean checkFuzzy = request.isOre() || patternItem.getItemDamage() == OreDictionary.WILDCARD_VALUE || patternItem.hasTagCompound() || patternItem.isItemStackDamageable();

		if (!checkFuzzy) {
			if (isSimulated) {
				return ia.simulateRemove(1, patternItem, null);
			}
			else {
				return ia.removeItems(1, patternItem, null);
			}
		}
		else {
			if (isSimulated) {
				return ia.simulateSimilarRemove(1, patternItem, FuzzyMode.IGNORE_ALL, null);
			}
			else {
				return ia.removeSimilarItems(1, patternItem, FuzzyMode.IGNORE_ALL, null);
			}
		}
	}

	private ItemStack canUseInSlot(int slot, ItemStack is) {
		if (recipe[slot] != null) {
			for (ItemStack option : recipe[slot]) {
				if (is.isItemEqual(option)) {
					return is;
				}
			}
		}
		return ItemStack.EMPTY;
	}
}
