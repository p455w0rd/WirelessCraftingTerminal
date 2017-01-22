package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

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
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.IPartitionList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;

public class PacketNEIRecipe extends WCTPacket {

	private ItemStack[][] recipe;

	// automatic.
	public PacketNEIRecipe(final ByteBuf stream) throws IOException {
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
						recipe[x][y] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(y));
					}
				}
			}
		}
	}

	// api
	public PacketNEIRecipe(final NBTTagCompound recipe) throws IOException {
		final ByteBuf data = Unpooled.buffer();

		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final DataOutputStream outputStream = new DataOutputStream(bytes);

		data.writeInt(getPacketID());

		CompressedStreamTools.writeCompressed(recipe, outputStream);
		data.writeBytes(bytes.toByteArray());

		configureWrite(data);
	}

	private WirelessTerminalGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
		if (it != null) {
			final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WirelessTerminalGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		final EntityPlayerMP pmp = (EntityPlayerMP) player;
		final Container con = pmp.openContainer;

		if (con instanceof IContainerCraftingPacket) {
			final IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
			IGridNode node = cct.getNetworkNode();

			if (node == null) {
				WirelessTerminalGuiObject obj = getGuiObject(RandomUtils.getWirelessTerm(player.inventory), player, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
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
				final IInventory craftMatrix = cct.getInventoryByName("crafting");
				final IInventory playerInventory = cct.getInventoryByName("container.inventory");

				final Actionable realForFake = Actionable.MODULATE;

				if (inv != null && recipe != null && security != null) {
					final InventoryCrafting testInv = new InventoryCrafting(new ContainerNull(), 3, 3);
					for (int x = 0; x < 9; x++) {
						if (recipe[x] != null && recipe[x].length > 0) {
							testInv.setInventorySlotContents(x, recipe[x][0]);
						}
					}

					final IRecipe r = Platform.findMatchingRecipe(testInv, pmp.worldObj);

					if (r != null && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
						final ItemStack is = r.getCraftingResult(testInv);

						if (is != null) {
							final IMEMonitor<IAEItemStack> storage = inv.getItemInventory();
							final IItemList all = storage.getStorageList();
							final ItemStack[] nullStack = new ItemStack[0];
							final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(nullStack);

							for (int x = 0; x < craftMatrix.getSizeInventory(); x++) {
								final ItemStack patternItem = testInv.getStackInSlot(x);

								ItemStack currentItem = craftMatrix.getStackInSlot(x);
								if (currentItem != null) {
									testInv.setInventorySlotContents(x, currentItem);
									final ItemStack newItemStack = r.matches(testInv, pmp.worldObj) ? r.getCraftingResult(testInv) : null;
									testInv.setInventorySlotContents(x, patternItem);

									if (newItemStack == null || !Platform.isSameItemPrecise(newItemStack, is)) {
										final IAEItemStack in = AEItemStack.create(currentItem);
										if (in != null) {
											final IAEItemStack out = Platform.poweredInsert(energy, storage, in, cct.getActionSource());
											if (out != null) {
												craftMatrix.setInventorySlotContents(x, out.getItemStack());
											}
											else {
												craftMatrix.setInventorySlotContents(x, null);
											}

											currentItem = craftMatrix.getStackInSlot(x);
										}
									}
								}

								// True if we need to fetch an item for the recipe
								if (patternItem != null && currentItem == null) {
									// Grab from network by recipe
									ItemStack whichItem = Platform.extractItemsByRecipe(energy, cct.getActionSource(), storage, player.worldObj, r, is, testInv, patternItem, x, all, realForFake, filter);
									//ItemStack whichItem = Platform.extractItemsByRecipe( energy, cct.getActionSource(), storage, player.worldObj, r, is, testInv, patternItem, x, all, realForFake, null );

									// If that doesn't get it, grab exact items from network (?)
									// TODO see if this code is necessary

									if (whichItem == null) {
										for (int y = 0; y < recipe[x].length; y++) {
											final IAEItemStack request = AEItemStack.create(recipe[x][y]);
											if (request != null) {
												if (filter == null || filter.isListed(request)) {
													request.setStackSize(1);
													final IAEItemStack out = Platform.poweredExtraction(energy, storage, request, cct.getActionSource());
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

									craftMatrix.setInventorySlotContents(x, whichItem);
								}
							}
							con.onCraftMatrixChanged(craftMatrix);
						}
					}
				}
			}
		}
	}

	/**
	 * Tries to extract an item from the player inventory. Does account for fuzzy items.
	 *
	 * @param player the {@link EntityPlayer} to extract from
	 * @param mode the {@link Actionable} to simulate or modulate the operation
	 * @param patternItem which {@link ItemStack} to extract
	 * @return null or a found {@link ItemStack}
	 */
	private ItemStack extractItemFromPlayerInventory(final EntityPlayer player, final Actionable mode, final ItemStack patternItem) {
		final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
		final AEItemStack request = AEItemStack.create(patternItem);
		//final boolean isSimulated = mode == Actionable.SIMULATE;
		final boolean checkFuzzy = request.isOre() || patternItem.getItemDamage() == OreDictionary.WILDCARD_VALUE || patternItem.hasTagCompound() || patternItem.isItemStackDamageable();

		if (!checkFuzzy) {
			return ia.removeItems(1, patternItem, null);
		}
		else {
			return ia.removeSimilarItems(1, patternItem, FuzzyMode.IGNORE_ALL, null);
		}
	}
}
