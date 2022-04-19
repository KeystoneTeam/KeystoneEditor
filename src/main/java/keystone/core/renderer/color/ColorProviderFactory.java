package keystone.core.renderer.color;

import keystone.core.renderer.Color4f;
import net.minecraft.util.math.Direction;

public final class ColorProviderFactory
{
    public static class ColorProviderBuilder implements IColorProvider
    {
        private IColorProvider provider;

        protected ColorProviderBuilder(IColorProvider parent)
        {
            this.provider = parent;
        }

        public ColorProviderBuilder withAlphaProvider(IAlphaProvider alphaProvider)
        {
            return new ColorProviderBuilder(direction -> this.provider.apply(direction).withAlpha(alphaProvider.apply(direction)));
        }

        @Override
        public Color4f apply(Direction direction)
        {
            return this.provider.apply(direction);
        }
    }

    public static ColorProviderBuilder colorProvider(IColorProvider provider)
    {
        return new ColorProviderBuilder(provider);
    }
    public static ColorProviderBuilder staticColor(Color4f color)
    {
        return new ColorProviderBuilder(direction -> color);
    }
}
