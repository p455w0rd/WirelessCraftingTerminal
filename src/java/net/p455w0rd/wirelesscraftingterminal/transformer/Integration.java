package net.p455w0rd.wirelesscraftingterminal.transformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationType;

public @interface Integration
{
	@Retention( RetentionPolicy.RUNTIME )
	@Target( ElementType.TYPE )
	@interface InterfaceList
	{
		Interface[] value();
	}

	@Retention( RetentionPolicy.RUNTIME )
	@Target( ElementType.TYPE )
	@interface Interface
	{
		String iface();

		IntegrationType iname();
	}

	@Retention( RetentionPolicy.RUNTIME )
	@Target( ElementType.METHOD )
	@interface Method
	{
		IntegrationType iname();
	}
}