package keystone.core.schematic;

import keystone.api.Keystone;
import keystone.core.schematic.formats.ISchematicFormat;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchematicLoader
{
    private static final Map<String, List<ISchematicFormat>> formats = new HashMap<>();

    public static void registerFormat(ISchematicFormat format)
    {
        for (String extension : format.getFileExtensions())
        {
            if (!formats.containsKey(extension.toLowerCase())) formats.put(extension.toLowerCase(), new ArrayList<>());
            formats.get(extension.toLowerCase()).add(format);
        }
    }

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
}
