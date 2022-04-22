package keystone.core.schematic;

import keystone.api.Keystone;
import keystone.core.schematic.formats.ISchematicFormat;
import keystone.core.schematic.formats.KeystoneSchematicFormat;
import keystone.core.utils.NBTSerializer;
import net.minecraft.nbt.NbtCompound;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SchematicLoader
{
    private static Set<String> extensions = new HashSet<>();
    private static KeystoneSchematicFormat defaultFormat;
    private static final Map<String, List<ISchematicFormat>> formats = new HashMap<>();

    public static void registerFormat(ISchematicFormat format)
    {
        for (String extension : format.getFileExtensions())
        {
            if (!formats.containsKey(extension.toLowerCase())) formats.put(extension.toLowerCase(), new ArrayList<>());
            formats.get(extension.toLowerCase()).add(format);
            extensions.add(extension);
        }
        if (format instanceof KeystoneSchematicFormat) defaultFormat = (KeystoneSchematicFormat) format;
    }
    public static void finalizeFormats()
    {
        extensions = Collections.unmodifiableSet(extensions);
    }

    public static Set<String> getExtensions() { return extensions; }

    public static NbtCompound serializeSchematic(KeystoneSchematic schematic) { return KeystoneSchematicFormat.saveSchematic(schematic); }
    public static void saveSchematic(KeystoneSchematic schematic, String path) { NBTSerializer.serialize(path, serializeSchematic(schematic)); }
    public static void saveSchematic(KeystoneSchematic schematic, File file) { NBTSerializer.serialize(file, serializeSchematic(schematic)); }

    public static KeystoneSchematic loadSchematic(String path)
    {
        return loadSchematic(new File(path));
    }
    public static KeystoneSchematic loadSchematic(File file)
    {
        Path path = file.toPath();
        if (!Files.exists(path))
        {
            Keystone.LOGGER.info("Trying to load non-existent schematic file '{}'!", file.getName());
            return null;
        }

        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        if (!formats.containsKey(extension))
        {
            Keystone.LOGGER.error("Trying to load unknown schematic format '{}'!", extension);
            return null;
        }

        List<ISchematicFormat> extensionFormats = formats.get(extension);
        for (ISchematicFormat format : extensionFormats)
        {
            KeystoneSchematic schematic = format.loadFile(file);
            if (schematic != null) return schematic;
        }
        return null;
    }
    public static KeystoneSchematic deserializeSchematic(NbtCompound schematicNBT)
    {
        return defaultFormat.deserialize(schematicNBT);
    }
}
