package keystone.core.renderer.common;

import java.util.HashMap;
import java.util.Map;

public class BoundingBoxType
{
    private static final Map<String, BoundingBoxType> typeMap = new HashMap<>();

    public static BoundingBoxType register(String name)
    {
        return typeMap.computeIfAbsent(name, k -> new BoundingBoxType(name));
    }
    public static BoundingBoxType get(String name) { return typeMap.get(name); }

    private final String name;

    private BoundingBoxType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BoundingBoxType other = (BoundingBoxType) obj;
        return this.name.equals(other.name);
    }
}
