package keystone.core.modules.filter.remapper.mappings;

import keystone.core.modules.filter.remapper.enums.MappingType;

public class Mapping extends AbstractMappingContainer
{
    private final MappingType type;
    private final String obfuscated;
    private final String deobfuscated;
    
    public Mapping(MappingType type, String obfuscated, String deobfuscated)
    {
        this.type = type;
        this.obfuscated = obfuscated;
        this.deobfuscated = deobfuscated;
    }
    
    public MappingType getType() { return this.type; }
    public String getObfuscated() { return this.obfuscated; }
    public String getDeobfuscated() { return this.deobfuscated; }
    
    @Override
    public String toString()
    {
        return type.name() + " " + deobfuscated + " -> " + obfuscated;
    }
}
