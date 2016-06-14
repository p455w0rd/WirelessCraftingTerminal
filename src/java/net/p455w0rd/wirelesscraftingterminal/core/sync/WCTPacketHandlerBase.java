package net.p455w0rd.wirelesscraftingterminal.core.sync;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketConfigSync;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketCraftRequest;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketEmptyTrash;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketInventoryAction;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketMEInventoryUpdate;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketMagnetFilter;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketNEIRecipe;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketOpenGui;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketPartialItem;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSetJobBytes;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSetMagnet;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwapSlots;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketUpdateCPUInfo;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketValueConfig;

public class WCTPacketHandlerBase {
	private static final Map<Class<? extends WCTPacket>, PacketTypes> REVERSE_LOOKUP = new HashMap<Class<? extends WCTPacket>, WCTPacketHandlerBase.PacketTypes>();

	public enum PacketTypes {
		PACKET_INVENTORY_ACTION(PacketInventoryAction.class),

		PACKET_ME_INVENTORY_UPDATE(PacketMEInventoryUpdate.class),

		PACKET_VALUE_CONFIG(PacketValueConfig.class),

		PACKET_SWITCH_GUIS(PacketSwitchGuis.class),

		PACKET_SWAP_SLOTS(PacketSwapSlots.class),

		PACKET_RECIPE_NEI(PacketNEIRecipe.class),

		PACKET_PARTIAL_ITEM(PacketPartialItem.class),

		PACKET_CRAFTING_REQUEST(PacketCraftRequest.class),

		PACKET_MAGNETFILTER_MODE(PacketMagnetFilter.class),

		PACKET_OPENWIRELESSTERM(PacketOpenGui.class),
		
		PACKET_SWITCHMAGNETMODE(PacketSetMagnet.class),
		
		PACKET_EMPTY_TRASH(PacketEmptyTrash.class),
		
		PACKET_SYNC_CONFIGS(PacketConfigSync.class),
		
		PACKET_SET_JOB(PacketSetJobBytes.class),
		
		PACKET_UPDATECPUINFO(PacketUpdateCPUInfo.class);

		private final Class<? extends WCTPacket> packetClass;
		private final Constructor<? extends WCTPacket> packetConstructor;

		PacketTypes(final Class<? extends WCTPacket> c) {
			this.packetClass = c;

			Constructor<? extends WCTPacket> x = null;
			try {
				x = this.packetClass.getConstructor(ByteBuf.class);
			}
			catch (final NoSuchMethodException ignored) {
			}
			catch (final SecurityException ignored) {
			}
			catch (final DecoderException ignored) {
			}

			this.packetConstructor = x;
			REVERSE_LOOKUP.put(this.packetClass, this);

			if (this.packetConstructor == null) {
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
			return this.packetConstructor.newInstance(in);
		}
	}
}