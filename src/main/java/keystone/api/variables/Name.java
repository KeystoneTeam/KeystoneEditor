package keystone.api.variables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply to a {@link java.lang.reflect.Field} or {@link java.lang.Enum} value to assign it a
 * custom name in the filter selection panel
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Name
{
    /**
     * @return The name of the {@link java.lang.reflect.Field} or {@link java.lang.Enum} value
     */
    String value();
}
