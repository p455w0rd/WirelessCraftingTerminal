package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.api.WCTApi;
import net.p455w0rd.wirelesscraftingterminal.common.WirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketOpenWirelessTerm extends WCTPacket {

	// automatic.
	public PacketOpenWirelessTerm(final ByteBuf stream) {

	}
	
	public PacketOpenWirelessTerm() {
		if( Platform.isClient() ) {
			EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
			//WCTApi.instance().interact().openWirelessCraftingTerminalGui(player);
		}
		final ByteBuf data = Unpooled.buffer();
		data.writeInt( this.getPacketID() );
		this.configureWrite( data );
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		World world = player.worldObj;
		int x = (int) player.posX;
		int y = (int) player.posY;
		int z = (int) player.posZ;
		//WCTGuiHandler.launchGui(WirelessCraftingTerminal.GUI_WCT, player, world, x, y, z);
		if (player.openContainer instanceof ContainerPlayer) {
			//WCTApi.instance().interact().openWirelessCraftingTerminalGui(player);
			player.openGui(WirelessCraftingTerminal.INSTANCE, WirelessCraftingTerminal.GUI_WCT, world, x, y, z);
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		World world = player.worldObj;
		int x = (int) player.posX;
		int y = (int) player.posY;
		int z = (int) player.posZ;
		//WCTGuiHandler.launchGui(WirelessCraftingTerminal.GUI_WCT, player, world, x, y, z);
		//WCTApi.instance().interact().openWirelessCraftingTerminalGui(player);
		player.openGui(WirelessCraftingTerminal.INSTANCE, WirelessCraftingTerminal.GUI_WCT, world, x, y, z);
	}
}
