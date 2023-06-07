package keystone.core.modules.filter.providers.impl;

import keystone.api.filters.KeystoneFilter;
import keystone.core.modules.filter.providers.AbstractJavaFilterProvider;
import keystone.core.utils.Result;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.InternalCompilerException;
import org.codehaus.janino.Compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DirectoryFilterProvider extends AbstractJavaFilterProvider
{
    public static final DirectoryFilterProvider INSTANCE = new DirectoryFilterProvider();
    private DirectoryFilterProvider() { }

    @Override
    public boolean isSourceSupported(File source) { return source.isDirectory(); }

    @Override
    protected Result<Void> compileSource(File source, Path compilerWorkspace, Compiler compiler)
    {
        String filterName = KeystoneFilter.getFilterName(source, false);
        List<File> files = new ArrayList<>();
        addFiles(source, files);
        File[] compilationUnits = files.toArray(File[]::new);

        try { compiler.compile(compilationUnits); return Result.success(null); }
        catch (CompileException | InternalCompilerException e) { return Result.failed("Unable to compile simple filter '" + filterName + "'", e); }
        catch (IOException e) { return Result.failed("Unable to open source file for filter '" + filterName + "'", e); }
    }

    private void addFiles(File directory, List<File> files)
    {
        for (File child : directory.listFiles())
        {
            if (child.isFile()) files.add(child);
            else addFiles(child, files);
        }
    }
}
