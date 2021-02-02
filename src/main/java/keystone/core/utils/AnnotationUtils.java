package keystone.core.utils;

import keystone.api.filters.Name;
import keystone.api.filters.Variable;
import keystone.api.utils.StringUtils;

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
}
