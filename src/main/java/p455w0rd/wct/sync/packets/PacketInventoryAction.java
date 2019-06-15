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
package p455w0rd.wct.sync.packets;

import java.io.IOException;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.ClientHelper;
import appeng.container.ContainerOpenContext;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import p455w0rd.ae2wtlib.api.container.ContainerWT;
import p455w0rd.ae2wtlib.api.networking.INetworkInfo;
import p455w0rd.wct.container.ContainerCraftAmount;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.sync.WCTPacket;

public class PacketInventoryAction extends WCTPacket {

	private final InventoryAction action;
	private final int slot;
	private final long id;
	private final IAEItemStack slotItem;

	// automatic.
	public PacketInventoryAction(final ByteBuf stream) throws IOException {
		action = InventoryAction.values()[stream.readInt()];
		slot = stream.readInt();
		id = stream.readLong();
		final boolean hasItem = stream.readBoolean();
		if (hasItem) {
			slotItem = AEItemStack.fromPacket(stream);
		}
		else {
			slotItem = null;
		}
	}

	// api
	public PacketInventoryAction(final InventoryAction action, final int slot, final IAEItemStack slotItem) throws IOException {

		if (Platform.isClient()) {
			throw new IllegalStateException("invalid packet, client cannot post inv actions with stacks.");
		}

		this.action = action;
		this.slot = slot;
		id = 0;
		this.slotItem = slotItem;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt(getPacketID());
		data.writeInt(action.ordinal());
		data.writeInt(slot);
		data.writeLong(id);

		if (slotItem == null) {
			data.writeBoolean(false);
		}
		else {
			data.writeBoolean(true);
			slotItem.writeToPacket(data);
		}

		configureWrite(data);
	}

	// api
	public PacketInventoryAction(final InventoryAction action, final int slot, final long id) {
		this.action = action;
		this.slot = slot;
		this.id = id;
		slotItem = null;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt(getPacketID());
		data.writeInt(action.ordinal());
		data.writeInt(slot);
		data.writeLong(id);
		data.writeBoolean(false);

		configureWrite(data);
	}

	@SuppressWarnings("unused")
	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		final EntityPlayerMP sender = (EntityPlayerMP) player;
		Container baseContainer = sender.openContainer;
		ContainerOpenContext context = null;
		if (sender.openContainer instanceof ContainerWCT) {
			baseContainer = sender.openContainer;
			context = ((ContainerWCT) baseContainer).getOpenContext();
		}

		if (action == InventoryAction.AUTO_CRAFT) {
			int x = (int) player.posX;
			int y = (int) player.posY;
			int z = (int) player.posZ;
			if (sender.openContainer instanceof ContainerWCT) {
				ContainerWCT wctContainer = (ContainerWCT) sender.openContainer;
				ModGuiHandler.open(ModGuiHandler.GUI_CRAFT_AMOUNT, player, player.getEntityWorld(), new BlockPos(x, y, z), false, wctContainer.isWTBauble(), wctContainer.getWTSlot());
			}
			/*
			else {
				ModGuiHandler.open(ModGuiHandler.GUI_CRAFT_AMOUNT, player, WTUtils.world(player), new BlockPos(x, y, z));
			}
			*/
			if (sender.openContainer instanceof ContainerCraftAmount) {
				final ContainerCraftAmount cca = (ContainerCraftAmount) sender.openContainer;

				if (baseContainer instanceof ContainerWT) {
					ContainerWT wtContainer = (ContainerWT) baseContainer;
					if (wtContainer.getTargetStack() != null) {
						cca.getCraftingItem().putStack(wtContainer.getTargetStack().asItemStackRepresentation());
						cca.setItemToCraft(wtContainer.getTargetStack());
					}
				}

				cca.detectAndSendChanges();
			}
		}
		else {
			if (baseContainer instanceof ContainerWCT) {
				((ContainerWCT) baseContainer).doAction(sender, action, slot, id);
				((ContainerWCT) baseContainer).detectAndSendChanges();
			}
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		if (action == InventoryAction.UPDATE_HAND) {
			ClientHelper ch = new ClientHelper();
			if (slotItem == null) {
				ch.getPlayers().get(0).inventory.setItemStack(ItemStack.EMPTY);
			}
			else {
				ch.getPlayers().get(0).inventory.setItemStack(slotItem.createItemStack());
			}
		}
	}
}