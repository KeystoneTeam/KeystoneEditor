package keystone.core.modules.filter;

import keystone.api.variables.Variable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class VariableContainer
{
    private final Map<String, Object> variableMap;

    public VariableContainer(Object instance)
    {
        variableMap = new HashMap<>();
        try
        {
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields)
            {
                Variable variable = field.getAnnotation(Variable.class);
                if (variable != null)
                {
                    field.setAccessible(true);
                    variableMap.put(field.getName(), field.get(instance));
                }
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    public void apply(Object instance)
    {
        try
        {
            for (Map.Entry<String, Object> entry : variableMap.entrySet())
            {
                Field field = instance.getClass().getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(instance, entry.getValue());
            }
        }
        catch (NoSuchFieldException ignored) { }
        catch (IllegalAccessException e) { e.printStackTrace(); }
    }
}
