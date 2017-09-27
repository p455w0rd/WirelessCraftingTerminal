package p455w0rd.wct.sync;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import p455w0rd.wct.sync.packets.PacketBaubleSync;
import p455w0rd.wct.sync.packets.PacketConfigSync;
import p455w0rd.wct.sync.packets.PacketCraftRequest;
import p455w0rd.wct.sync.packets.PacketEmptyTrash;
import p455w0rd.wct.sync.packets.PacketInventoryAction;
import p455w0rd.wct.sync.packets.PacketJEIRecipe;
import p455w0rd.wct.sync.packets.PacketMEInventoryUpdate;
import p455w0rd.wct.sync.packets.PacketMagnetFilter;
import p455w0rd.wct.sync.packets.PacketOpenGui;
import p455w0rd.wct.sync.packets.PacketPartialItem;
import p455w0rd.wct.sync.packets.PacketSetJobBytes;
import p455w0rd.wct.sync.packets.PacketSetMagnet;
import p455w0rd.wct.sync.packets.PacketSwapSlots;
import p455w0rd.wct.sync.packets.PacketSwitchGuis;
import p455w0rd.wct.sync.packets.PacketUpdateCPUInfo;
import p455w0rd.wct.sync.packets.PacketValueConfig;

public class WCTPacketHandlerBase {
	private static final Map<Class<? extends WCTPacket>, PacketTypes> REVERSE_LOOKUP = new HashMap<Class<? extends WCTPacket>, WCTPacketHandlerBase.PacketTypes>();

	public enum PacketTypes {
			PACKET_INVENTORY_ACTION(PacketInventoryAction.class),

			PACKET_ME_INVENTORY_UPDATE(PacketMEInventoryUpdate.class),

			PACKET_VALUE_CONFIG(PacketValueConfig.class),

			PACKET_SWITCH_GUIS(PacketSwitchGuis.class),

			PACKET_SWAP_SLOTS(PacketSwapSlots.class),

			PACKET_RECIPE_NEI(PacketJEIRecipe.class),

			PACKET_PARTIAL_ITEM(PacketPartialItem.class),

			PACKET_CRAFTING_REQUEST(PacketCraftRequest.class),

			PACKET_MAGNETFILTER_MODE(PacketMagnetFilter.class),

			PACKET_OPENWIRELESSTERM(PacketOpenGui.class),

			PACKET_SWITCHMAGNETMODE(PacketSetMagnet.class),

			PACKET_EMPTY_TRASH(PacketEmptyTrash.class),

			PACKET_SYNC_CONFIGS(PacketConfigSync.class),

			PACKET_SET_JOB(PacketSetJobBytes.class),

			PACKET_UPDATECPUINFO(PacketUpdateCPUInfo.class),

			PACKET_BAUBLE_SYNC(PacketBaubleSync.class);

		private final Class<? extends WCTPacket> packetClass;
		private final Constructor<? extends WCTPacket> packetConstructor;

		PacketTypes(final Class<? extends WCTPacket> c) {
			packetClass = c;

			Constructor<? extends WCTPacket> x = null;
			try {
				x = packetClass.getConstructor(ByteBuf.class);
			}
			catch (final NoSuchMethodException ignored) {
			}
			catch (final SecurityException ignored) {
			}
			catch (final DecoderException ignored) {
			}

			packetConstructor = x;
			REVERSE_LOOKUP.put(packetClass, this);

			if (packetConstructor == null) {
				throw new IllegalStateException("Invalid Packet Class " + c + ", must be constructable on DataInputStream");
			}
		}

		public static PacketTypes getPacket(final int id) {
			return (values())[id];
		}

		static PacketTypes getID(final Class<? extends WCTPacket> c) {
			return REVERSE_LOOKUP.get(c);
		}

		public WCTPacket parsePacket(final ByteBuf in) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			return packetConstructor.newInstance(in);
		}
	}
}