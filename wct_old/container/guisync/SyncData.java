package p455w0rd.wct.container.guisync;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.container.WCTBaseContainer;
import p455w0rd.wct.sync.network.NetworkHandler;
import p455w0rd.wct.sync.packets.PacketValueConfig;

public class SyncData {
	private final Container source;
	private final Field field;
	private final int channel;
	private Object clientVersion;

	public SyncData(final Container container, final Field field, final GuiSync annotation) {
		if (container instanceof ContainerWCT) {
			source = container;
		}
		else if (container instanceof WCTBaseContainer) {
			source = container;
		}
		else {
			source = container;
		}
		clientVersion = null;
		this.field = field;
		channel = annotation.value();
	}

	public int getChannel() {
		return channel;
	}

	public void tick(final IContainerListener c) {
		try {
			final Object val = field.get(source);
			if (val != null && clientVersion == null) {
				send(c, val);
			}
			else if (!val.equals(clientVersion)) {
				send(c, val);
			}
		}
		catch (final Exception e) {
		}
	}

	@SuppressWarnings("rawtypes")
	private void send(final IContainerListener o, final Object val) throws IOException {
		if (val instanceof String) {
			if (o instanceof EntityPlayerMP) {
				NetworkHandler.instance().sendTo(new PacketValueConfig("SyncDat." + channel, (String) val), (EntityPlayerMP) o);
			}
		}
		else if (field.getType().isEnum()) {
			o.sendWindowProperty(source, channel, ((Enum) val).ordinal());
		}
		else if (val instanceof Long || val.getClass() == long.class) {
			//NetworkHandler.instance.sendTo( new PacketProgressBar( this.channel, (Long) val ), (EntityPlayerMP) o );
		}
		else if (val instanceof Boolean || val.getClass() == boolean.class) {
			o.sendWindowProperty(source, channel, ((Boolean) val) ? 1 : 0);
		}
		else {
			o.sendWindowProperty(source, channel, (Integer) val);
		}

		clientVersion = val;
	}

	public void update(final Object val) {
		try {
			final Object oldValue = field.get(source);
			if (val instanceof String) {
				updateString(oldValue, (String) val);
			}
			else {
				updateValue(oldValue, (Long) val);
			}
		}
		catch (final Exception e) {
		}
	}

	private void updateString(final Object oldValue, final String val) {
		try {
			field.set(source, val);
		}
		catch (final Exception e) {
		}
	}

	@SuppressWarnings({
			"unchecked",
			"rawtypes"
	})
	private void updateValue(final Object oldValue, final long val) {
		try {
			if (field.getType().isEnum()) {
				final EnumSet<? extends Enum> valList = EnumSet.allOf((Class<? extends Enum>) field.getType());
				for (final Enum e : valList) {
					if (e.ordinal() == val) {
						field.set(source, e);
						break;
					}
				}
			}
			else {
				if (field.getType().equals(int.class)) {
					field.set(source, (int) val);
				}
				else if (field.getType().equals(long.class)) {
					field.set(source, val);
				}
				else if (field.getType().equals(boolean.class)) {
					field.set(source, val == 1);
				}
				else if (field.getType().equals(Integer.class)) {
					field.set(source, (int) val);
				}
				else if (field.getType().equals(Long.class)) {
					field.set(source, val);
				}
				else if (field.getType().equals(Boolean.class)) {
					field.set(source, val == 1);
				}
			}

			if (source instanceof ContainerWCT) {
				((ContainerWCT) source).onUpdate(field.getName(), oldValue, field.get(source));
			}
			if (source instanceof WCTBaseContainer) {
				((WCTBaseContainer) source).onUpdate(field.getName(), oldValue, field.get(source));
			}
		}
		catch (final Exception e) {
		}
	}
}
