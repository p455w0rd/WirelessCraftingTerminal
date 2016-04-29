package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;
import net.p455w0rd.wirelesscraftingterminal.handlers.ConfigHandler;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class PacketConfigSync extends WCTPacket {
	
	int wirelessTermMaxPower;
	boolean easyMode, boosterEnabled;

	public PacketConfigSync(final ByteBuf stream) {
		this.wirelessTermMaxPower = stream.readInt();
		this.easyMode = stream.readBoolean();
		this.boosterEnabled = stream.readBoolean();
	}

	// api
	public PacketConfigSync(int power, boolean mode, boolean booster) {
		this.wirelessTermMaxPower = power;
		this.easyMode = mode;
		this.boosterEnabled = booster;
		
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(this.getPacketID());
		data.writeInt(this.wirelessTermMaxPower);
		data.writeBoolean(this.easyMode);
		data.writeBoolean(this.boosterEnabled);
		this.configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		Reference.WCT_MAX_POWER = this.wirelessTermMaxPower;
		Reference.WCT_EASYMODE_ENABLED = this.easyMode;
		Reference.WCT_BOOSTER_ENABLED = this.boosterEnabled;
		ConfigHandler.removeBooster();
		ConfigHandler.removeBoosterIcon();
		ConfigHandler.reloadRecipes();
	}
	
}
