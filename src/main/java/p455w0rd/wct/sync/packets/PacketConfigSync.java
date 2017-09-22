package p455w0rd.wct.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

public class PacketConfigSync extends WCTPacket {

	int wirelessTermMaxPower;
	boolean boosterEnabled, mineTweakerOverride, enableChunkLoading, dragonDropsBooster;

	public PacketConfigSync(final ByteBuf stream) {
		wirelessTermMaxPower = stream.readInt();
		boosterEnabled = stream.readBoolean();
		//boosterDropChance = stream.readInt();
		mineTweakerOverride = stream.readBoolean();
		enableChunkLoading = stream.readBoolean();
		dragonDropsBooster = stream.readBoolean();
	}

	// api
	public PacketConfigSync(int power, boolean booster, boolean mtOverride, boolean chunkLoad, boolean dragonDrops) {
		wirelessTermMaxPower = power;
		boosterEnabled = booster;
		//boosterDropChance = dropChance;
		mineTweakerOverride = mtOverride;
		enableChunkLoading = chunkLoad;
		dragonDropsBooster = dragonDrops;

		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(wirelessTermMaxPower);
		data.writeBoolean(boosterEnabled);
		//data.writeInt(boosterDropChance);
		data.writeBoolean(mineTweakerOverride);
		data.writeBoolean(enableChunkLoading);
		data.writeBoolean(dragonDropsBooster);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {

	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		ModConfig.WCT_MAX_POWER = wirelessTermMaxPower;
		ModConfig.WCT_BOOSTER_ENABLED = boosterEnabled;
		ModConfig.WCT_MINETWEAKER_OVERRIDE = mineTweakerOverride;
		//ModConfig.WCT_BOOSTER_DROPCHANCE = boosterDropChance;
		ModConfig.WCT_ENABLE_CONTROLLER_CHUNKLOADER = enableChunkLoading;
		ModConfig.WCT_DRAGON_DROPS_BOOSTER = dragonDropsBooster;
	}

}
