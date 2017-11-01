/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.sync;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import p455w0rd.wct.sync.packets.PacketConfigSync;
import p455w0rd.wct.sync.packets.PacketCraftRequest;
import p455w0rd.wct.sync.packets.PacketEmptyTrash;
import p455w0rd.wct.sync.packets.PacketInventoryAction;
import p455w0rd.wct.sync.packets.PacketJEIRecipe;
import p455w0rd.wct.sync.packets.PacketMEInventoryUpdate;
import p455w0rd.wct.sync.packets.PacketMagnetFilter;
import p455w0rd.wct.sync.packets.PacketOpenGui;
import p455w0rd.wct.sync.packets.PacketSetInRange;
import p455w0rd.wct.sync.packets.PacketSetMagnet;
import p455w0rd.wct.sync.packets.PacketSwapSlots;
import p455w0rd.wct.sync.packets.PacketSwitchGuis;
import p455w0rd.wct.sync.packets.PacketSyncInfinityEnergy;
import p455w0rd.wct.sync.packets.PacketTargetItemStack;
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

			PACKET_TARGET_ITEM(PacketTargetItemStack.class),

			PACKET_CRAFTING_REQUEST(PacketCraftRequest.class),

			PACKET_MAGNETFILTER_MODE(PacketMagnetFilter.class),

			PACKET_OPENWIRELESSTERM(PacketOpenGui.class),

			PACKET_SWITCHMAGNETMODE(PacketSetMagnet.class),

			PACKET_EMPTY_TRASH(PacketEmptyTrash.class),

			PACKET_SYNC_CONFIGS(PacketConfigSync.class),

			PACKET_SYNC_INFINITY_ENERGY(PacketSyncInfinityEnergy.class),

			PACKET_SET_IN_RANGE(PacketSetInRange.class);

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