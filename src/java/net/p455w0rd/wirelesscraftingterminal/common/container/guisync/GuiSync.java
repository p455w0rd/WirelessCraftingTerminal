package net.p455w0rd.wirelesscraftingterminal.common.container.guisync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotates that this field should be synchronized between the server and client.
 * Requires the field to be public.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface GuiSync
{

	int value();
}
