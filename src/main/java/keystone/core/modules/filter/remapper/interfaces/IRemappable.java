package keystone.core.modules.filter.remapper.interfaces;

import keystone.core.modules.filter.remapper.enums.RemappingDirection;
import keystone.core.modules.filter.remapper.mappings.MappingTree;

public interface IRemappable<T>
{
    T remap(RemappingDirection direction, MappingTree mappings);
}
