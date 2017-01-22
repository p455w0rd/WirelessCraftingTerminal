package p455w0rd.wct.sync.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public interface IPacketHandler {

	void onPacketData(INetworkInfo manager, INetHandler handler, FMLProxyPacket packet, EntityPlayer player);
}