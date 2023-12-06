package keystone.core.mixins;

import net.minecraft.structure.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplate.class)
public interface StructureTemplateAccessor
{
    @Accessor("blockInfoLists")
    List<StructureTemplate.PalettedBlockInfoList> getBlockLists();
    
    @Accessor("entities")
    List<StructureTemplate.StructureEntityInfo> getEntities();
}