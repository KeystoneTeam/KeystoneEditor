package keystone.core.utils;

import keystone.api.Keystone;
import keystone.api.utils.StringUtils;
import keystone.api.variables.*;
import keystone.core.gui.IKeystoneTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class AnnotationUtils
{
    //region Fields
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
        if (tooltip != null) return IKeystoneTooltip.createSimple(tooltip.translatable() ? Text.translatable(tooltip.value()) : Text.of(tooltip.value()));
        else return null;
    }
    public static Result<Field> findDirtyFlag(Class<?> clazz)
    {
        Field dirtyFlag = null;
        boolean hasAnnotation = false;

        for (Field field : clazz.getDeclaredFields())
        {
            EditorDirtyFlag annotation = field.getAnnotation(EditorDirtyFlag.class);
            if (annotation != null)
            {
                if (!field.getType().equals(boolean.class)) return Result.failed("@EditorDirtyFlag can only be applied to a field of type 'boolean'! The field '" + field.getName() + "' is of type '" + field.getType().getName() + "'");
                else if (hasAnnotation) return Result.failed("A class cannot have multiple @EditorDirtyFlag fields! Both '" + dirtyFlag.getName() + "' and '" + field.getName() + "' have the annotation");
                else
                {
                    dirtyFlag = field;
                    hasAnnotation = true;
                }
            }
            else if (field.getName().equals("editorDirty") && field.getType().equals(boolean.class)) dirtyFlag = field;
        }

        if (dirtyFlag != null) dirtyFlag.setAccessible(true);
        return Result.success(dirtyFlag);
    }
    //endregion
    //region Enums
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
    //endregion
    //region Hooks
    public static void runHook(Object instance, Field field, Hook hook)
    {
        if (hook != null)
        {
            try
            {
                // Find and prepare hook method
                Method[] methods = field.getDeclaringClass().getDeclaredMethods();
                Method method = null;
                for (Method test : methods)
                {
                    if (validateHook(test, field, hook))
                    {
                        if (method == null) method = test;
                        else
                        {
                            hookError("Failed to run hook '" + hook.value() + "'! Multiple valid hooks found.");
                            return;
                        }
                    }
                }
                if (method == null)
                {
                    hookError("Failed to run hook '" + hook.value() + "'! Could not find valid hook.");
                    return;
                }
                method.setAccessible(true);
                
                // Invoke hook method
                if (method.getParameterCount() == 0) method.invoke(instance);
                else method.invoke(instance, field.get(instance));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    private static boolean validateHook(Method method, Field field, Hook hook)
    {
        if (!method.getName().equals(hook.value())) return false;
        if (method.getParameterCount() == 0) return true;
        else return method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(field.getType());
    }
    private static void hookError(String error)
    {
        Keystone.LOGGER.error(error);
        MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)));
    }
    //endregion
}
