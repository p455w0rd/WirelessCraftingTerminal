package p455w0rd.wct.container;

import javax.annotation.Nonnull;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.util.ItemSorters;

public class CraftingCPURecord implements Comparable<CraftingCPURecord> {

	private final String myName;
	private final ICraftingCPU cpu;
	private final long size;
	private final int processors;

	public CraftingCPURecord(final long size, final int coProcessors, final ICraftingCPU server) {
		this.size = size;
		processors = coProcessors;
		cpu = server;
		myName = server.getName();
	}

	@Override
	public int compareTo(@Nonnull final CraftingCPURecord o) {
		final int a = ItemSorters.compareLong(o.getProcessors(), getProcessors());
		if (a != 0) {
			return a;
		}
		return ItemSorters.compareLong(o.getSize(), getSize());
	}

	public ICraftingCPU getCpu() {
		return cpu;
	}

	String getName() {
		return myName;
	}

	int getProcessors() {
		return processors;
	}

	long getSize() {
		return size;
	}
}