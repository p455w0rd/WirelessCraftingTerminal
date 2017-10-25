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
package p455w0rd.wct.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerCraftingStatus extends ContainerCraftingCPU {

	private final List<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();
	@GuiSync(5)
	public int selectedCpu = -1;
	@GuiSync(6)
	public boolean noCPU = true;
	@GuiSync(7)
	public String myName = "";

	public ContainerCraftingStatus(final InventoryPlayer ip, final ITerminalHost te) {
		super(ip, te);
	}

	/*
		@Override
		public void detectAndSendChanges() {
			final ICraftingGrid cc = getNetwork().getCache(ICraftingGrid.class);
			final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

			int matches = 0;
			boolean changed = false;
			for (final ICraftingCPU c : cpuSet) {
				boolean found = false;
				for (final CraftingCPURecord ccr : cpus) {
					if (ccr.getCpu() == c) {
						found = true;
					}
				}

				final boolean matched = cpuMatches(c);

				if (matched) {
					matches++;
				}

				if (found == !matched) {
					changed = true;
				}
			}

			if (changed || cpus.size() != matches) {
				cpus.clear();
				for (final ICraftingCPU c : cpuSet) {
					if (cpuMatches(c)) {
						cpus.add(new CraftingCPURecord(c.getAvailableStorage(), c.getCoProcessors(), c));
					}
				}

				sendCPUs();
			}

			noCPU = cpus.isEmpty();

			super.detectAndSendChanges();
		}
	*/
	@Override
	public void detectAndSendChanges() {
		if (Platform.isServer() && getNetwork() != null) {
			final ICraftingGrid cc = getNetwork().getCache(ICraftingGrid.class);
			final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

			int matches = 0;
			boolean changed = false;
			for (final ICraftingCPU c : cpuSet) {
				boolean found = false;
				for (final CraftingCPURecord ccr : cpus) {
					if (ccr.getCpu() == c) {
						found = true;
					}
				}

				final boolean matched = cpuMatches(c);

				if (matched) {
					matches++;
				}

				if (found == !matched) {
					changed = true;
				}
			}

			if (changed || cpus.size() != matches) {
				cpus.clear();
				for (final ICraftingCPU c : cpuSet) {
					if (cpuMatches(c)) {
						cpus.add(new CraftingCPURecord(c.getAvailableStorage(), c.getCoProcessors(), c));
					}
				}

				sendCPUs();
			}

			noCPU = cpus.isEmpty();
		}

		super.detectAndSendChanges();
	}

	private boolean cpuMatches(final ICraftingCPU c) {
		return c.isBusy();
	}

	private void sendCPUs() {
		Collections.sort(cpus);

		if (selectedCpu >= cpus.size()) {
			selectedCpu = -1;
			myName = "";
		}
		else if (selectedCpu != -1) {
			myName = cpus.get(selectedCpu).getName();
		}

		if (selectedCpu == -1 && cpus.size() > 0) {
			selectedCpu = 0;
		}

		if (selectedCpu != -1) {
			if (cpus.get(selectedCpu).getCpu() != getMonitor()) {
				setCPU(cpus.get(selectedCpu).getCpu());
			}
		}
		else {
			setCPU(null);
		}
	}

	public void cycleCpu(final boolean next) {
		if (next) {
			selectedCpu++;
		}
		else {
			selectedCpu--;
		}

		if (selectedCpu < -1) {
			selectedCpu = cpus.size() - 1;
		}
		else if (selectedCpu >= cpus.size()) {
			selectedCpu = -1;
		}

		if (selectedCpu == -1 && cpus.size() > 0) {
			selectedCpu = 0;
		}

		if (selectedCpu == -1) {
			myName = "";
			setCPU(null);
		}
		else {
			myName = cpus.get(selectedCpu).getName();
			setCPU(cpus.get(selectedCpu).getCpu());
		}
	}
}
