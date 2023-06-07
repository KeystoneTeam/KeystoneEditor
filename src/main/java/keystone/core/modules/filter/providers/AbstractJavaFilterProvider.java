package keystone.core.modules.filter.providers;

import keystone.api.KeystoneCache;
import keystone.core.modules.filter.remapper.FilterRemapper;
import keystone.core.utils.FileUtils;
import keystone.core.utils.Result;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.commons.compiler.util.resource.DirectoryResourceFinder;
import org.codehaus.commons.compiler.util.resource.FileResourceCreator;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.Compiler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public abstract class AbstractJavaFilterProvider implements IFilterProvider
{
    protected abstract Result<Void> compileSource(File source, Path compilerWorkspace, Compiler compiler);

    @Override
    public Result<Path> getFilter(File source)
    {
        Path compilerWorkspace = KeystoneCache.newTempDirectory();
        Result<Path> result = run(source, compilerWorkspace);
        FileUtils.deleteRecursively(compilerWorkspace.toFile(), false);
        return result;
    }

    private Result<Path> run(File source, Path compilerWorkspace)
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
        String jarName = FilenameUtils.removeExtension(source.getName()) + ".jar";
        Path compiledJar = KeystoneCache.getCompiledDirectory().resolve(jarName);
        try { buildJar(compilerWorkspace, compiledJar.toFile()); }
        catch (IOException e) { return Result.failed("Unable to build content jar '" + compiledJar + "'", e); }

        // Run Remapper
        try
        {
            Path remappedJar = KeystoneCache.getRemappedDirectory().resolve(jarName);
            FilterRemapper.remapFile(compiledJar, remappedJar, FilterRemapper.mappings("named", "intermediary"));
            return Result.success(remappedJar);
        }
        catch (IOException e) { return Result.failed("Could not remap compiled filter jar '" + compilationResult.get() + "'", e); }
    }

    //region JAR
    private void buildJar(Path classesDirectory, File outputFile) throws IOException
    {
        if (outputFile.exists()) FileUtils.deleteRecursively(outputFile, false);

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
             JarOutputStream jar = new JarOutputStream(fileOutputStream))
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
