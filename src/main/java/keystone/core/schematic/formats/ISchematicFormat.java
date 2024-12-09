package keystone.core.schematic.formats;

import keystone.core.schematic.KeystoneSchematic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface ISchematicFormat
{
    String[] getFileExtensions();
    KeystoneSchematic readFile(Path path) throws Exception;
    void writeFile(Path path, KeystoneSchematic schematic) throws IOException;
}
