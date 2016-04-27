package net.p455w0rd.wirelesscraftingterminal.core.crash;

import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationRegistry;

public class IntegrationCrashEnhancement extends BaseCrashEnhancement
{
	public IntegrationCrashEnhancement()
	{
		super( "AE2 Integration", IntegrationRegistry.INSTANCE.getStatus() );
	}
}
