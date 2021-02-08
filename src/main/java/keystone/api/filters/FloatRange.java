package keystone.api.filters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply to a Float {@link keystone.api.filters.Variable} in a filter to confine it to a range
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FloatRange
{
    /**
     * @return The minimum value of the {@link keystone.api.filters.Variable}
     */
    float min() default Float.MIN_VALUE;

    /**
     * @return The maximum value of the {@link keystone.api.filters.Variable}
     */
    float max() default Float.MAX_VALUE;

    /**
     * @return How much to change the {@link keystone.api.filters.Variable} by when scrolling
     */
    float scrollStep() default 1.0f;
}
