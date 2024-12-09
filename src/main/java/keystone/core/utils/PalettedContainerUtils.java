package keystone.core.utils;

import keystone.api.Keystone;
import keystone.core.mixins.common.ArrayPaletteAccessor;
import keystone.core.mixins.common.BiMapPaletteAccessor;
import keystone.core.mixins.common.PalettedContainerAccessor;
import keystone.core.mixins.common.SingularPaletteAccessor;
import net.minecraft.world.chunk.*;

public class PalettedContainerUtils
{
    public static <T> PalettedContainer<T> copyContainer(PalettedContainer<T> container)
    {
        PalettedContainer<T> copy = container.copy();
        Palette<T> palette = ((PalettedContainerAccessor<T>)copy).getData().palette();
        
        if (palette instanceof SingularPalette<T>) ((SingularPaletteAccessor<T>)palette).setListener(copy);
        else if (palette instanceof ArrayPalette<T>) ((ArrayPaletteAccessor<T>)palette).setListener(copy);
        else if (palette instanceof BiMapPalette<T>) ((BiMapPaletteAccessor<T>)palette).setListener(copy);
        else if (!(palette instanceof IdListPalette<T>)) Keystone.LOGGER.error("Trying to deep copy unknown palette type '{}'! Cannot update palette resize listener!", palette.getClass().getName());
        
        return copy;
    }
}
