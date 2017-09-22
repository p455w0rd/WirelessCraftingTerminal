package p455w0rd.wct.client.me;

import javax.annotation.Nonnull;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ItemSorters;
import net.minecraft.client.resources.I18n;

public class ClientDCInternalInv implements Comparable<ClientDCInternalInv> {

	private final String unlocalizedName;
	private final AppEngInternalInventory inventory;

	private final long id;
	private final long sortBy;

	public ClientDCInternalInv(final int size, final long id, final long sortBy, final String unlocalizedName) {
		inventory = new AppEngInternalInventory(null, size);
		this.unlocalizedName = unlocalizedName;
		this.id = id;
		this.sortBy = sortBy;
	}

	public String getName() {
		final String s = I18n.format(unlocalizedName + ".name");
		if (s.equals(unlocalizedName + ".name")) {
			return I18n.format(unlocalizedName);
		}
		return s;
	}

	@Override
	public int compareTo(@Nonnull final ClientDCInternalInv o) {
		return ItemSorters.compareLong(sortBy, o.sortBy);
	}

	public AppEngInternalInventory getInventory() {
		return inventory;
	}

	public long getId() {
		return id;
	}
}