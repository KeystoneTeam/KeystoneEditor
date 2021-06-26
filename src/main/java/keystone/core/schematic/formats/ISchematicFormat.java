package keystone.core.schematic.formats;

import keystone.core.schematic.KeystoneSchematic;

import java.io.File;

public interface ISchematicFormat
{
    String[] getFileExtensions();
    KeystoneSchematic loadFile(File file);
}
