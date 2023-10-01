package keystone.core.modules.filter.providers;

import keystone.api.Keystone;
import keystone.api.KeystoneCache;
import keystone.core.modules.filter.cache.FilterCache;
import keystone.core.modules.filter.remapper.FilterRemapper;
import keystone.core.utils.FileUtils;
import keystone.core.utils.Result;
import net.minecraft.SharedConstants;
import org.codehaus.commons.compiler.util.resource.DirectoryResourceFinder;
import org.codehaus.commons.compiler.util.resource.FileResourceCreator;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.Compiler;

import java.io.*;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public abstract class AbstractJavaFilterProvider extends AbstractRemappingFilterProvider
{
    protected abstract Result<Void> compileSource(File source, Path compilerWorkspace, Compiler compiler);

    @Override
    protected Result<Path> getNamedFilter(File source, FilterCache.Entry entry)
    {
        Path compilerWorkspace = KeystoneCache.newTempDirectory();
        Result<Path> result = run(source, compilerWorkspace, entry);
        FileUtils.deleteRecursively(compilerWorkspace.toFile(), false);
        return result;
    }

    private Result<Path> run(File source, Path compilerWorkspace, FilterCache.Entry cache)
    {
        // If a compiled JAR file is not cached
        if (!cache.compiled().toFile().isFile())
        {
            // Create a Janino compiler
            Compiler compiler = new Compiler();
            compiler.setTargetVersion(8);
            compiler.setIClassLoader(new ClassLoaderIClassLoader(FilterRemapper.REMAPPED_CLASS_LOADER));
            compiler.setClassFileFinder(new DirectoryResourceFinder(KeystoneCache.getCompiledDirectory().toFile()));
            compiler.setClassFileCreator(new FileResourceCreator() { @Override protected File getFile(String resourceName) { return compilerWorkspace.resolve(resourceName).toFile(); } });

            // Run Compilation Code
            Result<Void> compilationResult = compileSource(source, compilerWorkspace, compiler);
            if (compilationResult.isFailed()) return Result.failed(compilationResult);

            // Create Jar File from Compiled Classes
            try { buildJar(compilerWorkspace, cache.compiled().toFile()); }
            catch (IOException e) { return Result.failed("Unable to build content jar '" + cache.compiled() + "'", e); }
        }

        return Result.success(cache.compiled());
    }

    //region JAR

    private void buildJar(Path classesDirectory, File outputFile) throws IOException
    {
        if (outputFile.exists()) FileUtils.deleteRecursively(outputFile, false);

        Manifest manifest = new Manifest();
        Attributes attributes = new Attributes();
        manifest.getEntries().put("Keystone", attributes);

        attributes.putValue("Minecraft-Version", SharedConstants.getGameVersion().getName());
        attributes.putValue("Filter-API-Version", Keystone.API_VERSION);

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
             JarOutputStream jar = new JarOutputStream(fileOutputStream, manifest))
        {
            addToJar(classesDirectory.toFile(), classesDirectory, jar);
        }
    }
    private void addToJar(File source, Path root, JarOutputStream jar) throws IOException
    {
        Path relative = root.relativize(source.toPath());
        String name = relative.toString().replace("\\", "/");
        if (source.isDirectory())
        {
            // Force trailing '/', as per the ZIP specification
            if (!name.endsWith("/")) name += "/";

            // If this is not the root directory
            if (name.length() > 1)
            {
                // Create a directory entry in the JAR
                JarEntry entry = new JarEntry(name);
                entry.setTime(source.lastModified());
                jar.putNextEntry(entry);
                jar.closeEntry();
            }

            // Add this directory's children to the JAR
            for (File file : source.listFiles()) addToJar(file, root, jar);
        }
        else
        {
            // Create a JAR entry for this file
            JarEntry entry = new JarEntry(name);
            entry.setTime(source.lastModified());
            jar.putNextEntry(entry);

            // Write this file to the JAR
            try (FileInputStream fileInputStream = new FileInputStream(source);
                 BufferedInputStream fileStream = new BufferedInputStream(fileInputStream))
            {
                byte[] buffer = new byte[1024];
                while (true)
                {
                    int count = fileStream.read(buffer);
                    if (count == -1) break;
                    jar.write(buffer, 0, count);
                }
                jar.closeEntry();
            }
        }
    }
    //endregion
}
