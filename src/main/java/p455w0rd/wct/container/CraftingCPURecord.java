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

import javax.annotation.Nonnull;

import appeng.api.networking.crafting.ICraftingCPU;

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
		final int a = Long.compare(o.getProcessors(), getProcessors());
		if (a != 0) {
			return a;
		}
		return Long.compare(o.getSize(), getSize());
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