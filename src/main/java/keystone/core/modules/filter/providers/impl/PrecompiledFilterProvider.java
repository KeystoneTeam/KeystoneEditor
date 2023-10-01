package keystone.core.modules.filter.providers.impl;

import keystone.api.Keystone;
import keystone.core.modules.filter.cache.FilterCache;
import keystone.core.modules.filter.providers.AbstractRemappingFilterProvider;
import keystone.core.utils.Result;
import net.fabricmc.loader.impl.util.version.SemanticVersionImpl;
import net.minecraft.SharedConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PrecompiledFilterProvider extends AbstractRemappingFilterProvider
{
    public static final PrecompiledFilterProvider INSTANCE = new PrecompiledFilterProvider();
    private PrecompiledFilterProvider() { }

    @Override
    public boolean isSourceSupported(File source) { return source.isFile() && source.getName().toLowerCase().endsWith(".jar"); }

    @Override
    protected Result<Path> getNamedFilter(File source, FilterCache.Entry entry)
    {
        // Copy to Cache
        if (!entry.compiled().toFile().isFile())
        {
            try { Files.copy(source.toPath(), entry.compiled(), StandardCopyOption.REPLACE_EXISTING); }
            catch (IOException e) { return Result.failed("Could not copy precompiled filter '" + source + "' to cache path", e); }
        }

        // Validate Version
        try
        {
            JarFile jar = new JarFile(entry.compiled().toFile());
            Manifest manifest = jar.getManifest();

            Attributes attributes = manifest.getAttributes("Keystone");
            if (attributes == null) return Result.failed("Precompiled filter '" + source + "' does not contain version info in the manifest");

            // Get Version Info
            String minecraftVersion = attributes.getValue("Minecraft-Version");
            SemanticVersionImpl filterAPIVersion = new SemanticVersionImpl(attributes.getValue("Filter-API-Version"), false);
            String currentMCVersion = SharedConstants.getGameVersion().getName();
            SemanticVersionImpl currentFilterAPIVersion = new SemanticVersionImpl(Keystone.API_VERSION, false);

            // Minecraft Version Check
            if (!minecraftVersion.equals(currentMCVersion)) return Result.failed("Filter '" + source + "' was compiled for Minecraft " + minecraftVersion + ", it needs to be for Minecraft " + currentMCVersion);

            // Filter API Version Check
            if (filterAPIVersion.getVersionComponent(0) != currentFilterAPIVersion.getVersionComponent(0) ||
                filterAPIVersion.getVersionComponent(1) < currentFilterAPIVersion.getVersionComponent(1))
            {
                return Result.failed("Filter '" + source + "' was compiled for older filter API version " + filterAPIVersion + ", it must be for " + currentFilterAPIVersion + " or higher");
            }
        }
        catch (Exception e) { return Result.failed("Could not validate precompiled filter version for filter '" + source + "'", e); }

        return Result.success(entry.compiled());
    }
}
