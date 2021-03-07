package keystone.api.variables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply to a {@link keystone.api.variables.Variable} to run a method when its value is changed
 * through an editor widget. The method signature must have a no parameters
 * as the field this is annotating
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hook
{
    /**
     * @return The name of the method to run
     */
    String value();
}
