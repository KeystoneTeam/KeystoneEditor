package keystone.api.variables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply to a boolean field to tell the Editor GUI that this is a dirty flag.
 * Setting this field to true will cause the Editor GUI to refresh itself.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditorDirtyFlag
{
}
