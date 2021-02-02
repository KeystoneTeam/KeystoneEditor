package keystone.core.utils;

import keystone.api.filters.Name;
import keystone.api.filters.Variable;
import keystone.api.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class AnnotationUtils
{
    public static final String getFieldName(Variable variable, Field field)
    {
        Name nameAnnotation = field.getAnnotation(Name.class);
        String variableName = StringUtils.addSpacesToSentence(StringUtils.titleCase(field.getName().trim()));
        if (!variable.value().trim().isEmpty()) variableName = variable.value().trim();
        if (nameAnnotation != null) variableName = nameAnnotation.value().trim();
        return variableName;
    }
    public static final <T extends Enum<?>> String getEnumValueName(T value)
    {
        Name nameAnnotation = getEnumAnnotation(value, Name.class);
        String variableName = StringUtils.enumCaseToTitleCase(value.name());
        if (nameAnnotation != null) variableName = nameAnnotation.value();
        return variableName;
    }
    public static final <T extends Enum<?>, A extends Annotation> A getEnumAnnotation(T enumValue, Class<A> annotationClass)
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
}
