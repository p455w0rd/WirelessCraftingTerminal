package net.p455w0rd.wirelesscraftingterminal.core.sync.network;

import java.lang.reflect.InvocationTargetException;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacketHandlerBase;

public class WCTClientPacketHandler extends WCTPacketHandlerBase implements IPacketHandler
{

	@Override
	public void onPacketData( final INetworkInfo network, final FMLProxyPacket packet, EntityPlayer player )
	{
		final ByteBuf stream = packet.payload();

		player = Minecraft.getMinecraft().thePlayer;

		try
		{
			final int packetType = stream.readInt();
			final WCTPacket pack = PacketTypes.getPacket( packetType ).parsePacket( stream );
			pack.clientPacketData( network, pack, player );
		}
		catch( final InstantiationException e )
		{
			WCTLog.debug( e.getMessage() );
		}
		catch( final IllegalAccessException e )
		{
			WCTLog.debug( e.getMessage() );
		}
		catch( final IllegalArgumentException e )
		{
			WCTLog.debug( e.getMessage() );
		}
		catch( final InvocationTargetException e )
		{
			WCTLog.debug( e.getMessage() );
		}
	}
}
