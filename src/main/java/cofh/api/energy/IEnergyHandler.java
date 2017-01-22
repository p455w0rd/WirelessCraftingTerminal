package cofh.api.energy;

import net.minecraft.util.EnumFacing;

public abstract interface IEnergyHandler extends IEnergyConnection {
	public abstract int getEnergyStored(EnumFacing paramEnumFacing);

	public abstract int getMaxEnergyStored(EnumFacing paramEnumFacing);
}
