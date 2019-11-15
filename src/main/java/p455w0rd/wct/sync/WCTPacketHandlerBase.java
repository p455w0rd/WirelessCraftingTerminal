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
import p455w0rd.wct.sync.packets.*;

public class WCTPacketHandlerBase {
	private static final Map<Class<? extends WCTPacket>, PacketTypes> REVERSE_LOOKUP = new HashMap<>();

	public enum PacketTypes {
			PACKET_INVENTORY_ACTION(PacketInventoryAction.class),

			PACKET_ME_INVENTORY_UPDATE(PacketMEInventoryUpdate.class),

			PACKET_VALUE_CONFIG(PacketValueConfig.class),

			PACKET_SWITCH_GUIS(PacketSwitchGuis.class),

			PACKET_SWAP_SLOTS(PacketSwapSlots.class),

			PACKET_RECIPE_NEI(PacketJEIRecipe.class),

			PACKET_TARGET_ITEM(PacketTargetItemStack.class),

			PACKET_CRAFTING_REQUEST(PacketCraftRequest.class),

			PACKET_MAGNETFILTER_MODE_HELD(PacketMagnetFilterHeld.class),

			PACKET_MAGNETFILTER_MODE_WCT(PacketMagnetFilterWCT.class),

			PACKET_OPENGUI(PacketOpenGui.class),

			PACKET_SWITCHMAGNETMODE_HELD(PacketSetMagnetHeld.class),

			PACKET_SWITCHMAGNETMODE_WCT(PacketSetMagnetWCT.class),

			PACKET_CYCLEMAGNETMODE_KEYBIND(PacketCycleMagnetKeybind.class),

			PACKET_SHIFTCRAFT(PacketSetShiftCraft.class);

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
			return values()[id];
		}

		static PacketTypes getID(final Class<? extends WCTPacket> c) {
			return REVERSE_LOOKUP.get(c);
		}

		public WCTPacket parsePacket(final ByteBuf in) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			return packetConstructor.newInstance(in);
		}
	}
}