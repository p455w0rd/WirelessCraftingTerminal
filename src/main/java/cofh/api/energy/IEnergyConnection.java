package cofh.api.energy;

import net.minecraft.util.EnumFacing;

public abstract interface IEnergyConnection {
	public abstract boolean canConnectEnergy(EnumFacing paramEnumFacing);
}
