package keystone.api.variables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When added to a field annotated with {@link Variable}, the editor widget will
 * have the displayed value modified by the functions provided in this annotation
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisplayModifiers
{
    /**
     * @return The value to scale the display by. For example, 20 can be used
     * for a rate of change processed every tick so the editor shows the rate
     * per second instead
     */
    float numberScale();
    
    /**
     * @return If greater than or equal to zero, the display will only show
     * up to the given number of decimal points
     */
    int decimalPoints() default 3;
}
