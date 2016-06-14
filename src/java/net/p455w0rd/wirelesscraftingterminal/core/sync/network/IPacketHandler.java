package net.p455w0rd.wirelesscraftingterminal.core.sync.network;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import net.minecraft.entity.player.EntityPlayer;


public interface IPacketHandler
{

	void onPacketData( INetworkInfo manager, FMLProxyPacket packet, EntityPlayer player );
}