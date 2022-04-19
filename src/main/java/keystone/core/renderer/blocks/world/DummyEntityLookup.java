package keystone.core.renderer.blocks.world;

import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityLookup;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

public class DummyEntityLookup<T extends EntityLike> implements EntityLookup<T>
{
    @Nullable
    @Override
    public T get(int id)
    {
        return null;
    }

    @Nullable
    @Override
    public T get(UUID uuid)
    {
        return null;
    }

    @Override
    public Iterable<T> iterate()
    {
        return Collections.emptyList();
    }

    @Override
    public <U extends T> void forEach(TypeFilter<T, U> filter, Consumer<U> action)
    {

    }

    @Override
    public void forEachIntersects(Box box, Consumer<T> action)
    {

    }

    @Override
    public <U extends T> void forEachIntersects(TypeFilter<T, U> filter, Box box, Consumer<U> action)
    {

    }
}
