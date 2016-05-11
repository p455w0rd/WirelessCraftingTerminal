package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;
import net.p455w0rd.wirelesscraftingterminal.handlers.ConfigHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.RecipeHandler;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class PacketConfigSync extends WCTPacket {
	
	int wirelessTermMaxPower, boosterDropChance;
	boolean easyMode, boosterEnabled, mineTweakerOverride, oldMT, mtChanged = false;

	public PacketConfigSync(final ByteBuf stream) {
		this.oldMT = Reference.WCT_MINETWEAKER_OVERRIDE;
		this.wirelessTermMaxPower = stream.readInt();
		this.easyMode = stream.readBoolean();
		this.boosterEnabled = stream.readBoolean();
		this.boosterDropChance = stream.readInt();
		this.mineTweakerOverride = stream.readBoolean();
	}

	// api
	public PacketConfigSync(int power, boolean mode, boolean booster, int dropChance, boolean mtOverride) {
		this.oldMT = Reference.WCT_MINETWEAKER_OVERRIDE;
		this.wirelessTermMaxPower = power;
		this.easyMode = mode;
		this.boosterEnabled = booster;
		this.boosterDropChance = dropChance;
		this.mineTweakerOverride = mtOverride;
		
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(this.getPacketID());
		data.writeInt(this.wirelessTermMaxPower);
		data.writeBoolean(this.easyMode);
		data.writeBoolean(this.boosterEnabled);
		data.writeInt(this.boosterDropChance);
		data.writeBoolean(this.mineTweakerOverride);
		this.configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		if (oldMT != this.mineTweakerOverride) {
			mtChanged = true;
		}
		Reference.WCT_MAX_POWER = this.wirelessTermMaxPower;
		Reference.WCT_EASYMODE_ENABLED = this.easyMode;
		Reference.WCT_BOOSTER_ENABLED = this.boosterEnabled;
		Reference.WCT_BOOSTER_DROPCHANCE = this.boosterDropChance;
		Reference.WCT_MINETWEAKER_OVERRIDE = this.mineTweakerOverride;
		ConfigHandler.removeBooster();
		ConfigHandler.removeBoosterIcon();
		RecipeHandler.loadRecipes(mtChanged);
		mtChanged = false;
	}
	
}
