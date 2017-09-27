package p455w0rd.wct.sync.packets;

import baubles.api.cap.IBaublesItemHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import p455w0rd.wct.integration.Baubles;
import p455w0rd.wct.sync.WCTPacket;
import p455w0rd.wct.sync.network.INetworkInfo;

/**
 * @author p455w0rd
 *
 */
public class PacketBaubleSync extends WCTPacket {

	int slot;
	ItemStack stack;

	public PacketBaubleSync(final ByteBuf stream) {
		slot = stream.readInt();
		stack = ByteBufUtils.readItemStack(stream);
	}

	// api
	public PacketBaubleSync(int slotIn, ItemStack stackIn) {
		slot = slotIn;
		stack = stackIn;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(slot);
		ByteBufUtils.writeItemStack(data, stack);
		configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer playerIn) {
		IBaublesItemHandler baubles = Baubles.getBaubles(playerIn);
		Minecraft.getMinecraft().addScheduledTask(() -> {
			World world = playerIn.getEntityWorld();
			if (world == null) {
				return;
			}
			baubles.setStackInSlot(slot, stack != null ? stack.copy() : null);
		});
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		IBaublesItemHandler baubles = Baubles.getBaubles(player);
		Minecraft.getMinecraft().addScheduledTask(() -> {
			World world = player.getEntityWorld();
			if (world == null) {
				return;
			}
			baubles.setStackInSlot(slot, stack != null ? stack.copy() : null);
		});
	}

}
