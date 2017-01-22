package p455w0rd.wct.sync.packets;

import io.netty.buffer.*;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.wct.init.ModConfig;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

public class PacketConfigSync extends WCTPacket {

	int wirelessTermMaxPower, boosterDropChance;
	boolean easyMode, boosterEnabled, mineTweakerOverride, oldMT, mtChanged = false;

	public PacketConfigSync(final ByteBuf stream) {
		oldMT = ModConfig.WCT_MINETWEAKER_OVERRIDE;
		wirelessTermMaxPower = stream.readInt();
		easyMode = stream.readBoolean();
		boosterEnabled = stream.readBoolean();
		boosterDropChance = stream.readInt();
		mineTweakerOverride = stream.readBoolean();
	}

	// api
	public PacketConfigSync(int power, boolean mode, boolean booster, int dropChance, boolean mtOverride) {
		oldMT = ModConfig.WCT_MINETWEAKER_OVERRIDE;
		wirelessTermMaxPower = power;
		easyMode = mode;
		boosterEnabled = booster;
		boosterDropChance = dropChance;
		mineTweakerOverride = mtOverride;

		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(wirelessTermMaxPower);
		data.writeBoolean(easyMode);
		data.writeBoolean(boosterEnabled);
		data.writeInt(boosterDropChance);
		data.writeBoolean(mineTweakerOverride);
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
	}

}
