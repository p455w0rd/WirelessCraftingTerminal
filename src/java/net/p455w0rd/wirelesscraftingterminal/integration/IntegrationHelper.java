package net.p455w0rd.wirelesscraftingterminal.integration;

public class IntegrationHelper
{

	public static void testClassExistence( final Object o, final Class<?> clz )
	{
		clz.isInstance( o );
	}
}
