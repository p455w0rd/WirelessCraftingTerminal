package net.p455w0rd.wirelesscraftingterminal.integration;

public interface IIntegrationModule
{
	void init() throws Throwable;

	void postInit();
}
