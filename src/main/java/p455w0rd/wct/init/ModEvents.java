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

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.core.AEConfig;
import appeng.integration.Integrations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.integration.Baubles;
import p455w0rd.wct.client.gui.GuiWCT;
import p455w0rd.wct.init.ModIntegration.Mods;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.util.WCTUtils;

/**
 * @author p455w0rd
 *
 */
@EventBusSubscriber(modid = ModGlobals.MODID)
public class ModEvents {

	public static long CLIENT_TICKS = 0L;
	public static long SERVER_TICKS = 0L;

	@SubscribeEvent
	public static void onItemRegistryReady(RegistryEvent.Register<Item> event) {
		ModItems.register(event.getRegistry());
	}

	@SubscribeEvent
	public static void tickEvent(TickEvent.PlayerTickEvent e) {
		EntityPlayer player = e.player;
		if (!(player instanceof EntityPlayerMP)) {
			return;
		}
		InventoryPlayer playerInv = player.inventory;
		List<Pair<Boolean, Pair<Integer, ItemStack>>> terminals = WCTUtils.getCraftingTerminals(player);
		int invSize = playerInv.getSizeInventory();
		if (invSize <= 0) {
			return;
		}
		for (int i = 0; i < terminals.size(); i++) {
			ItemStack wct = terminals.get(i).getRight().getRight();
			if (!ItemMagnet.getMagnetFromWCT(wct).isEmpty()) {
				((ItemMagnet) ItemMagnet.getMagnetFromWCT(wct).getItem()).doMagnet(player, wct, terminals.get(i).getLeft(), terminals.get(i).getRight().getLeft());
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onClientTick(ClientTickEvent e) {
		if (e.phase == Phase.END) {
			if (CLIENT_TICKS > Long.MAX_VALUE - 1000) {
				CLIENT_TICKS = 0L;
			}
			CLIENT_TICKS++;
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onKeyInput(KeyInputEvent e) {
		WCTUtils.handleKeybind();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMouseEvent(MouseEvent event) {
		WCTUtils.handleKeybind();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onModelBake(ModelBakeEvent event) {
		ModItems.initModels(event);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onModelRegister(ModelRegistryEvent event) {
		ModItems.registerTEISRs(event);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onkeyTyped(GuiScreenEvent.KeyboardInputEvent.Post e) {
		if (Mods.JEI.isLoaded() && Minecraft.getMinecraft().currentScreen instanceof GuiWCT) {
			Enum<?> searchMode = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);
			if (searchMode == SearchBoxMode.JEI_AUTOSEARCH || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH || searchMode == SearchBoxMode.JEI_AUTOSEARCH_KEEP || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH_KEEP) {
				GuiWCT gui = (GuiWCT) Minecraft.getMinecraft().currentScreen;
				String searchText = Integrations.jei().getSearchText();
				if (gui.getSearchField() != null) {
					gui.getRepo().setSearchString(searchText);
					gui.getRepo().updateView();
					gui.getSearchField().setText(searchText);
					GuiWCT.memoryText = searchText;
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPickup(EntityItemPickupEvent e) {
		if (e.getEntityPlayer() != null && e.getEntityPlayer() instanceof EntityPlayerMP) {
			if (!WTApi.instance().getConfig().isOldInfinityMechanicEnabled() && e.getItem().getItem().getItem() == WTApi.instance().getBoosterCard()) {
				if (Mods.BAUBLES.isLoaded()) {
					for (Pair<Integer, ItemStack> termPair : Baubles.getAllWTBaubles(e.getEntityPlayer())) {
						ItemStack wirelessTerminal = termPair.getRight();
						if (!wirelessTerminal.isEmpty() && WTApi.instance().shouldConsumeBoosters(wirelessTerminal)) {
							e.setCanceled(true);
							ItemStack boosters = e.getItem().getItem().copy();
							WTApi.instance().addInfinityBoosters(wirelessTerminal, boosters);
							WTApi.instance().getNetHandler().sendTo(WTApi.instance().getNetHandler().createInfinityEnergySyncPacket(WTApi.instance().getInfinityEnergy(wirelessTerminal), e.getEntityPlayer().getUniqueID(), true, termPair.getLeft()), (EntityPlayerMP) e.getEntityPlayer());
							e.getItem().setDead();
							return;
						}
					}
				}
				for (Pair<Boolean, Pair<Integer, ItemStack>> termPair : WCTUtils.getCraftingTerminals(e.getEntityPlayer())) {
					ItemStack wirelessTerminal = termPair.getRight().getRight();
					boolean shouldConsume = WTApi.instance().shouldConsumeBoosters(wirelessTerminal);
					if (!wirelessTerminal.isEmpty() && shouldConsume) {
						e.setCanceled(true);
						ItemStack boosters = e.getItem().getItem().copy();
						WTApi.instance().addInfinityBoosters(wirelessTerminal, boosters);
						WTApi.instance().getNetHandler().sendTo(WTApi.instance().getNetHandler().createInfinityEnergySyncPacket(WTApi.instance().getInfinityEnergy(wirelessTerminal), e.getEntityPlayer().getUniqueID(), true, termPair.getRight().getLeft()), (EntityPlayerMP) e.getEntityPlayer());
						e.getItem().setDead();
						return;
					}
				}
			}
		}
	}
}
