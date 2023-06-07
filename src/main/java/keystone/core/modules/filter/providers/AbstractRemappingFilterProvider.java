package keystone.core.modules.filter.providers;

import keystone.core.modules.filter.cache.FilterCache;
import keystone.core.modules.filter.remapper.FilterRemapper;
import keystone.core.utils.Result;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractRemappingFilterProvider implements IFilterProvider
{
    protected abstract Result<Path> getNamedFilter(File source, FilterCache.Entry entry);

    @Override
    public Result<Path> getFilter(File source, FilterCache.Entry cache)
    {
        Result<Path> namedJar = getNamedFilter(source, cache);
        if (namedJar.isFailed()) return namedJar;

        // If a remapped JAR file is not cached
        if (!cache.remapped().toFile().isFile())
        {
            // Run Remapper
            try { FilterRemapper.remapFile(namedJar.get(), cache.remapped(), FilterRemapper.NAMED_TO_TARGET); }
            catch (IOException e) { return Result.failed("Could not remap compiled filter jar '" + namedJar.get() + "'", e); }
        }

        return Result.success(cache.remapped());
    }
}
