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

public class SimpleFilterProvider extends AbstractJavaFilterProvider
{
    public static final SimpleFilterProvider INSTANCE = new SimpleFilterProvider();
    private SimpleFilterProvider() { }

    @Override
    public boolean isSourceSupported(File source) { return source.isFile() && source.getName().toLowerCase().endsWith(".java"); }

    @Override
    protected Result<Void> compileSource(File source, Path compilerWorkspace, Compiler compiler)
    {
        String filterName = KeystoneFilter.getFilterName(source, false);

        // Compile Filter File to Classes
        try { compiler.compile(new File[] { source }); return Result.success(null); }
        catch (CompileException | InternalCompilerException e) { return Result.failed("Unable to compile simple filter '" + filterName + "'", e); }
        catch (IOException e) { return Result.failed("Unable to open content file '" + source.toPath() + "'", e); }
    }
}
