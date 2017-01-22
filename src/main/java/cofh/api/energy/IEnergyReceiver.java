package cofh.api.energy;

import net.minecraft.util.EnumFacing;

public abstract interface IEnergyReceiver extends IEnergyHandler {
	public abstract int receiveEnergy(EnumFacing paramEnumFacing, int paramInt, boolean paramBoolean);
}
