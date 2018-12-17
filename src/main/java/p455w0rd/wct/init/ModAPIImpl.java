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
package p455w0rd.wct.init;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.relauncher.Side;
import p455w0rd.ae2wtlib.integration.Baubles;
import p455w0rd.wct.WCT;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.WCTApi;
import p455w0rd.wct.sync.packets.PacketOpenGui;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class ModAPIImpl extends WCTApi {

	private static ModAPIImpl INSTANCE = null;

	public static ModAPIImpl instance() {
		if (ModAPIImpl.INSTANCE == null) {
			if (!ModAPIImpl.hasFinishedPreInit()) {
				return null;
			}
			ModAPIImpl.INSTANCE = new ModAPIImpl();
		}
		return INSTANCE;
	}

	protected static boolean hasFinishedPreInit() {
		if (WCT.PROXY.getLoaderState() == LoaderState.NOINIT) {
			ModLogger.warn("API is not available until " + ModGlobals.NAME + " starts the PreInit phase.");
			return false;
		}

		return true;
	}

	@Override
	public void openWCTGui(final EntityPlayer player, final boolean isBauble, final int wctSlot) {
		if ((player == null) || (player instanceof FakePlayer) || (player instanceof EntityPlayerMP) || FMLCommonHandler.instance().getSide() == Side.SERVER) {
			return;
		}
		ItemStack is = isBauble ? Baubles.getWTBySlot(player, wctSlot, IWirelessCraftingTerminalItem.class) : WCTUtils.getWCTBySlot(player, wctSlot);
		if (is.isEmpty()) {
			return;
		}
		if (isTerminalLinked(is)) {
			ModNetworking.instance().sendToServer(new PacketOpenGui(ModGuiHandler.GUI_WCT, wctSlot, isBauble));
		}
	}

	private boolean isTerminalLinked(final ItemStack wirelessTerminalItemstack) {
		String sourceKey = "";
		if (wirelessTerminalItemstack.getItem() instanceof IWirelessCraftingTerminalItem && wirelessTerminalItemstack.hasTagCompound()) {
			sourceKey = ((IWirelessCraftingTerminalItem) wirelessTerminalItemstack.getItem()).getEncryptionKey(wirelessTerminalItemstack);
			return (sourceKey != null) && (!sourceKey.isEmpty());
		}
		return false;
	}

	@Override
	public void openMagnetGui(EntityPlayer player, boolean isWCTaBauble, int wctSlot) {
		ModGuiHandler.open(ModGuiHandler.GUI_MAGNET, player, player.getEntityWorld(), player.getPosition(), true, isWCTaBauble, wctSlot);
	}

}
