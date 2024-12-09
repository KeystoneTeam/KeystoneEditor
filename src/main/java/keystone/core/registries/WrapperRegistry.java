package keystone.core.registries;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class WrapperRegistry<BaseType, WrapperType>
{
    public interface WrapperFactory<BaseType, WrapperType>
    {
        WrapperType create(short keystoneID, BaseType base);
    }
    
    private final Map<BaseType, Short> idMap = new HashMap<>();
    private final WrapperType[] registry;
    
    public WrapperRegistry(Stream<BaseType> stream, WrapperFactory<BaseType, WrapperType> wrapperFactory)
    {
        short nextID = 1;
        List<WrapperType> wrapperList = new ArrayList<>();
        wrapperList.add(null);
        
        Iterator<BaseType> iterator = stream.iterator();
        while (iterator.hasNext())
        {
            BaseType base = iterator.next();
            WrapperType wrapper = wrapperFactory.create(nextID, base);
            
            idMap.put(base, nextID);
            wrapperList.add(wrapper);
            
            nextID++;
        }
        
        registry = wrapperList.toArray((WrapperType[]) new Object[wrapperList.size()]);
    }
    
    public WrapperType fromKeystoneID(short keystoneID) { return registry[keystoneID]; }
    public WrapperType fromBaseType(BaseType base) { return registry[idMap.getOrDefault(base, (short) 0)]; }
    public short getKeystoneID(BaseType base) { return idMap.getOrDefault(base, (short) 0); }
}
