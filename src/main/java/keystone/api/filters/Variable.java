package keystone.api.filters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply to a field in a {@link keystone.api.filters.KeystoneFilter KeystoneFilter} to make it show up in
 * the filter selection panel. Supported types are int, float, boolean, enum, String, {@link keystone.api.wrappers.BlockMask BlockMask},
 * and {@link keystone.api.wrappers.BlockPalette BlockPalette}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Variable
{
    /**
     * @return The name of the filter variable. Defaults to the space-seperated title case of the field
     */
    String value() default "";
}
