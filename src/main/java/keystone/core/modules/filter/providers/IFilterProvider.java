package keystone.core.modules.filter.providers;

import keystone.core.utils.Result;

import java.io.File;
import java.nio.file.Path;

public interface IFilterProvider
{
    boolean isSourceSupported(File source);
    Result<Path> getFilter(File source);
}
