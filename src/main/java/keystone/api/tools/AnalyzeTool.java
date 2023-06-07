package keystone.api.tools;

import keystone.api.KeystoneDirectories;
import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.entities.Entity;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeTool extends KeystoneFilter
{
    public static class Entry
    {
        private int count;
        private final Map<String, Integer> variantCounts;

        public Entry()
        {
            count = 0;
            variantCounts = new HashMap<>();
        }

        public void add(String variant)
        {
            count++;
            if (!variantCounts.containsKey(variant)) variantCounts.put(variant, 1);
            else variantCounts.put(variant, variantCounts.get(variant) + 1);
        }

        public int getCount() { return count; }
        public List<String> getVariants()
        {
            List<String> ret = new ArrayList<>(variantCounts.keySet());
            ret.sort(Comparator.comparingInt(variantCounts::get).reversed());
            return ret;
        }
        public int getVariantCount(String variant) { return variantCounts.get(variant); }
    }

    private Map<String, Entry> blockCounts;
    private Map<String, Integer> entityCounts;

    public AnalyzeTool()
    {
        setName("Analyze");
    }

    @Override
    public void preparePass()
    {
        this.blockCounts = new HashMap<>();
        this.entityCounts = new HashMap<>();
    }
    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        BlockType blockType = region.getBlockType(x, y, z);
        if (!blockCounts.containsKey(blockType.block())) blockCounts.put(blockType.block(), new Entry());
        blockCounts.get(blockType.block()).add(blockType.allProperties());
    }
    @Override
    public void processEntity(Entity entity, WorldRegion region)
    {
        if (!entityCounts.containsKey(entity.type())) entityCounts.put(entity.type(), 1);
        else entityCounts.put(entity.type(), entityCounts.get(entity.type()) + 1);
    }
    @Override
    public void finished()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
        String fileName = "analysis_" + formatter.format(LocalDateTime.now()) + ".txt";

        File file = KeystoneDirectories.getAnalysesDirectory().resolve(fileName).toFile();
        try (FileOutputStream fileOutput = new FileOutputStream(file))
        {
            try (PrintStream printStream = new PrintStream(fileOutput))
            {
                writeBlocks(printStream);
                printStream.println();
                writeEntities(printStream);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            cancel("Failed to write analysis file! Reason: " + e.getLocalizedMessage());
            return;
        }

        Util.getOperatingSystem().open(file);
    }

    public List<String> getBlockEntries()
    {
        List<String> ret = new ArrayList<>(blockCounts.keySet());
        ret.sort(Comparator.comparingInt(a -> blockCounts.get(a).count).reversed());
        return ret;
    }
    public List<String> getEntityEntries()
    {
        List<String> ret = new ArrayList<>(entityCounts.keySet());
        ret.sort(Comparator.comparingInt(a -> entityCounts.get(a)).reversed());
        return ret;
    }
    public Entry getBlockEntry(String block)
    {
        return blockCounts.get(block);
    }
    public int getEntityCount(String entityType)
    {
        return entityCounts.get(entityType);
    }

    private void writeBlocks(PrintStream out)
    {
        int maximumDigits = 0;
        for (Entry entry : blockCounts.values())
        {
            int digits = String.valueOf(entry.count).length();
            if (digits > maximumDigits) maximumDigits = digits;
        }

        out.println("########## BLOCKS ##########");
        for (String block : getBlockEntries())
        {
            Entry entry = blockCounts.get(block);

            int digits = String.valueOf(entry.count).length();
            for (int i = digits; i < maximumDigits; i++) out.print(' ');
            out.print(entry.count);
            out.print(' ');

            if (entry.variantCounts.size() > 1)
            {
                out.println(block);
                writeBlockVariants(out, block, entry, maximumDigits + 1);
            }
            else
            {
                out.print(block);
                String variant = entry.getVariants().get(0);
                if (variant != null && !variant.equals(""))
                {
                    out.print('[');
                    out.print(variant);
                    out.println(']');
                }
                else out.println();
            }
        }
    }
    private void writeBlockVariants(PrintStream out, String type, Entry entry, int startingSpaces)
    {
        int maximumDigits = 0;
        for (int count : entry.variantCounts.values())
        {
            int digits = String.valueOf(count).length();
            if (digits > maximumDigits) maximumDigits = digits;
        }

        for (String variant : entry.getVariants())
        {
            for (int i = 0; i < startingSpaces; i++) out.print(' ');
            out.print("    ");
            int digits = String.valueOf(entry.getVariantCount(variant)).length();
            for (int i = digits; i < maximumDigits; i++) out.print(' ');
            out.print(entry.getVariantCount(variant));
            out.print(' ');
            out.print(type);
            out.print('[');
            out.print(variant);
            out.println(']');
        }
    }
    private void writeEntities(PrintStream out)
    {
        int maximumDigits = 0;
        for (int count : entityCounts.values())
        {
            int digits = String.valueOf(count).length();
            if (digits > maximumDigits) maximumDigits = digits;
        }

        out.println("########## ENTITIES ##########");
        for (String entity : getEntityEntries())
        {
            int count = entityCounts.get(entity);
            int digits = String.valueOf(count).length();
            for (int i = digits; i < maximumDigits; i++) out.print(' ');
            out.print(count);
            out.print(' ');
            out.println(entity);
        }
    }
}
