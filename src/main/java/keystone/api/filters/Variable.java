package keystone.api.filters;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Variable
{
    String value() default "";
}
