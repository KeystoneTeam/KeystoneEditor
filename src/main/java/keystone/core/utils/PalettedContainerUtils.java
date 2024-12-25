package keystone.core.utils;

import keystone.api.Keystone;
import keystone.core.mixins.common.*;
import net.minecraft.world.chunk.*;

import java.util.List;

public class PalettedContainerUtils
{
    public static <T> PalettedContainer<T> copyContainer(PalettedContainer<T> container)
    {
        PalettedContainer<T> copy = container.copy();
        Palette<T> palette = ((PalettedContainerAccessor<T>)copy).getData().palette();
        ((PalettedContainerDataAccessor<T>)(Object)((PalettedContainerAccessor<?>) copy).getData()).setPalette(deepCopyPalette(palette, copy));
        return copy;
    }
    public static <T> Palette<T> deepCopyPalette(Palette<T> original, PaletteResizeListener<T> resizeListener)
    {
        if (original instanceof SingularPalette<T> casted)
        {
            SingularPaletteAccessor<T> accessor = (SingularPaletteAccessor<T>)casted;
            T entry = accessor.getEntry();
            List<T> entries = entry != null ? List.of(entry) : List.of();
            return new SingularPalette<>(accessor.getIdList(), resizeListener, entries);
        }
        else if (original instanceof ArrayPalette<T> casted)
        {
            ((ArrayPaletteAccessor<T>)casted).setListener(resizeListener);
            return casted;
        }
        else if (original instanceof BiMapPalette<T> casted)
        {
            ((BiMapPaletteAccessor<T>)casted).setListener(resizeListener);
            return casted;
        }
        else if (original instanceof IdListPalette<T> casted) { return original; }
        else
        {
            Keystone.LOGGER.error("Unknown palette type: {}", original.getClass().getName());
            return original;
        }
    }
    public static <T> PalettedContainer<T> fromReadableContainer(ReadableContainer<T> container, int sizeX, int sizeY, int sizeZ)
    {
        if (container instanceof PalettedContainer<T> palettedContainer) return copyContainer(palettedContainer);
        else
        {
            PalettedContainer<T> ret = container.slice();
            for (int x = 0; x < sizeX; x++) for (int y = 0; y < sizeY; y++) for (int z = 0; z < sizeZ; z++) ret.set(x, y, z, container.get(x, y, z));
            return ret;
        }
    }
}
