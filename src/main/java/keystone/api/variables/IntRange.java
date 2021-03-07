package keystone.api.variables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply to an Integer {@link Variable} in a filter to confine it to a range
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntRange
{
    /**
     * @return The minimum value of the {@link Variable}
     */
    int min() default Integer.MIN_VALUE;

    /**
     * @return The maximum value of the {@link Variable}
     */
    int max() default Integer.MAX_VALUE;

    /**
     * @return How much to change the {@link Variable} by when scrolling
     */
    int scrollStep() default 1;
}
