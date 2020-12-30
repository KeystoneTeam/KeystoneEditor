package keystone.core.renderer.common;

import java.util.HashMap;
import java.util.Map;

public class BoundingBoxType
{
    private static final Map<Integer, BoundingBoxType> structureTypeMap = new HashMap<>();

    public static final BoundingBoxType SelectionBox = register("selection_box");

    private static BoundingBoxType register(String name)
    {
        return structureTypeMap.computeIfAbsent(name.hashCode(), k -> new BoundingBoxType(name));
    }
    public static BoundingBoxType getByNameHash(Integer nameHash)
    {
        return structureTypeMap.get(nameHash);
    }

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
