package keystone.core.renderer.interfaces;

import keystone.core.renderer.Color4f;
import net.minecraft.util.math.Direction;

public interface IColorProvider
{
    Color4f apply(Direction direction);
}
