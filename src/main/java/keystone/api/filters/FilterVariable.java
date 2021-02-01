package keystone.api.filters;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterVariable
{
    String value() default "";
}
