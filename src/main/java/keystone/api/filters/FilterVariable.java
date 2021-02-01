package keystone.api.filters;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterVariable
{
    String name() default "";
    float min() default Float.MIN_VALUE;
    float max() default Float.MAX_VALUE;
}
