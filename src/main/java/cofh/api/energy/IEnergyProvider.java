package cofh.api.energy;

import net.minecraft.util.EnumFacing;

public abstract interface IEnergyProvider extends IEnergyHandler {
	public abstract int extractEnergy(EnumFacing paramEnumFacing, int paramInt, boolean paramBoolean);
}
