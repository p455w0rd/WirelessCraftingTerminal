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

import java.util.Random;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.core.AEConfig;
import appeng.integration.Integrations;
import appeng.tile.networking.TileController;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.wct.WCT;
import p455w0rd.wct.client.gui.GuiWCT;
import p455w0rd.wct.client.render.BaubleRenderDispatcher;
import p455w0rd.wct.init.ModIntegration.Mods;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.packets.PacketConfigSync;
import p455w0rd.wct.sync.packets.PacketSyncInfinityEnergy;
import p455w0rd.wct.util.WCTUtils;
import p455w0rdslib.capabilities.CapabilityChunkLoader;
import p455w0rdslib.capabilities.CapabilityChunkLoader.ProviderTE;
import p455w0rdslib.util.ChunkUtils;

/**
 * @author p455w0rd
 *
 */
public class ModEvents {

	public static long CLIENT_TICKS = 0L;
	public static long SERVER_TICKS = 0L;

	public static void preInit() {
		MinecraftForge.EVENT_BUS.register(new ModEvents());
		ChunkUtils.register(WCT.INSTANCE);
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<TileEntity> event) {
		if (event.getObject() instanceof TileController && ModConfig.WCT_ENABLE_CONTROLLER_CHUNKLOADER) {
			TileController controller = (TileController) event.getObject();
			event.addCapability(new ResourceLocation(ModGlobals.MODID, "chunkloader"), new ProviderTE(controller));
		}
	}

	@SubscribeEvent
	public void onPlace(BlockEvent.PlaceEvent e) {
		World world = e.getWorld();
		BlockPos pos = e.getPos();
		if (world != null && pos != null && world.getTileEntity(pos) != null && !world.isRemote && ModConfig.WCT_ENABLE_CONTROLLER_CHUNKLOADER) {
			if (world.getTileEntity(pos) instanceof TileController) {
				TileEntity tile = world.getTileEntity(pos);
				if (tile.hasCapability(CapabilityChunkLoader.CAPABILITY_CHUNKLOADER_TE, null)) {
					tile.getCapability(CapabilityChunkLoader.CAPABILITY_CHUNKLOADER_TE, null).attachChunkLoader(WCT.INSTANCE);
				}
			}
		}
	}

	@SubscribeEvent
	public void onBreak(BlockEvent.BreakEvent e) {
		World world = e.getWorld();
		BlockPos pos = e.getPos();
		if (world != null && pos != null && world.getTileEntity(pos) != null && !world.isRemote && ModConfig.WCT_ENABLE_CONTROLLER_CHUNKLOADER) {
			if (world.getTileEntity(pos) instanceof TileController) {
				TileEntity tile = world.getTileEntity(pos);
				if (tile.hasCapability(CapabilityChunkLoader.CAPABILITY_CHUNKLOADER_TE, null)) {
					tile.getCapability(CapabilityChunkLoader.CAPABILITY_CHUNKLOADER_TE, null).detachChunkLoader(WCT.INSTANCE);
				}
			}
		}
	}

