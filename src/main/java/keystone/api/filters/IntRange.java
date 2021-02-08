package keystone.api.filters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply to an Integer {@link keystone.api.filters.Variable} in a filter to confine it to a range
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntRange
{
    /**
     * @return The minimum value of the {@link keystone.api.filters.Variable}
     */
    int min() default Integer.MIN_VALUE;

    /**
     * @return The maximum value of the {@link keystone.api.filters.Variable}
     */
    int max() default Integer.MAX_VALUE;

    /**
     * @return How much to change the {@link keystone.api.filters.Variable} by when scrolling
     */
    int scrollStep() default 1;
}
