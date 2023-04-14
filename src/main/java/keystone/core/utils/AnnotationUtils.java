package keystone.core.utils;

import keystone.api.Keystone;
import keystone.api.utils.StringUtils;
import keystone.api.variables.Hook;
import keystone.api.variables.Name;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.core.gui.IKeystoneTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
            List<Text> builtTooltip = List.of(tooltip.translatable() ? Text.translatable(tooltip.value()) : Text.of(tooltip.value()));
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
}
