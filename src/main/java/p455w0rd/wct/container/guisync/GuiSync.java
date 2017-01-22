package p455w0rd.wct.container.guisync;

import java.lang.annotation.*;

/**
 * Annotates that this field should be synchronized between the server and client.
 * Requires the field to be public.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GuiSync {

	int value();
}
