package net.p455w0rd.wirelesscraftingterminal.integration.modules.helpers;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyReceiver;


public class NullRFHandler implements IEnergyReceiver
{

	@Override
	public int receiveEnergy( final ForgeDirection from, final int maxReceive, final boolean simulate )
	{
		return 0;
	}

	@Override
	public int getEnergyStored( final ForgeDirection from )
	{
		return 0;
	}

	@Override
	public int getMaxEnergyStored( final ForgeDirection from )
	{
		return 0;
	}

	@Override
	public boolean canConnectEnergy( final ForgeDirection from )
	{
		return true;
	}
}
