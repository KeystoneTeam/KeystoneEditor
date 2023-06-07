package keystone.core.modules.filter.providers.impl;

import keystone.core.modules.filter.cache.FilterCache;
import keystone.core.modules.filter.providers.AbstractRemappingFilterProvider;
import keystone.core.utils.Result;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PrecompiledFilterProvider extends AbstractRemappingFilterProvider
{
    public static final PrecompiledFilterProvider INSTANCE = new PrecompiledFilterProvider();
    private PrecompiledFilterProvider() { }

    @Override
    public boolean isSourceSupported(File source) { return source.isFile() && source.getName().toLowerCase().endsWith(".jar"); }

    @Override
    protected Result<Path> getNamedFilter(File source, FilterCache.Entry entry)
    {
        if (!entry.compiled().toFile().isFile())
        {
            try { Files.copy(source.toPath(), entry.compiled(), StandardCopyOption.REPLACE_EXISTING); }
            catch (IOException e) { return Result.failed("Could not copy precompiled filter '" + source + "' to cache path", e); }
        }
        return Result.success(entry.compiled());
    }
}
