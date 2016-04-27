package net.p455w0rd.wirelesscraftingterminal.core.crash;

import cpw.mods.fml.common.ICrashCallable;


abstract class BaseCrashEnhancement implements ICrashCallable
{
	private final String name;
	private final String value;

	public BaseCrashEnhancement( final String name, final String value )
	{
		this.name = name;
		this.value = value;
	}

	@Override
	public final String call() throws Exception
	{
		return this.value;
	}

	@Override
	public final String getLabel()
	{
		return this.name;
	}
}