	@SubscribeEvent
	public void tickEvent(TickEvent.PlayerTickEvent e) {
		EntityPlayer player = e.player;
		IInventory playerInv = player.inventory;
		ItemStack wirelessTerm = WCTUtils.getWirelessTerm((InventoryPlayer) playerInv);
		int invSize = playerInv.getSizeInventory();
		if (invSize <= 0) {
			return;
		}
		if (player instanceof EntityPlayerMP && !ModConfig.USE_OLD_INFINTY_MECHANIC && !wirelessTerm.isEmpty() && WCTUtils.shouldConsumeBoosters(wirelessTerm)) {
			for (int i = 0; i < invSize; ++i) {
				ItemStack slotStack = playerInv.getStackInSlot(i);
				if (!slotStack.isEmpty() && slotStack.getItem() == ModItems.BOOSTER_CARD) {
					WCTUtils.addInfinityBoosters(wirelessTerm, slotStack);
					ModNetworking.instance().sendTo(new PacketSyncInfinityEnergy(WCTUtils.getInfinityEnergy(wirelessTerm)), (EntityPlayerMP) player);
					playerInv.setInventorySlotContents(i, ItemStack.EMPTY);
				}
			}
		}
		if (!wirelessTerm.isEmpty() && wirelessTerm.hasTagCompound()) {
			NBTTagCompound magnetNBT = wirelessTerm.getSubCompound(WCTUtils.MAGNET_SLOT_NBT);
			if (magnetNBT != null) {
				NBTTagList tagList = magnetNBT.getTagList("Items", 10);
				if (tagList != null && !tagList.hasNoTags()) {
					NBTTagCompound magCompound = tagList.getCompoundTagAt(0);
					if (magCompound != null) {
						ItemStack magnetItem = new ItemStack(magCompound);
						if (!magnetItem.isEmpty()) {
							if (magnetItem.getItem() instanceof ItemMagnet) {
								((ItemMagnet) magnetItem.getItem()).setItemStack(magnetItem);
								((ItemMagnet) magnetItem.getItem()).doMagnet(magnetItem, WCTUtils.world(e.player), e.player, wirelessTerm);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(ClientTickEvent e) {
		if (e.phase == Phase.END) {
			if (CLIENT_TICKS > Long.MAX_VALUE - 1000) {
				CLIENT_TICKS = 0L;
			}
			CLIENT_TICKS++;
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onKeyInput(KeyInputEvent e) {
		WCTUtils.handleKeybind();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMouseEvent(MouseEvent event) {
		WCTUtils.handleKeybind();
	}

	@SubscribeEvent
	public void onMobDrop(LivingDropsEvent event) {
		ItemStack stack = new ItemStack(ModItems.BOOSTER_CARD);
		EntityItem drop = new EntityItem(event.getEntityLiving().getEntityWorld(), event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, stack);
		if (event.getEntity() instanceof EntityDragon && ModConfig.WCT_BOOSTER_ENABLED && ModConfig.WCT_DRAGON_DROPS_BOOSTER) {
			event.getDrops().add(drop);
		}

		if (event.getEntity() instanceof EntityWither && ModConfig.WCT_BOOSTER_ENABLED && ModConfig.WCT_WITHER_DROPS_BOOSTER) {
			Random rand = event.getEntityLiving().getEntityWorld().rand;
			int n = rand.nextInt(100);
			if (n <= ModConfig.WCT_BOOSTER_DROP_CHANCE) {
				event.getDrops().add(drop);
			}
		}

		if (event.getEntity() instanceof EntityEnderman && ModConfig.WCT_BOOSTER_ENABLED && ModConfig.WCT_ENDERMAN_DROP_BOOSTERS) {
			Random rand = event.getEntityLiving().getEntityWorld().rand;
			int n = rand.nextInt(100);
			if (n <= ModConfig.WCT_ENDERMAN_BOOSTER_DROP_CHANCE) {
				event.getDrops().add(drop);
			}
		}

	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent e) {
		if (e.player instanceof EntityPlayerMP) {
			final PacketConfigSync p = new PacketConfigSync(ModConfig.WCT_MAX_POWER, ModConfig.WCT_BOOSTER_ENABLED, ModConfig.WCT_MINETWEAKER_OVERRIDE, ModConfig.WCT_ENABLE_CONTROLLER_CHUNKLOADER, ModConfig.WCT_DRAGON_DROPS_BOOSTER);
			ModNetworking.instance().sendTo((WCTPacket) p, (EntityPlayerMP) e.player);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
		if (Mods.BAUBLES.isLoaded() && !BaubleRenderDispatcher.getRegistry().containsKey(event.getRenderer())) {
			event.getRenderer().addLayer(new BaubleRenderDispatcher(event.getRenderer()));
			BaubleRenderDispatcher.getRegistry().put(event.getRenderer(), null);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onkeyTyped(GuiScreenEvent.KeyboardInputEvent.Post e) {
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
	public void onPickup(EntityItemPickupEvent e) {
		if (e.getEntityPlayer() != null && e.getEntityPlayer() instanceof EntityPlayerMP) {
			if (!ModConfig.USE_OLD_INFINTY_MECHANIC && e.getItem().getItem().getItem() == ModItems.BOOSTER_CARD) {
				ItemStack wirelessTerminal = WCTUtils.getWirelessTerm(e.getEntityPlayer().inventory);
				if (!wirelessTerminal.isEmpty() && WCTUtils.shouldConsumeBoosters(wirelessTerminal)) {
					e.setCanceled(true);
					ItemStack boosters = e.getItem().getItem().copy();
					WCTUtils.addInfinityBoosters(wirelessTerminal, boosters);
					ModNetworking.instance().sendTo(new PacketSyncInfinityEnergy(WCTUtils.getInfinityEnergy(wirelessTerminal)), (EntityPlayerMP) e.getEntityPlayer());
					e.getItem().setDead();
				}
			}
		}
	}
}
