package keystone.core.modules.filter.cache;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import keystone.api.KeystoneCache;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FilterCache
{
    public static class Entry
    {
        private final File source;
        private final Path compiled;
        private final Path remapped;
        private final String version;
        private final String checksum;

        private Entry(File source, Path compiled, Path remapped, String version, String checksum)
        {
            this.source = source;
            this.compiled = compiled;
            this.remapped = remapped;
            this.version = version;
            this.checksum = checksum;
        }

        public Path compiled() { return compiled; }
        public Path remapped() { return remapped; }
        public String version() { return version; }

        public static Entry read(NbtCompound nbt)
        {
            File source = new File(nbt.getString("source"));
            Path compiled = KeystoneCache.getCompiledDirectory().resolve(nbt.getString("compiled"));
            Path remapped = KeystoneCache.getRemappedDirectory().resolve(nbt.getString("remapped"));
            String version = nbt.getString("version");
            String checksum = nbt.getString("checksum");
            return new Entry(source, compiled, remapped, version, checksum);
        }
        public NbtCompound write()
        {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("source", source.getPath());
            nbt.putString("compiled", KeystoneCache.getCompiledDirectory().relativize(compiled).toString());
            nbt.putString("remapped", KeystoneCache.getRemappedDirectory().relativize(remapped).toString());
            nbt.putString("version", version);
            nbt.putString("checksum", checksum);
            return nbt;
        }
    }

    private static final Map<String, Map<File, Map<String, Entry>>> entries = new HashMap<>();
    private static Map<File, Map<String, Entry>> usableEntries;

    public static Entry getEntry(File source)
    {
        String checksum = HashCode.fromBytes(checksum(source)).toString();

        try
        {
            source = source.getCanonicalFile();
            Map<String, Entry> entryMap = usableEntries.get(source);
            if (entryMap != null)
            {
                Entry entry = entryMap.get(checksum);
                if (entry != null) return entry;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        String prefix = FilenameUtils.removeExtension(source.getName()) + "-";
        UUID uuid = UUID.randomUUID();
        Path compiled = KeystoneCache.getCompiledDirectory().resolve(prefix + uuid + ".jar");
        Path remapped = KeystoneCache.getRemappedDirectory().resolve(prefix + uuid + ".jar");
        String version = SharedConstants.getGameVersion().getName();
        Entry entry = new Entry(source, compiled, remapped, version, checksum);
        usableEntries.computeIfAbsent(source, (k) -> new HashMap<>()).put(checksum, entry);

        write();
        return entry;
    }

    public static void load()
    {
        entries.clear();
        usableEntries = null;

        File cacheFile = KeystoneCache.getCacheDirectory().resolve("filter_cache.nbt").toFile();
        if (cacheFile.isFile())
        {
            try
            {
                NbtCompound nbt = NbtIo.read(cacheFile);
                NbtList entriesNBT = nbt.getList("entries", NbtElement.COMPOUND_TYPE);
                for (int i = 0; i < entriesNBT.size(); i++)
                {
                    NbtCompound entryNBT = entriesNBT.getCompound(i);
                    Entry entry = Entry.read(entryNBT);
                    entries.computeIfAbsent(entry.version, (version) -> new HashMap<>())
                            .computeIfAbsent(entry.source, (source) -> new HashMap<>())
                            .put(entry.checksum, entry);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        usableEntries = entries.computeIfAbsent(SharedConstants.getGameVersion().getName(), (version) -> new HashMap<>());
    }
    public static void write()
    {
        NbtList entriesNBT = new NbtList();
        for (Map<File, Map<String, Entry>> versionEntries : entries.values())
        {
            for (Map<String, Entry> checksumEntries : versionEntries.values())
            {
                for (Entry entry : checksumEntries.values())
                {
                    entriesNBT.add(entry.write());
                }
            }
        }

        NbtCompound nbt = new NbtCompound();
        nbt.put("entries", entriesNBT);

        try
        {
            File cacheFile = KeystoneCache.getCacheDirectory().resolve("filter_cache.nbt").toFile();
            NbtIo.write(nbt, cacheFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static byte[] checksum(File file)
    {
        try
        {
            if (file.isFile())
            {
                ByteSource byteSource = Files.asByteSource(file);
                HashCode hash = byteSource.hash(Hashing.sha256());
                return hash.asBytes();
            }
            else if (file.isDirectory())
            {
                byte[] hash = new byte[32];
                for (File child : file.listFiles())
                {
                    byte[] childHash = checksum(child);
                    for (int i = 0; i < hash.length; i++) hash[i] ^= childHash[i];
                }
                return hash;
            }
            else throw new RuntimeException("Cannot generate checksum of file '" + file + "'! File is neither a file nor a directory.");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
