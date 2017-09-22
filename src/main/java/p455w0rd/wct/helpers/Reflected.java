package p455w0rd.wct.helpers;

import java.lang.annotation.*;

/**
 * Marker interface to help identify invocation of reflection
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
		ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD
})
public @interface Reflected {

}
