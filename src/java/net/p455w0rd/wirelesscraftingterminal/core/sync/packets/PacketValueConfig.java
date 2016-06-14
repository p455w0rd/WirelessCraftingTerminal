package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.IMouseWheelItem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftingCPU;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftingCPU;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftingStatus;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.WCTBaseContainer;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;

public class PacketValueConfig extends WCTPacket {

	private final String Name;
	private final String Value;

	// automatic.
	public PacketValueConfig(final ByteBuf stream) throws IOException {
		final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(stream.array(), stream.readerIndex(), stream.readableBytes()));
		this.Name = dis.readUTF();
		this.Value = dis.readUTF();
	}

	// api
	public PacketValueConfig(final String name, final String value) throws IOException {
		this.Name = name;
		this.Value = value;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt(this.getPacketID());
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final DataOutputStream dos = new DataOutputStream(bos);
		dos.writeUTF(name);
		dos.writeUTF(value);
		data.writeBytes(bos.toByteArray());
		this.configureWrite(data);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		final Container c = player.openContainer;

		if (this.Name.equals("Item") && RandomUtils.getWirelessTerm(player.inventory) != null && RandomUtils.getWirelessTerm(player.inventory).getItem() instanceof IMouseWheelItem) {
			final ItemStack is = RandomUtils.getWirelessTerm(player.inventory);
			final IMouseWheelItem si = (IMouseWheelItem) is.getItem();
			si.onWheel(is, this.Value.equals("WheelUp"));
		}
		else if (this.Name.equals("Terminal.Cpu") && c instanceof ContainerCraftingStatus) {
			final ContainerCraftingStatus qk = (ContainerCraftingStatus) c;
			qk.cycleCpu(this.Value.equals("Next"));
		}
		else if (this.Name.equals("Terminal.Cpu") && c instanceof ContainerCraftConfirm) {
			final ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
			qk.cycleCpu(this.Value.equals("Next"));
		}
		else if (this.Name.equals("Terminal.Start") && c instanceof ContainerCraftConfirm) {
			final ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
			qk.startJob();
		}
		else if (this.Name.equals("TileCrafting.Cancel") && c instanceof ContainerCraftingCPU) {
			final ContainerCraftingCPU qk = (ContainerCraftingCPU) c;
			qk.cancelCrafting();
		}
		else if (c instanceof IConfigurableObject) {
			final IConfigManager cm = ((IConfigurableObject) c).getConfigManager();

			for (final Settings e : cm.getSettings()) {
				if (e.name().equals(this.Name)) {
					final Enum<?> def = cm.getSetting(e);

					try {
						cm.putSetting(e, Enum.valueOf(def.getClass(), this.Value));
					}
					catch (final IllegalArgumentException err) {
						// :P
					}

					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		final Container c = player.openContainer;

		if (this.Name.equals("CustomName")) {
			if (c instanceof ContainerWirelessCraftingTerminal) {
				((ContainerWirelessCraftingTerminal) c).setCustomName(this.Value);
			}
			if (c instanceof WCTBaseContainer) {
				((WCTBaseContainer) c).setCustomName(this.Value);
			}
		}
		else if (this.Name.startsWith("SyncDat.")) {
			if (c instanceof ContainerWirelessCraftingTerminal) {
				((ContainerWirelessCraftingTerminal) c).stringSync(Integer.parseInt(this.Name.substring(8)), this.Value);
			}
			if (c instanceof WCTBaseContainer) {
				((WCTBaseContainer) c).stringSync(Integer.parseInt(this.Name.substring(8)), this.Value);
			}
		}
		else if (this.Name.equals("CraftingStatus") && this.Value.equals("Clear")) {
			final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
			if (gs instanceof GuiCraftingCPU) {
				((GuiCraftingCPU) gs).clearItems();
			}
		}
		else if (c instanceof IConfigurableObject) {
			final IConfigManager cm = ((IConfigurableObject) c).getConfigManager();

			for (final Settings e : cm.getSettings()) {
				if (e.name().equals(this.Name)) {
					final Enum<?> def = cm.getSetting(e);

					try {
						cm.putSetting(e, Enum.valueOf(def.getClass(), this.Value));
					}
					catch (final IllegalArgumentException err) {
						// :P
					}
					break;
				}
			}
		}
	}
}