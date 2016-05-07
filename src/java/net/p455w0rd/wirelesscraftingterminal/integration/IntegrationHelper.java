package net.p455w0rd.wirelesscraftingterminal.integration;

public class IntegrationHelper
{

	public static void testClassExistence( final Object obj, final Class<?> clazz )
	{
		clazz.isInstance(obj);
	}
}
