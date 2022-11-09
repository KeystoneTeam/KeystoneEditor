package keystone.core.utils;

import keystone.api.utils.StringUtils;
import keystone.api.variables.Hook;
import keystone.api.variables.Name;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.core.gui.IKeystoneTooltip;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class AnnotationUtils
{
    public static String getFieldName(Variable variable, Field field)
    {
        Name nameAnnotation = field.getAnnotation(Name.class);
        String variableName = StringUtils.addSpacesToSentence(StringUtils.titleCase(field.getName().trim()));
        if (!variable.value().trim().isEmpty()) variableName = variable.value().trim();
        if (nameAnnotation != null) variableName = nameAnnotation.value().trim();
        return variableName;
    }
    public static IKeystoneTooltip getFieldTooltip(Screen screen, Field field)
    {
        Tooltip tooltip = field.getAnnotation(Tooltip.class);
        if (tooltip != null)
        {
            List<Text> builtTooltip = List.of(Text.of(tooltip.value()));
            return (stack, mouseX, mouseY, partialTicks) -> screen.renderTooltip(stack, builtTooltip, mouseX, mouseY);
        }
        else return null;
    }
    public static <T extends Enum<?>> String getEnumValueName(T value)
    {
        Name nameAnnotation = getEnumAnnotation(value, Name.class);
        String variableName = StringUtils.enumCaseToTitleCase(value.name());
        if (nameAnnotation != null) variableName = nameAnnotation.value();
        return variableName;
    }
    public static <T extends Enum<?>, A extends Annotation> A getEnumAnnotation(T enumValue, Class<A> annotationClass)
    {
        try
        {
            Class<? extends Enum> enumClass = enumValue.getClass();
            return enumClass.getField(enumValue.name()).getAnnotation(annotationClass);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    public static void runHook(Object instance, Hook hook)
    {
        if (instance != null && hook != null)
        {
            try
            {
                Method method = instance.getClass().getDeclaredMethod(hook.value());
                method.setAccessible(true);
                method.invoke(instance);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
