package keystone.api.variables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply to a filter {@link Variable} to assign it a custom tooltip in the filter selection panel
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tooltip
{
    /**
     * @return The tooltip text of the {@link Variable}
     */
    String value();
}
