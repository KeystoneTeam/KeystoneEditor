package keystone.api.variables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When added to a field annotated with {@link Variable}, the editor widget will
 * have the displayed value scaled by a given value. This does not affect the
 * value stored within the field itself, only the widget display
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisplayScale
{
    /**
     * @return The value to scale the display by. For example, 20 can be used
     * for a rate of change processed every tick so the editor shows the rate
     * per second instead
     */
    float value();
}
