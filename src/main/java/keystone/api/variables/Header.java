package keystone.api.variables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add a header widget above this field in a field widget list.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header
{
    /**
     * @return The name of the header. This will be used in the header separator widget
     * in the field widget list.
     */
    String value();
}
