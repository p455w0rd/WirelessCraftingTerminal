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
package p455w0rd.wct.implementation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.relauncher.Side;
import p455w0rd.wct.WCT;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.api.WCTApi;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.init.ModLogger;
import p455w0rd.wct.init.ModNetworking;
import p455w0rd.wct.sync.packets.PacketOpenGui;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
public class WCTAPIImpl extends WCTApi {

	private static WCTAPIImpl INSTANCE = null;

	public static WCTAPIImpl instance() {
		if (WCTAPIImpl.INSTANCE == null) {
			if (!WCTAPIImpl.hasFinishedPreInit()) {
				return null;
			}
			WCTAPIImpl.INSTANCE = new WCTAPIImpl();
		}
		return INSTANCE;
	}

	protected static boolean hasFinishedPreInit() {
		if (WCT.PROXY.getLoaderState() == LoaderState.NOINIT) {
			ModLogger.warn("API is not available until WCT finishes the PreInit phase.");
			return false;
		}

		return true;
	}

	@Override
	public boolean isInfinityBoosterCardEnabled() {
		return ModConfig.WCT_BOOSTER_ENABLED;
	}

	@Override
	public void openWirelessCraftingTerminalGui(final EntityPlayer player) {
		if ((player == null) || (player instanceof FakePlayer) || (player instanceof EntityPlayerMP) || FMLCommonHandler.instance().getSide() == Side.SERVER) {
			return;
		}
		ItemStack is = WCTUtils.getWirelessTerm(player.inventory);
		if (is == null) {
			return;
		}
		if (isTerminalLinked(is)) {
			ModNetworking.instance().sendToServer(new PacketOpenGui(ModGuiHandler.GUI_WCT));
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

}
